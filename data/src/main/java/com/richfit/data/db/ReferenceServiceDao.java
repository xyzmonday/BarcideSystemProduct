package com.richfit.data.db;

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
            int index = -1;
            while (cursor.moveToNext()) {
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
            //开始查询缓存和明细
            //2. 查询缓存
            cursor = db.rawQuery("select id from mtl_transaction_headers where ref_code_id = ?",
                    new String[]{refCodeId});
            while (cursor.moveToNext()) {
                refData.transId = cursor.getString(0);
            }
            cursor.close();

            //3. 获取明细数据
            Cursor detailCursor = db.rawQuery(createSqlForReadPoInfoDetail(bizType), new String[]{refData.refCodeId, bizType});
            ArrayList<RefDetailEntity> billDetailList = new ArrayList<>();
            RefDetailEntity item;
            index = -1;
            while (detailCursor.moveToNext()) {
                item = new RefDetailEntity();
                item.workId = detailCursor.getString(++index);
                item.workCode = detailCursor.getString(++index);
                item.workName = detailCursor.getString(++index);
                item.invId = detailCursor.getString(++index);
                item.invCode = detailCursor.getString(++index);
                item.invName = detailCursor.getString(++index);
                item.actQuantity = detailCursor.getString(++index);
                item.refLineId = detailCursor.getString(++index);
                item.lineNum = detailCursor.getString(++index);
                item.materialId = detailCursor.getString(++index);
                item.materialNum = detailCursor.getString(++index);
                item.materialDesc = detailCursor.getString(++index);
                item.materialGroup = detailCursor.getString(++index);
                item.unit = detailCursor.getString(++index);
                item.orderQuantity = detailCursor.getString(++index);
                item.qmFlag = detailCursor.getString(++index);
                billDetailList.add(item);
            }
            detailCursor.close();
            refData.billDetailList = billDetailList;
        } catch (Exception e) {
            L.e("读取采购订单单据数据出错 = " + e.getMessage());
            return refData;
        } finally {
            db.close();
        }

        return refData;
    }

    /**
     * 拼接读取采购订单单据数据的sql语句
     *
     * @return
     */
    private String createSqlForReadPoInfoHeader(String bizType) {
        StringBuffer sb = new StringBuffer();
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
            String poId = "";
            Cursor cursor = db.rawQuery("select distinct po.id from mtl_po_headers po where po.po_num = ?",
                    new String[]{refData.recordNum});
            while (cursor.moveToNext()) {
                poId = cursor.getString(0);
            }
            cursor.close();

            if (TextUtils.isEmpty(poId)) {
                //新增一条
                poId = UiUtil.getUUID();
                String currentDate = UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1);
                db.execSQL(insertSelective(), new Object[]{poId, refData.recordNum,
                        currentDate , "", "", "",
                        refData.supplierNum, refData.supplierDesc, "", "", "", "", refData.recordCreator,
                        "ZFD", "", "", "", "", "1", "Y", refData.recordCreator,currentDate,
                        refData.recordCreator, currentDate, refData.workId, ""});
            } else {

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    private String insertSelective() {
        StringBuffer sb = new StringBuffer();
        sb.append("INSERT INTO MTL_PO_HEADERS ")
                .append("(")
                .append("ID, PO_NUM,PO_DATE,PURCHASE_MODE, CONTRACT_NUM, CONTRACT_NAME,")
                .append("SUPPLIER_CODE,SUPPLIER_DESC, PO_ORG, PO_ORG_DESC,")
                .append("PO_GROUP_EN,PO_GROUP_CN,PO_CREATOR,ZD_FLAG,DOC_PEOPLE,")
                .append("DOC_PEOPLE_NAME,DOC_DATE,CONTRACT_AMOUNT,TYPE,")
                .append("STATUS,CREATED_BY,LAST_UPDATED_BY,LAST_UPDATE_DATE,WORK_ID,")
                .append("PO_TYPE")
                .append(")")
                .append("VALUES (")
                .append("?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?")
                .append(")");
        L.e("保存采购订单抬头sql = " + sb.toString());
        return sb.toString();
    }


    /**
     * 创建保存采购订单单据数据明细部分的sql。
     * 注意这里由于不同的业务是使用了同一个应收数量，所以查询条件为
     * refCodeId+bizType
     *
     * @param bizType
     * @return
     */
    private String createSqlForWritePoInfoDetail(String bizType) {
        StringBuffer sb = new StringBuffer();
        sb.append("insert or replace into mtl_po_lines ");
        switch (bizType) {
            case "12":
                sb.append("(id,po_id,biz_type,ref_type,line_num,work_id,material_id,")
                        .append("material_num,material_desc,material_group,unit,act_quantity,order_quantity)")
                        .append(" values(?,?,?,?,?,?,?,?,?,?,?,?,?) ");
                break;
        }
        L.e("保存采购订单单据数据抬头部分sql = " + sb.toString());
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
