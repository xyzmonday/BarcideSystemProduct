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
            cursor = db.rawQuery(createSqlForReadPoInfoDetail(bizType), new String[]{refData.refCodeId, bizType});
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
                item.actQuantity = cursor.getString(++index);
                item.refLineId = cursor.getString(++index);
                item.lineNum = cursor.getString(++index);
                item.materialId = cursor.getString(++index);
                item.materialNum = cursor.getString(++index);
                item.materialDesc = cursor.getString(++index);
                item.materialGroup = cursor.getString(++index);
                item.unit = cursor.getString(++index);
                item.orderQuantity = cursor.getString(++index);
                item.qmFlag = cursor.getString(++index);
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
                    "ZFD", "1", "Y",refData.recordCreator,currentDate, refData.recordCreator,
                    currentDate, refData.workId, ""});

            //处理明细
            final List<RefDetailEntity> list = refData.billDetailList;
            Cursor cursor;
            if (list != null && list.size() > 0) {
                clearStringBuffer();
                sb.append("insert into MTL_PO_LINES ");
                sb.append("(ID,PO_ID,LINE_NUM,WORK_ID,INV_ID,MATERIAL_ID,")
                        .append("MATERIAL_NUM,MATERIAL_DESC,MATERIAL_GROUP,")
                        .append("ORDER_QUANTITY,ACT_QUANTITY,QM_FLAG,UNIT,")
                        .append("CREATED_BY,CREATION_DATE,LAST_UPDATED_BY,")
                        .append("LAST_UPDATE_DATE,SEND_WORK_ID,SEND_INV_ID,")
                        .append("STATUS,LINE_TYPE,BIZ_TYPE,REF_TYPE)")
                        .append(" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

                for (RefDetailEntity data : list) {

                    String poLineId = "";
                    //获取该行的行id(注意这里的查询条件)
                    cursor = db.rawQuery("select id from MTL_PO_LINES where po_id = ?  and biz_type = ? and ref_type = ?",
                            new String[]{poId, bizType, refType});

                    while (cursor.moveToNext()) {
                        poLineId = cursor.getString(0);
                    }
                    cursor.close();
                    //注意这里需要将bizType和refType保存到单据数据中
                    if (TextUtils.isEmpty(poLineId)) {
                        poLineId = UiUtil.getUUID();
                        db.execSQL(sb.toString(), new Object[]{poLineId, poId,
                                data.lineNum, data.workId, data.invId, data.materialId, data.materialNum,
                                data.materialDesc, data.materialGroup, data.orderQuantity, data.actQuantity,
                                data.qmFlag, data.unit, refData.recordCreator, currentDate, "", "",
                                data.workId, data.invId, "Y", data.lineType, bizType, refType});
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
        sb.append("select ");
        switch (bizType) {
            //103-出库
            case "12":
                sb.append(" po.id,po.po_num,po.supplier_code,po.supplier_desc,")
                        .append("po.doc_people,po.work_id,")
                        .append("worg.org_code as work_code,worg.org_name as work_name ");

                break;
        }
        //查询的表
        sb.append("from mtl_po_headers po left join P_AUTH_ORG worg on ")
                .append("po.work_id = worg.org_id where po.po_num = ?");

        L.e("读取采购订单单据抬头sql = " + sb.toString());
        return sb.toString();
    }

    /**
     * 读取采购订单的单据数据明细
     *
     * @param bizType
     * @return
     */
    private String createSqlForReadPoInfoDetail(String bizType) {
        StringBuffer sb = new StringBuffer();
        sb.append("select ");
        //输出的字段
        switch (bizType) {
            case "00":
                sb.append("L.work_id,WORG.org_code as work_code,WORG.org_name as work_name,")
                        .append("L.inv_id,IORG.org_code as inv_code,IORG.org_name as inv_name,");
                break;
            case "01":
                sb.append("L.work_id,WORG.org_code as work_code ,WORG.org_name as work_name,")
                        .append("L.inv_id,IORG.org_code as inv_code ,IORG.org_name as inv_name,")
                        .append("L.act_quantity,");
                break;
            case "38":
                sb.append("L.send_work_id as work_id,SWORG.org_code as work_code,SWORG.org_name as work_name,")
                        .append("L.send_inv_id as inv_id,SIORG.org_code as inv_code,SIORG.org_name as inv_name,")
                        .append("L.act_quantity,");
                break;
            case "311":
                sb.append("L.work_id,WORG.org_code as work_code,WORG.org_name as work_name,")
                        .append("L.inv_id,IORG.org_code as inv_code,IORG.org_name as inv_name,")
                        .append("L.act_quantity,");
                break;
            case "11":
                sb.append("L.work_id,WORG.org_code as work_code,WORG.org_name as work_name,")
                        .append("L.inv_id,IORG.org_code as inv_code,IORG.org_name as inv_name,")
                        .append("L.act_quantity,");
                break;
            case "12":
                sb.append("L.work_id,WORG.org_code as work_code,WORG.org_name as work_name,")
                        .append("L.inv_id,IORG.org_code as inv_code,IORG.org_name as inv_name,")
                        .append("L.act_quantity,");
                break;
            case "13":
                sb.append("L.work_id,WORG.org_code as work_code,WORG.org_name as work_name,")
                        .append("L.inv_id,IORG.org_code as inv_code,IORG.org_name as inv_name,")
                        .append("H.ref_mat_doc,H.ref_mat_doc_item,")
                        .append("H.ref_mat_doc ")
                        .append(" || ").append("'_'").append(" || ").append("H.ref_mat_doc_item")
                        .append(" as line_num_105")
                        .append("H.act_quantity as act_quantity");
                break;
            case "110":
                sb.append("L.work_id,WORG.org_code as work_code,WORG.org_name as work_name,")
                        .append("L.inv_id,IORG.org_code as inv_code,IORG.org_name as inv_name,")
                        .append("INSR.ins_lot,INSR.mat_doc as ref_mat_doc,")
                        .append("INSR.mat_doc_item as ref_mat_doc_item")
                        .append("INSR.ins_lot_quantity,")
                        .append("INSR.qualified_quantity as act_quantity")
                        .append("INSR.unquanlified_quantity,INSR.unqalified_quantity as return_quantity");
                break;
            case "19":
                sb.append("L.work_id,WORG.org_code as work_code,WORG.org_name as work_name,")
                        .append("L.inv_id,IORG.org_code as inv_code,IORG.org_name as inv_name,")
                        .append("L.act_quantity,").append("L.line_type");
                break;
            case "19_ZJ":
                sb.append("MRL.reservation_num as ref_mat_doc,MRL.line_num as ref_mat_doc_item,")
                        .append("MRL.work_id as work_id,")
                        .append("RWORG.org_code as work_code")
                        .append("RWORG.org_name as work_name")
                        .append("RIORG.org_code as inv_code")
                        .append("RIORG.org_name as org_name")
                        .append("MRL.material_id ,RM.material_num,RM.material_desc,RM.material_group")
                        .append("MRL.order_quantity,MRL.order_quantity as act_quantity");
                break;
            case "23":
                sb.append("MRL.work_id as work_id ,RWORG.org_code as work_code ,")
                        .append("MRL.inv_id as inv_id,MRL.org_name as inv_name")
                        .append("MRL.material_id ,RM.material_num,RM.material_desc,RM.material_group")
                        .append("MRL.order_quantity,MRL.order_quantity as act_quantity")
                        .append("MRL.last_flag,");
                break;
            case "45":
                sb.append("L.send_work_id as work_id , SWORG.org_code as org_code")
                        .append("SWORG.org_name as org_name ,L.send_inv_id as inv_id,")
                        .append("SIORG.org_code as inv_code ,SIORG.org_name as inv_name,")
                        .append("L.act_quantity");
                break;
            case "51":
                sb.append("L.work_id,WORG.org_code as work_code,")
                        .append("WORG.org_name as work_name")
                        .append("IORG.org_code as inv_code,")
                        .append("IORG.org_name as inv_name,")
                        .append("L.act_quantity,")
                        .append("L.is_return,L.line_type");
                break;
        }
        //查询的表
        sb.append(" L.id, l.line_num,  L.material_id, L.material_num,  L.material_desc,  L.material_group,")
                .append(" L.unit,L.order_quantity, L.qm_flag ")
                .append(" from mtl_po_lines L  left join p_auth_org WORG ")
                .append(" on L.work_id = WORG.org_id left join p_auth_org IORG ")
                .append(" on L.inv_id = IORG.org_id left join p_auth_org SWORG ")
                .append(" on L.send_work_id = SWORG.org_id  left join p_auth_org SIORG ")
                .append(" on L.send_inv_id = SIORG.org_id ");

        switch (bizType) {
            case "01":
                sb.append(" left join mtl_po_lines_custom CUSTOM ")
                        .append(" on L.id = CUSTOM.po_line_id");
                break;
            case "13":
                sb.append(", MTL_PO_HISTORY H ");
                break;
            case "110":
                sb.append(", MTL_INSPECTION_RESULT INSR ");
                break;
            case "23":
                sb.append(", MTL_RESERVATION_LINES MRL left join p_auth_org RWORG on MRL.work_id = RWORG.id ")
                        .append("left join p_auth_org RIORG on MRL.inv_id = RIORG.id")
                        .append("left join BASE_MATERIAL_CODE RM on MRL.material_id = RM.id");
                break;
            case "19_ZJ":
                sb.append(", MTL_RESERVATION_LINES MRL left join p_auth_org RWORG on MRL.work_id = RWORG.id ")
                        .append("left join p_auth_org RIORG on MRL.inv_id = RIORG.id")
                        .append("left join BASE_MATERIAL_CODE RM on MRL.material_id = RM.id ");
                break;


        }

        //查询条件
        sb.append(" WHERE 1 = 1 ");

        switch (bizType) {
            case "13":
                sb.append("and L.id = H.po_line_id");
                break;
            case "110":
                sb.append("and  L.id = INSR.po_line_id");
                break;
            case "23":
                sb.append("and L.id = MRL.po_line_id");
                break;
            case "19_ZJ":
                sb.append("and L.id = MRL.po_line_id");
        }

        //抬头id和行id
        sb.append("and L.po_id = ? and L.biz_type = ? order by L.line_num");
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
