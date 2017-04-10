package com.richfit.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.richfit.common_lib.exception.Exception;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.L;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.repository.IReferenceServiceDao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by monday on 2017/3/29.
 */

public class ReferenceServiceDao extends BaseDao implements IReferenceServiceDao {


    @Inject
    public ReferenceServiceDao(@ContextLife("Application") Context context) {
        super(context);
    }

    /**
     * 获取采购订单
     *
     * @param refNum
     * @param refType
     * @param bizType
     * @param moveType
     * @param refLineId
     * @param userId
     * @return
     */
    @Override
    public ReferenceEntity getPoInfo(String refNum, String refType, String bizType, String moveType, String refLineId, String userId) {
        ReferenceEntity refData = new ReferenceEntity();
        SQLiteDatabase db = getWritableDB();

        try {
            //查询单据抬头
            Cursor cursor = db.rawQuery(createSqlForReadPoInfoHeader(bizType), new String[]{refNum});
            int index;
            while (cursor.moveToNext()) {
                index = -1;
                refData.refCodeId = cursor.getString(++index);
                refData.recordNum = cursor.getString(++index);
                refData.supplierNum = cursor.getString(++index);
                refData.supplierDesc = cursor.getString(++index);
                refData.recordCreator = cursor.getString(++index);
                refData.workId = cursor.getString(++index);
                refData.workCode = cursor.getString(++index);
                refData.workName = cursor.getString(++index);
            }
            cursor.close();
            //如果未获取到抬头的refCodeId，那么直接返回null
            final String refCodeId = refData.refCodeId;
            if (TextUtils.isEmpty(refCodeId)) {
                return refData;
            }

            //3. 获取单据的明细数据
            cursor = db.rawQuery(createSqlForReadPoInfoDetail(), new String[]{refData.refCodeId, bizType});
            ArrayList<RefDetailEntity> billDetailList = new ArrayList<>();
            RefDetailEntity item;

            while (cursor.moveToNext()) {
                item = new RefDetailEntity();
                index = -1;
                item.workId = cursor.getString(++index);
                item.workCode = cursor.getString(++index);
                item.workName = cursor.getString(++index);
                item.invId = cursor.getString(++index);
                item.invCode = cursor.getString(++index);
                item.invName = cursor.getString(++index);
                item.refLineId = cursor.getString(++index);
                final String lineId = cursor.getString(++index);
                if (!TextUtils.isEmpty(lineId)) {
                    item.refLineId = lineId;
                }
                item.refCodeId = cursor.getString(++index);
                item.lineNum = cursor.getString(++index);
                item.materialId = cursor.getString(++index);
                item.materialNum = cursor.getString(++index);
                item.materialDesc = cursor.getString(++index);
                item.materialGroup = cursor.getString(++index);
                item.orderQuantity = cursor.getString(++index);
                item.actQuantity = cursor.getString(++index);
                item.qmFlag = cursor.getString(++index);
                item.unit = cursor.getString(++index);
                item.refDoc = cursor.getString(++index);
                item.refDocItem = cursor.getInt(++index);
                item.insLot = cursor.getString(++index);
                item.insLotQuantity = cursor.getString(++index);
                item.qualifiedQuantity = cursor.getString(++index);
                item.unqualifiedQuantity = cursor.getString(++index);
                item.returnQuantity = cursor.getString(++index);
                item.lineNum105 = cursor.getString(++index);
                billDetailList.add(item);
            }
            cursor.close();
            refData.billDetailList = billDetailList;

            // 3.具体业务相关处理
            switch (bizType) {
                // 庆阳的验收功能相关处理
                case "00":
                    break;
                case "01":
                    // 青海的验收功能相关处理
                    break;
                case "38":// UB/STO发出 351
                case "311":// UB/STO接收 101
                case "11":// 采购入库-101
                case "12":// 采购入库-103
                case "13":// 采购入库-105（非必检）
                case "19":// 委外入库
                case "19_ZJ":// 委外入库-组件
                case "23":// 委外出库
                case "45":// UB/STO退库 352
                    addPOHeaderCache(db, refData, refCodeId, bizType, refType, userId);
                    break;
                case "110":// 青海油田采购入库-105（必检）
                    //这里我们不需要处理非必检情况，因为从服务器获取到的单据数据已经处理过了
                    addPOHeaderCache(db, refData, refCodeId, bizType, refType, userId);
                    break;
            }

        } catch (Exception e) {
            L.e("读取采购订单单据数据出错 = " + e.getMessage());
            return refData;
        } finally {
            db.close();
        }

        return refData;
    }

    private void addPOHeaderCache(SQLiteDatabase db, ReferenceEntity refData, String refCodeId,
                                  String bizType, String refType, String userId) {
        //获取抬头缓存
        clearStringBuffer();
        String[] selections;
        List<String> selectionList = new ArrayList<>();
        sb.append("SELECT H.ID, H.VOUCHER_DATE ")
                .append(" FROM MTL_TRANSACTION_HEADERS H ")
                .append(" WHERE H.TRANS_FLAG = '0' ");
        if (!TextUtils.isEmpty(refType)) {
            sb.append("  AND H.REF_TYPE = ?");
            selectionList.add(refType);
        }
        if (!TextUtils.isEmpty(userId)) {
            sb.append(" AND H.CREATED_BY = ?");
            selectionList.add(userId);
        }
        if (!TextUtils.isEmpty(refCodeId)) {
            sb.append(" AND H.REF_CODE_ID = ?");
            selectionList.add(refCodeId);
        }

        sb.append(" AND H.BIZ_TYPE = ?");
        selectionList.add(bizType);

        selections = new String[selectionList.size()];
        selectionList.toArray(selections);

        int index;
        Cursor cursor = db.rawQuery(sb.toString(), selections);
        while (cursor.moveToNext()) {
            index = -1;
            refData.transId = cursor.getString(++index);
            refData.voucherDate = cursor.getString(++index);
        }
        cursor.close();
    }

    /**
     * 将服务器获取到的采购订单数据保存到本地。这里模拟的是服务器从sap获取单据数据的过程。
     * 区别是保存的可能不是原始单据的信息。
     *
     * @param refData
     * @param bizType
     * @param refType
     */
    @Override
    public void savePoInfo(ReferenceEntity refData, String bizType, String refType) {
        SQLiteDatabase db = getWritableDB();
        try {
            //1. 处理抬头
            String poId = refData.refCodeId;
            clearStringBuffer();
            sb.append("insert or replace into MTL_PO_HEADERS (")
                    .append("ID, PO_NUM,PO_DATE,SUPPLIER_CODE,SUPPLIER_DESC,ZD_FLAG,")
                    .append("TYPE,STATUS,CREATED_BY,CREATION_DATE,LAST_UPDATED_BY,LAST_UPDATE_DATE,WORK_ID,PO_TYPE")
                    .append(")")
                    .append("VALUES (")
                    .append("?,?,?,?,?,?,?,?,?,?,?,?,?,?")
                    .append(")");

            String currentDate = UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1);
            db.execSQL(sb.toString(), new Object[]{poId, refData.recordNum,
                    currentDate, refData.supplierNum, refData.supplierDesc,
                    "ZFD", "1", "Y", refData.recordCreator, currentDate, refData.recordCreator,
                    currentDate, refData.workId, ""});

            //2. 处理明细
            final List<RefDetailEntity> list = refData.billDetailList;
            if (list != null && list.size() > 0) {
                Cursor cursor;
                clearStringBuffer();

                sb.append("insert into MTL_PO_LINES ");
                sb.append("(id,po_id,po_line_id,line_num,work_id,inv_id,material_id,")
                        .append("material_num,material_desc,material_group,")
                        .append("order_quantity,act_quantity,qm_flag,unit,")
                        .append("created_by,creation_date,")
                        .append("send_work_id,send_inv_id,")
                        .append("status,line_type,biz_type,ref_type,")
                        .append("ref_doc,ref_doc_item,ins_lot,ins_lot_quantity,qualified_quantity,")
                        .append("unqualified_quantity,return_quantity,line_num_105)")
                        .append(" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                //先将明细的行的主键查出来
                StringBuffer lineSql = new StringBuffer();
                lineSql.append("select id from MTL_PO_LINES where po_id = ? and biz_type = ? and ref_type = ?");
                switch (bizType) {
                    case "13":
                        lineSql.append(" and line_num_105 = ?");
                        break;
                    default:
                        lineSql.append(" and id = ?");
                        break;
                }

                for (RefDetailEntity data : list) {
                    String poLineId = null;
                    switch (bizType) {
                        case "13":
                            cursor = db.rawQuery(lineSql.toString(), new String[]{poId, bizType, refType, data.lineNum105});
                            break;
                        default:
                            cursor = db.rawQuery(lineSql.toString(), new String[]{poId, bizType, refType, data.refLineId});
                            break;
                    }
                    while (cursor.moveToNext()) {
                        poLineId = cursor.getString(0);
                    }
                    cursor.close();
                    //注意这里需要将bizType和refType保存到单据数据中
                    if (TextUtils.isEmpty(poLineId)) {
                        switch (bizType) {
                            case "13":
                                //对于105非必检，如果没有历史数据，那么需要自动生成一个主键
                                poLineId = UiUtil.getUUID();
                                break;
                            default:
                                poLineId = data.refLineId;
                                break;
                        }
                        db.execSQL(sb.toString(), new Object[]{poLineId, poId, data.refLineId,
                                data.lineNum, data.workId, data.invId, data.materialId, data.materialNum,
                                data.materialDesc, data.materialGroup, data.orderQuantity, data.actQuantity,
                                data.qmFlag, data.unit, refData.recordCreator, currentDate,
                                data.workId, data.invId, "Y", data.lineType, bizType, refType,
                                data.refDoc, data.refDocItem, data.insLot,
                                data.insLotQuantity, data.qualifiedQuantity,
                                data.unqualifiedQuantity, data.returnQuantity, data.lineNum105});//注意105必检将
                    } else {
                        ContentValues cv = new ContentValues();
                        cv.put("last_updated_by", refData.recordCreator);
                        cv.put("last_update_date", currentDate);
                        db.update("MTL_PO_LINES", cv, "po_id = ? and id = ? and biz_type = ?",
                                new String[]{poId, poLineId, bizType});
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }


    /**
     * 拼接读取采购订单单据数据的sql语句
     *
     * @return
     */
    private String createSqlForReadPoInfoHeader(String bizType) {
        clearStringBuffer();
        //输出的字段信息
        sb.append("select po.id,po.po_num,po.supplier_code,po.supplier_desc,")
                .append("po.doc_people,po.work_id,")
                .append("worg.org_code as work_code,worg.org_name as work_name ");
        //查询的表
        sb.append("from mtl_po_headers po left join P_AUTH_ORG worg on ")
                .append("po.work_id = worg.org_id where po.po_num = ?");

        L.e("读取采购订单单据抬头sql = " + sb.toString());
        return sb.toString();
    }

    /**
     * 读取采购订单的单据数据明细
     *
     * @return
     */
    private String createSqlForReadPoInfoDetail() {
        clearStringBuffer();
        sb.append("select L.work_id,WORG.org_code as work_code,WORG.org_name as work_name,")
                .append("L.inv_id,IORG.org_code as inv_code,IORG.org_name as inv_name,")
                .append("L.id,L.po_id,L.po_line_id,L.line_num,L.material_id,")
                .append("L.material_num,L.material_desc,L.material_group,")
                .append("L.order_quantity,L.act_quantity,L.qm_flag,L.unit,")
                .append("L.ref_doc,L.ref_doc_item,L.ins_lot,L.ins_lot_quantity,L.qualified_quantity,")
                .append("L.unqualified_quantity,L.return_quantity,L.line_num_105 ");

        //查询条件
        sb.append(" from MTL_PO_LINES L left join p_auth_org WORG on L.work_id = WORG.org_id ")
                .append(" left join p_auth_org IORG  on L.inv_id = IORG.org_id ");

        //抬头id和行id
        sb.append("where L.po_id = ? and L.biz_type = ? order by L.line_num");
        L.e("读取采购订单单据明细sql = " + sb.toString());
        return sb.toString();
    }


    @Override
    public ReferenceEntity getInspectionInfo(String refNum, String refType, String bizType, String moveType, String refLineId, String userId) {
        return null;
    }

    @Override
    public void saveInspectionInfo(ReferenceEntity refData, String bizType, String refType) {

    }

    @Override
    public ReferenceEntity getReservationInfo(String refNum, String refType, String bizType, String moveType, String refLineId, String userId) {
        return null;
    }

    @Override
    public void saveReservationInfo(ReferenceEntity refData, String bizType, String refType) {

    }

    @Override
    public ReferenceEntity getDeliveryInfo(String refNum, String refType, String bizType, String moveType, String refLineId, String userId) {
        return null;
    }

    @Override
    public void saveDeliveryInfo(ReferenceEntity refData, String bizType, String refType) {

    }
}
