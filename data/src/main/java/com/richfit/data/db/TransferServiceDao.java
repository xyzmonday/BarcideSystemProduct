package com.richfit.data.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.richfit.common_lib.scope.ContextLife;
import com.richfit.domain.bean.LocationInfoEntity;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.repository.ITransferServiceDao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by monday on 2017/3/29.
 */

public class TransferServiceDao extends BaseDao implements ITransferServiceDao {

    @Inject
    public TransferServiceDao(@ContextLife("Application") Context context) {
        super(context);
    }

    /**
     * 获取无参考的整单缓存
     *
     * @param refCodeId
     * @param refType
     * @param bizType
     * @param workId
     * @param invId
     * @param recWorkId
     * @param recInvId
     * @param userId
     * @return
     */
    @Override
    public ReferenceEntity getBusinessTransferInfo(String recordNum, String refCodeId, String bizType,
                                                   String refType, String userId, String workId,
                                                   String invId, String recWorkId, String recInvId) {
        SQLiteDatabase db = getWritableDB();
        ReferenceEntity refData = new ReferenceEntity();
        String[] selections;
        ArrayList<String> selectionList = new ArrayList<>();
        ArrayList<RefDetailEntity> billDetailList = new ArrayList<>();
        clearStringBuffer();
        sb.append("select h.id, h.voucher_date, h.remark, h.ref_code_id")
                .append(" from mtl_transaction_headers h ")
                .append(" where h.trans_flag = '0'");
        if (!TextUtils.isEmpty(userId)) {
            sb.append(" and created_by = ?");
            selectionList.add(userId);
        }

        if (!TextUtils.isEmpty(bizType)) {
            sb.append(" and biz_type = ?");
            selectionList.add(bizType);
        }
        selections = new String[selectionList.size()];
        selectionList.toArray(selections);
        Cursor cursor = db.rawQuery(sb.toString(), selections);

        while (cursor.moveToNext()) {
            refData.transId = cursor.getString(0);
            refData.voucherDate = cursor.getString(1);
            refData.remark = cursor.getString(2);
            refData.refCodeId = cursor.getString(3);
        }
        clearStringBuffer();
        cursor.close();

        if (TextUtils.isEmpty(refData.transId))
            return refData;

        // 行缓存
        // orgType 用来标识组织机构的信息是从 ERP_ORG 取值 还是从 HRM_ORG 取值
        String orgType = "p_auth_org";
        switch (bizType) {
            case "91":
            case "92":
            case "93":
            case "94":
                orgType = "p_auth_org2";
                break;
            default:
                break;
        }
        clearStringBuffer();
        String[] lineSelections;
        selectionList.clear();
        sb.append("SELECT T.ID, T.REF_LINE_ID, T.WORK_ID,")
                .append("T.INV_ID,T.REC_WORK_ID,T.REC_INV_ID,")
                .append("T.MATERIAL_ID,T.QUANTITY,")
                .append("M.MATERIAL_NUM,M.MATERIAL_DESC,M.MATERIAL_GROUP,")
                .append("W.ORG_CODE AS WORK_CODE,W.ORG_NAME AS WORK_NAME,")
                .append("I.ORG_CODE AS INV_CODE,I.ORG_NAME AS INV_NAME,")
                .append("RW.ORG_CODE AS REC_WORK_CODE,RW.ORG_NAME AS REC_WORK_NAME,")
                .append("RI.ORG_CODE AS REC_INV_CODE,RI.ORG_NAME AS REC_INV_NAME,")
                .append("T.INS_LOT,T.DECISION_CODE,T.PROJECT_TEXT,T.MOVE_CAUSE,")
                .append("T.MOVE_CAUSE_DESC,T.RETURN_QUANTITY,T.REF_DOC,T.REF_DOC_ITEM  ")
                .append("FROM  MTL_TRANSACTION_LINES T ");

        sb.append("  LEFT JOIN P_AUTH_ORG RW ON T.REC_WORK_ID = RW.ORG_ID")
                .append(" LEFT JOIN ").append(orgType).append(" RI ON T.REC_INV_ID = RI.ORG_ID")
                .append(" LEFT JOIN ").append(orgType).append(" I ON T.INV_ID = I.ORG_ID ")
                .append(" ,").append(orgType).append(" W ,")
                .append("  BASE_MATERIAL_CODE    M ");
        //条件
        sb.append(" WHERE T.WORK_ID = W.ORG_ID ")
                .append(" AND T.MATERIAL_ID = M.ID ");
        sb.append(" AND T.TRANS_ID = ?");
        selectionList.add(refData.transId);

        lineSelections = new String[selectionList.size()];
        selectionList.toArray(lineSelections);

        RefDetailEntity item;
        int index;
        cursor = db.rawQuery(sb.toString(), lineSelections);
        while (cursor.moveToNext()) {
            index = -1;
            item = new RefDetailEntity();
            item.transLineId = cursor.getString(++index);
            item.refLineId = cursor.getString(++index);
            item.workId = cursor.getString(++index);
            item.invId = cursor.getString(++index);
            item.recWorkId = cursor.getString(++index);
            item.recInvId = cursor.getString(++index);
            item.materialId = cursor.getString(++index);
            item.totalQuantity = cursor.getString(++index);
            item.materialNum = cursor.getString(++index);
            item.materialDesc = cursor.getString(++index);
            item.materialGroup = cursor.getString(++index);
            item.workCode = cursor.getString(++index);
            item.workName = cursor.getString(++index);
            item.invCode = cursor.getString(++index);
            item.invName = cursor.getString(++index);
            item.recWorkCode = cursor.getString(++index);
            item.recWorkName = cursor.getString(++index);
            item.recInvCode = cursor.getString(++index);
            item.recInvName = cursor.getString(++index);
            item.insLot = cursor.getString(++index);
            item.decisionCode = cursor.getString(++index);
            item.projectText = cursor.getString(++index);
            item.moveCause = cursor.getString(++index);
            item.moveCauseDesc = cursor.getString(++index);
            item.returnQuantity = cursor.getString(++index);
            item.refDoc = cursor.getString(++index);
            item.refDocItem = cursor.getInt(++index);
            billDetailList.add(item);
        }
        clearStringBuffer();
        cursor.close();

        //获取仓位缓存
        sb.append(" SELECT T.ID,T.TRANS_ID,T.TRANS_LINE_ID,T.LOCATION,")
                .append("L.BATCH_NUM,T.QUANTITY,T.REC_LOCATION,L.REC_BATCH_NUM,")
                .append("L.SPECIAL_FLAG,L.SPECIAL_NUM,L.SPECIAL_CONVERT ")
                .append("FROM MTL_TRANSACTION_LINES_LOCATION T , MTL_TRANSACTION_LINES_SPLIT L")
                .append(" WHERE T.TRANS_LINE_SPLIT_ID = L.ID ")
                .append(" AND T.TRANS_LINE_ID = ?")
                .append(" ORDER BY T.LOCATION");
        for (RefDetailEntity data : billDetailList) {
            ArrayList<LocationInfoEntity> locations = new ArrayList<>();
            LocationInfoEntity locItem;
            cursor = db.rawQuery(sb.toString(), new String[]{data.transLineId});
            while (cursor.moveToNext()) {
                index = -1;
                locItem = new LocationInfoEntity();
                locItem.id = cursor.getString(++index);
                locItem.transId = cursor.getString(++index);
                locItem.transLineId = cursor.getString(++index);
                locItem.location = cursor.getString(++index);
                locItem.batchFlag = cursor.getString(++index);
                locItem.quantity = cursor.getString(++index);
                locItem.recLocation = cursor.getString(++index);
                locItem.recBatchFlag = cursor.getString(++index);
                locItem.specialInvFlag = cursor.getString(++index);
                locItem.specialInvNum = cursor.getString(++index);
                locItem.specialConvert = cursor.getString(++index);
                locItem.locationCombine = !TextUtils.isEmpty(locItem.specialInvFlag) ?
                        locItem.location + "_" + locItem.specialInvFlag + "_" + locItem.specialInvNum :
                        locItem.location;
                locations.add(locItem);
            }
            data.locationList = locations;
            cursor.close();
        }
        //最后确定是否有缓存
        boolean hashCache = false;
        for (RefDetailEntity detail : billDetailList) {
            if (detail.locationList != null && detail.locationList.size() > 0) {
                hashCache = true;
                break;
            }
        }
        if (!hashCache)
            return refData;
        refData.billDetailList = billDetailList;
        db.close();
        return refData;
    }

    /**
     * 获取有参考的整单缓存
     *
     * @param refCodeId
     * @param refType
     * @param bizType
     * @param workId
     * @param invId
     * @param recWorkId
     * @param recInvId
     * @param userId
     * @return
     */
    @Override
    public ReferenceEntity getBusinessTransferInfoRef(String recordNum, String refCodeId, String bizType,
                                                      String refType, String userId, String workId,
                                                      String invId, String recWorkId, String recInvId) {
        // 查 transId 条件bizType、refType、refCodeId、createdBy
        ReferenceEntity refData = new ReferenceEntity();

        clearStringBuffer();
        String[] selections;
        List<String> selectionList = new ArrayList<>();
        SQLiteDatabase db = getWritableDB();

        //查询抬头缓存(注意这里trans_flag)
        sb.append("SELECT H.ID, H.VOUCHER_DATE, H.REMARK, H.REF_CODE_ID ")
                .append("  FROM MTL_TRANSACTION_HEADERS H")
                .append("  WHERE H.TRANS_FLAG = '0'");

        if (!TextUtils.isEmpty(refType)) {
            sb.append(" AND H.REF_TYPE = ?");
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

        if (!TextUtils.isEmpty(bizType)) {
            sb.append(" AND H.BIZ_TYPE = ?");
            selectionList.add("19_ZJ".equals(bizType) ? "19" : bizType);
        }

        selections = new String[selectionList.size()];
        selectionList.toArray(selections);

        Cursor cursor = db.rawQuery(sb.toString(), selections);
        int index;
        while (cursor.moveToNext()) {
            index = -1;
            refData.transId = cursor.getString(++index);
            refData.voucherDate = cursor.getString(++index);
            refData.refCodeId = cursor.getString(++index);
        }
        cursor.close();

        if (TextUtils.isEmpty(refData.transId)) {
            return refData;
        }

        //查询行缓存
        clearStringBuffer();
        selectionList.clear();
        sb.append(" SELECT T.ID,T.REF_LINE_ID,T.WORK_ID,T.INV_ID,")
                .append("T.REC_WORK_ID,T.REC_INV_ID,T.MATERIAL_ID,")
                .append("T.QUANTITY,")
                .append("W.ORG_CODE AS WORK_CODE,")
                .append("W.ORG_NAME AS WORK_NAME,")
                .append("I.ORG_CODE AS INV_CODE,")
                .append("I.ORG_NAME AS INV_NAME,")
                .append("RW.ORG_CODE AS REC_WORK_CODE,")
                .append("RW.ORG_NAME AS REC_WORK_NAME,")
                .append("RI.ORG_CODE AS REC_INV_CODE,")
                .append("RI.ORG_NAME AS REC_INV_NAME,")
                .append("T.QUANTITY,T.INS_LOT,T.DECISION_CODE,T.PROJECT_TEXT,")
                .append("T.MOVE_CAUSE,T.MOVE_CAUSE_DESC,")
                .append("T.RETURN_QUANTITY,T.REF_DOC,T.REF_DOC_ITEM ")

                .append("FROM MTL_TRANSACTION_LINES T ");

        sb.append(" LEFT JOIN P_AUTH_ORG RW ON T.REC_WORK_ID = RW.ORG_ID ")
                .append(" LEFT JOIN P_AUTH_ORG RI ON T.REC_INV_ID = RI.ORG_ID,")
                .append(" P_AUTH_ORG               W, ")
                .append(" P_AUTH_ORG               I ");

        //查询条件Trans_id,bizType
        sb.append(" WHERE T.WORK_ID = W.ORG_ID ")
                .append(" AND T.INV_ID = I.ORG_ID ")
                .append(" AND T.TRANS_ID = ? ");

        cursor = db.rawQuery(sb.toString(), new String[]{refData.transId});
        ArrayList<RefDetailEntity> billDetailList = new ArrayList<>();
        RefDetailEntity item;
        while (cursor.moveToNext()) {
            index = -1;
            item = new RefDetailEntity();
            item.transLineId = cursor.getString(++index);
            item.refLineId = cursor.getString(++index);
            item.workId = cursor.getString(++index);
            item.invId = cursor.getString(++index);
            item.recWorkId = cursor.getString(++index);
            item.recInvId = cursor.getString(++index);
            item.materialId = cursor.getString(++index);
            item.quantity = cursor.getString(++index);
            item.workCode = cursor.getString(++index);
            item.workName = cursor.getString(++index);
            item.invCode = cursor.getString(++index);
            item.invName = cursor.getString(++index);
            item.recWorkCode = cursor.getString(++index);
            item.recWorkName = cursor.getString(++index);
            item.recInvCode = cursor.getString(++index);
            item.recInvName = cursor.getString(++index);
            item.totalQuantity = cursor.getString(++index);
            item.insLot = cursor.getString(++index);
            item.decisionCode = cursor.getString(++index);
            item.projectText = cursor.getString(++index);
            item.moveCause = cursor.getString(++index);
            item.moveCauseDesc = cursor.getString(++index);
            item.returnQuantity = cursor.getString(++index);
            item.refDoc = cursor.getString(++index);
            item.refDocItem = cursor.getInt(++index);
            billDetailList.add(item);
        }
        cursor.close();

        if (billDetailList.size() == 0) {
            return refData;
        }

        //获取仓位缓存
        clearStringBuffer();
        sb.append(" SELECT T.ID,T.TRANS_ID,T.TRANS_LINE_ID,T.LOCATION,")
                .append("L.BATCH_NUM,T.QUANTITY,T.REC_LOCATION,L.REC_BATCH_NUM,")
                .append("L.SPECIAL_FLAG,L.SPECIAL_NUM,L.SPECIAL_CONVERT ")
                .append("FROM MTL_TRANSACTION_LINES_LOCATION T , MTL_TRANSACTION_LINES_SPLIT L")
                .append(" WHERE T.TRANS_LINE_SPLIT_ID = L.ID ")
                .append(" AND T.TRANS_LINE_ID = ?")
                .append(" ORDER BY T.LOCATION");

        for (RefDetailEntity data : billDetailList) {
            ArrayList<LocationInfoEntity> locations = new ArrayList<>();
            LocationInfoEntity locItem;
            cursor = db.rawQuery(sb.toString(), new String[]{data.transLineId});
            while (cursor.moveToNext()) {
                index = -1;
                locItem = new LocationInfoEntity();
                locItem.id = cursor.getString(++index);
                locItem.transId = cursor.getString(++index);
                locItem.transLineId = cursor.getString(++index);
                locItem.location = cursor.getString(++index);
                locItem.batchFlag = cursor.getString(++index);
                locItem.quantity = cursor.getString(++index);
                locItem.recLocation = cursor.getString(++index);
                locItem.recBatchFlag = cursor.getString(++index);
                locItem.specialInvFlag = cursor.getString(++index);
                locItem.specialInvNum = cursor.getString(++index);
                locItem.specialConvert = cursor.getString(++index);
                locItem.locationCombine = !TextUtils.isEmpty(locItem.specialInvFlag) ?
                        locItem.location + "_" + locItem.specialInvFlag + "_" + locItem.specialInvNum :
                        locItem.location;
                locations.add(locItem);
            }
            data.locationList = locations;
            cursor.close();
        }
        //最后确定是否有缓存
        boolean hashCache = false;
        for (RefDetailEntity detail : billDetailList) {
            if (detail.locationList != null && detail.locationList.size() > 0) {
                hashCache = true;
                break;
            }
        }
        if (!hashCache)
            return refData;
        refData.billDetailList = billDetailList;

        db.close();
        return refData;
    }

    /**
     * 获取验收的单条缓存。获取验收的缓存与其他的缓存有区别。
     * 如果没有缓存，默认明细返回一个空的list，而且不View层不能回调
     * 到onError方法。
     *
     * @param refCodeId
     * @param refType
     * @param bizType
     * @param refLineId
     * @param workId
     * @param invId
     * @param recWorkId
     * @param recInvId
     * @param materialNum
     * @param batchFlag
     * @param location
     * @param refDoc
     * @param refDocItem
     * @param userId
     * @return
     */
    @Override
    public ReferenceEntity getInspectTransferInfoSingle(String refCodeId, String refType, String bizType,
                                                        String refLineId, String workId, String invId,
                                                        String recWorkId, String recInvId, String materialNum,
                                                        String batchFlag, String location, String refDoc, int refDocItem, String userId) {
        SQLiteDatabase db = getWritableDB();
        final ReferenceEntity refData = new ReferenceEntity();
        final ArrayList<RefDetailEntity> billDetails = new ArrayList<>();
        clearStringBuffer();
        switch (refType) {
            case "0":
                Cursor cursor = db.rawQuery("select count(*) as count from mtl_inspection_headers t where t.po_id = ? and t.ins_flag = ?",
                        new String[]{refCodeId, "1"});
                int count = -1;
                while (cursor.moveToNext()) {
                    count = cursor.getInt(0);
                }
                cursor.close();
                if (count == 0) {
                    refData.billDetailList = billDetails;
                } else if (count == 1) {
                    // 2.获取具体数据
                    sb.append("select l.id as trans_line_id,l.inspection_id as trans_id,l.po_line_id,")
                            .append("l.work_id,l.inv_id,")
                            .append("l.material_id ,l.unit,")
                            .append("l.quantity,l.qualified_quantity,")
                            .append("l.inspection_result,l.remark,")
                            .append("i.org_code as inv_code,")
                            .append("i.org_name as inv_name,")
                            .append("mpl.order_quantity,custom.random_quantity,")
                            .append("custom.rust_quantity,custom.damaged_quantity,")
                            .append("custom.bad_quantity,custom.other_quantity,")
                            .append("custom.z_package,custom.qm_num,custom.certificate,")
                            .append("custom.instructions,custom.qm_certificate, custom.claim_num,")
                            .append("custom.manufacturer, custom.inspection_quantity ");
                    sb.append(" from mtl_inspection_headers h ,mtl_inspection_lines l ")
                            .append(" left join mtl_inspection_lines_custom custom ")
                            .append(" on l.id = custom.inspection_line_id")
                            .append(" left join p_auth_org i")
                            .append(" on l.inv_id = i.org_id, ")
                            .append(" mtl_po_lines mpl ");
                    sb.append(" where h.id = l.inspection_id   and h.ins_flag = '1'")
                            .append(" and h.po_id = ? and l.po_line_id = ?");
                    cursor = db.rawQuery(sb.toString(), new String[]{refCodeId, refLineId});
                    int index;
                    RefDetailEntity d = null;
                    while (cursor.moveToNext()) {
                        index = -1;
                        d = new RefDetailEntity();
                        // 缓存的id
                        d.transLineId = cursor.getString(++index);
                        d.transId = cursor.getString(++index);
                        d.refLineId = cursor.getString(++index);
                        d.workId = cursor.getString(++index);
                        d.invId = cursor.getString(++index);
                        d.materialId = cursor.getString(++index);
                        d.unit = cursor.getString(++index);
                        // 缓存的应收数量(注意手持端只能看到totalQuantity)
                        d.totalQuantity = cursor.getString(++index);
                        // 缓存的完好数量
                        d.qualifiedQuantity = cursor.getString(++index);
                        // 检验结果
                        d.inspectionResult = cursor.getString(++index);
                        d.remark = cursor.getString(++index);
                        d.invCode = cursor.getString(++index);
                        d.invName = cursor.getString(++index);
                        d.orderQuantity = cursor.getString(++index);
                        // 缓存的抽检数量
                        d.randomQuantity = cursor.getString(++index);
                        // 缓存的锈蚀数量
                        d.rustQuantity = cursor.getString(++index);
                        // 缓存的损坏数量
                        d.damagedQuantity = cursor.getString(++index);
                        // 缓存的变质数量
                        d.badQuantity = cursor.getString(++index);
                        // 缓存的其他数量
                        d.otherQuantity = cursor.getString(++index);
                        // 包装情况
                        d.sapPackage = cursor.getString(++index);
                        // 质检单号
                        d.qmNum = cursor.getString(++index);
                        // 合格证
                        d.certificate = cursor.getString(++index);
                        // 说明书
                        d.instructions = cursor.getString(++index);
                        // 质检证书
                        d.qmCertificate = cursor.getString(++index);
                        // 索赔单
                        d.claimNum = cursor.getString(++index);
                        // 制造商
                        d.manufacturer = cursor.getString(++index);
                        // 送检数
                        d.inspectionQuantity = cursor.getString(++index);
                        billDetails.add(d);
                    }
                    cursor.close();
                    refData.billDetailList = billDetails;
                } else {
                    refData.billDetailList = billDetails;
                }
                break;
        }
        db.close();
        return refData;
    }

    /**
     * 获取无参考的单条缓存
     *
     * @param refCodeId
     * @param refType
     * @param bizType
     * @param refLineId
     * @param workId
     * @param invId
     * @param recWorkId
     * @param recInvId
     * @param materialNum
     * @param batchFlag
     * @param location
     * @param refDoc
     * @param refDocItem
     * @param userId
     * @return
     */
    @Override
    public ReferenceEntity getBusinessTransferInfoSingle(String refCodeId, String refType, String bizType,
                                                         String refLineId, String workId, String invId,
                                                         String recWorkId, String recInvId,
                                                         String materialNum, String batchFlag, String location, String refDoc, int refDocItem, String userId) {
        // 条件
        // 无参考：bizType、createdBy、
        SQLiteDatabase db = getWritableDB();
        ReferenceEntity refData = new ReferenceEntity();
        String[] selections;
        ArrayList<String> selectionList = new ArrayList<>();
        ArrayList<RefDetailEntity> billDetailList = new ArrayList<>();
        clearStringBuffer();
        sb.append("select h.id, h.voucher_date, h.remark, h.ref_code_id")
                .append(" from mtl_transaction_headers h ")
                .append(" where h.trans_flag = '0'");
        if (!TextUtils.isEmpty(userId)) {
            sb.append(" and created_by = ?");
            selectionList.add(userId);
        }

        if (!TextUtils.isEmpty(bizType)) {
            sb.append(" and biz_type = ?");
            selectionList.add(bizType);
        }
        selections = new String[selectionList.size()];
        selectionList.toArray(selections);
        Cursor cursor = db.rawQuery(sb.toString(), selections);

        while (cursor.moveToNext()) {
            refData.transId = cursor.getString(0);
            refData.voucherDate = cursor.getString(1);
            refData.remark = cursor.getString(2);
            refData.refCodeId = cursor.getString(3);
        }
        clearStringBuffer();
        cursor.close();
        if (TextUtils.isEmpty(refData.transId)) {
            // 如果没有查询到缓存 则只赋值物料信息
            sb.append("select distinct id,material_num,material_desc,material_group,unit ")
                    .append(" from BASE_MATERIAL_CODE ")
                    .append(" where material_num = ?");
            cursor = db.rawQuery(sb.toString(), new String[]{materialNum});
            RefDetailEntity item;
            while (cursor.moveToNext()) {
                item = new RefDetailEntity();
                item.materialId = cursor.getString(0);
                item.materialNum = cursor.getString(1);
                item.materialDesc = cursor.getString(2);
                item.materialGroup = cursor.getString(3);
                item.unit = cursor.getString(4);
                billDetailList.add(item);
            }
            clearStringBuffer();
            cursor.close();
            if (billDetailList.size() == 0 || billDetailList.size() > 1) {
                //直接返回，回到onError
                return refData;
            }
        } else {
            // 行缓存
            // orgType 用来标识组织机构的信息是从 ERP_ORG 取值 还是从 HRM_ORG 取值
            String orgType = "p_auth_org";
            switch (bizType) {
                case "91":
                case "92":
                case "93":
                case "94":
                    orgType = "p_auth_org2";
                    break;
                default:
                    break;
            }
            clearStringBuffer();
            String[] lineSelections;
            selectionList.clear();
            sb.append("SELECT T.ID, T.REF_LINE_ID, T.WORK_ID,")
                    .append("T.INV_ID,T.REC_WORK_ID,T.REC_INV_ID,")
                    .append("T.MATERIAL_ID,T.QUANTITY,")
                    .append("M.MATERIAL_NUM,M.MATERIAL_DESC,M.MATERIAL_GROUP,")
                    .append("W.ORG_CODE AS WORK_CODE,W.ORG_NAME AS WORK_NAME,")
                    .append("I.ORG_CODE AS INV_CODE,I.ORG_NAME AS INV_NAME,")
                    .append("RW.ORG_CODE AS REC_WORK_CODE,RW.ORG_NAME AS REC_WORK_NAME,")
                    .append("RI.ORG_CODE AS REC_INV_CODE,RI.ORG_NAME AS REC_INV_NAME,")
                    .append("T.INS_LOT,T.DECISION_CODE,T.PROJECT_TEXT,T.MOVE_CAUSE,")
                    .append("T.MOVE_CAUSE_DESC,T.RETURN_QUANTITY,T.REF_DOC,T.REF_DOC_ITEM  ")
                    .append("FROM  MTL_TRANSACTION_LINES T ");

            sb.append("  LEFT JOIN P_AUTH_ORG RW ON T.REC_WORK_ID = RW.ORG_ID")
                    .append(" LEFT JOIN ").append(orgType).append(" RI ON T.REC_INV_ID = RI.ORG_ID")
                    .append(" LEFT JOIN ").append(orgType).append(" I ON T.INV_ID = I.ORG_ID ")
                    .append(" ,").append(orgType).append(" W ,")
                    .append("  BASE_MATERIAL_CODE    M ");
            //条件
            sb.append(" WHERE T.WORK_ID = W.ORG_ID ")
                    .append(" AND T.MATERIAL_ID = M.ID ");
            if (!TextUtils.isEmpty(materialNum)) {
                sb.append(" AND M.MATERIAL_NUM = ?");
                selectionList.add(materialNum);
            }

            if (!TextUtils.isEmpty(refDoc)) {
                sb.append(" AND T.REF_DOC = ?");
                selectionList.add(refDoc);
            }

            if (refDocItem > 0) {
                sb.append(" AND T.REF_DOC_ITEM = ?");
                selectionList.add(String.valueOf(refDocItem));
            }

            if (!TextUtils.isEmpty(bizType) && "19_ZJ".equalsIgnoreCase(bizType)) {
                sb.append(" AND T.REF_DOC IS NOT NULL");
            }

            if (!TextUtils.isEmpty(bizType) && "19".equalsIgnoreCase(bizType)) {
                sb.append(" AND T.REF_DOC IS NULL");
            }

            sb.append(" AND T.TRANS_ID = ?");
            selectionList.add(refData.transId);

            lineSelections = new String[selectionList.size()];
            selectionList.toArray(lineSelections);

            RefDetailEntity item;
            int index;
            cursor = db.rawQuery(sb.toString(), lineSelections);
            while (cursor.moveToNext()) {
                index = -1;
                item = new RefDetailEntity();
                item.transLineId = cursor.getString(++index);
                item.refLineId = cursor.getString(++index);
                item.workId = cursor.getString(++index);
                item.invId = cursor.getString(++index);
                item.recWorkId = cursor.getString(++index);
                item.recInvId = cursor.getString(++index);
                item.materialId = cursor.getString(++index);
                item.totalQuantity = cursor.getString(++index);
                item.materialNum = cursor.getString(++index);
                item.materialDesc = cursor.getString(++index);
                item.materialGroup = cursor.getString(++index);
                item.workCode = cursor.getString(++index);
                item.workName = cursor.getString(++index);
                item.invCode = cursor.getString(++index);
                item.invName = cursor.getString(++index);
                item.recWorkCode = cursor.getString(++index);
                item.recWorkName = cursor.getString(++index);
                item.recInvCode = cursor.getString(++index);
                item.recInvName = cursor.getString(++index);
                item.insLot = cursor.getString(++index);
                item.decisionCode = cursor.getString(++index);
                item.projectText = cursor.getString(++index);
                item.moveCause = cursor.getString(++index);
                item.moveCauseDesc = cursor.getString(++index);
                item.returnQuantity = cursor.getString(++index);
                item.refDoc = cursor.getString(++index);
                item.refDocItem = cursor.getInt(++index);
                billDetailList.add(item);
            }
            clearStringBuffer();
            cursor.close();

            if (billDetailList.size() == 0) {
                // 没有缓存 赋值物料信息
                sb.append("select distinct id,material_num,material_desc,material_group,unit ")
                        .append(" from BASE_MATERIAL_CODE ")
                        .append(" where material_num = ?");
                cursor = db.rawQuery(sb.toString(), new String[]{materialNum});
                while (cursor.moveToNext()) {
                    item = new RefDetailEntity();
                    item.materialId = cursor.getString(0);
                    item.materialNum = cursor.getString(1);
                    item.materialDesc = cursor.getString(2);
                    item.materialGroup = cursor.getString(3);
                    item.unit = cursor.getString(4);
                    billDetailList.add(item);
                }
                clearStringBuffer();
                cursor.close();
                if (billDetailList.size() == 0 || billDetailList.size() > 1) {
                    //直接返回，回到onError
                    return refData;
                }
            } else {
                // 有缓存
                //获取仓位缓存
                clearStringBuffer();
                sb.append(" SELECT T.ID,T.TRANS_ID,T.TRANS_LINE_ID,T.LOCATION,")
                        .append("L.BATCH_NUM,T.QUANTITY,T.REC_LOCATION,L.REC_BATCH_NUM,")
                        .append("L.SPECIAL_FLAG,L.SPECIAL_NUM ")
                        .append("FROM MTL_TRANSACTION_LINES_LOCATION T , MTL_TRANSACTION_LINES_SPLIT L")
                        .append(" WHERE T.TRANS_LINE_SPLIT_ID = L.ID ")
                        .append(" AND T.TRANS_LINE_ID = ?")
                        .append(" ORDER BY T.LOCATION");
                for (RefDetailEntity data : billDetailList) {
                    ArrayList<LocationInfoEntity> locations = new ArrayList<>();
                    LocationInfoEntity locItem;
                    cursor = db.rawQuery(sb.toString(), new String[]{data.transLineId});
                    while (cursor.moveToNext()) {
                        index = -1;
                        locItem = new LocationInfoEntity();
                        locItem.id = cursor.getString(++index);
                        locItem.transId = cursor.getString(++index);
                        locItem.transLineId = cursor.getString(++index);
                        locItem.location = cursor.getString(++index);
                        locItem.batchFlag = cursor.getString(++index);
                        locItem.quantity = cursor.getString(++index);
                        locItem.recLocation = cursor.getString(++index);
                        locItem.recBatchFlag = cursor.getString(++index);
                        locItem.specialInvFlag = cursor.getString(++index);
                        locItem.specialInvNum = cursor.getString(++index);
                        locItem.locationCombine = !TextUtils.isEmpty(locItem.specialInvFlag) ?
                                locItem.location + "_" + locItem.specialInvFlag + "_" + locItem.specialInvNum :
                                locItem.location;
                        locations.add(locItem);
                    }
                    data.locationList = locations;
                    cursor.close();
                }
                //最后确定是否有缓存
                boolean hashCache = false;
                for (RefDetailEntity detail : billDetailList) {
                    if (detail.locationList != null && detail.locationList.size() > 0) {
                        hashCache = true;
                        break;
                    }
                }
                if (!hashCache)
                    return refData;
            }
        }
        db.close();
        refData.billDetailList = billDetailList;
        return refData;
    }

    /**
     * 获取有参考的单条缓存
     *
     * @param refCodeId
     * @param refType
     * @param businessType
     * @param refLineId
     * @param workId
     * @param invId
     * @param recWorkId
     * @param recInvId
     * @param materialNum
     * @param batchFlag
     * @param location
     * @param refDoc
     * @param refDocItem
     * @param userId
     * @return
     */
    @Override
    public ReferenceEntity getBusinessTransferInfoSingleRef(String refCodeId, String refType, String businessType,
                                                            String refLineId, String workId, String invId, String recWorkId,
                                                            String recInvId, String materialNum, String batchFlag, String location,
                                                            String refDoc, int refDocItem, String userId) {
        // 条件
        // 有参考：bizType、refType、refCodeId
        SQLiteDatabase db = getWritableDB();
        ReferenceEntity refData = new ReferenceEntity();
        clearStringBuffer();
        String[] selections;
        List<String> selectionList = new ArrayList<>();

        //获取抬头缓存
        sb.append("SELECT H.ID, H.VOUCHER_DATE, H.REMARK, H.REF_CODE_ID ")
                .append(" FROM MTL_TRANSACTION_HEADERS H ")
                .append(" WHERE H.TRANS_FLAG = '0' ");

        if (!TextUtils.isEmpty(refType)) {
            sb.append("  AND REF_TYPE = ?");
            selectionList.add(refType);
        }

        if (!TextUtils.isEmpty(userId)) {
            sb.append(" AND CREATED_BY = ?");
            selectionList.add(userId);
        }

        if (!TextUtils.isEmpty(refCodeId)) {
            sb.append(" AND REF_CODE_ID = ?");
            selectionList.add(refCodeId);
        }

        sb.append(" AND BIZ_TYPE = ?");
        selectionList.add(businessType);

        selections = new String[selectionList.size()];
        selectionList.toArray(selections);
        int index;
        Cursor cursor = db.rawQuery(sb.toString(), selections);
        while (cursor.moveToNext()) {
            index = -1;
            refData.transId = cursor.getString(++index);
            refData.voucherDate = cursor.getString(++index);
            refData.remark = cursor.getString(++index);
            refData.refCodeId = cursor.getString(++index);
        }
        cursor.close();

        if (TextUtils.isEmpty(refData.transId)) {
            //如果没有获取到抬头的缓存
            return refData;
        }

        //获取明细缓存 条件 transId refLineId
        clearStringBuffer();
        String[] lineSelections;
        selectionList.clear();
        sb.append("SELECT T.ID, T.REF_LINE_ID, T.WORK_ID,")
                .append("T.INV_ID,T.REC_WORK_ID,T.REC_INV_ID,")
                .append("T.MATERIAL_ID,T.QUANTITY,")
                .append("M.MATERIAL_NUM,M.MATERIAL_DESC,M.MATERIAL_GROUP,")
                .append("W.ORG_CODE AS WORK_CODE,W.ORG_NAME AS WORK_NAME,")
                .append("I.ORG_CODE AS INV_CODE,I.ORG_NAME AS INV_NAME,")
                .append("RW.ORG_CODE AS REC_WORK_CODE,RW.ORG_NAME AS REC_WORK_NAME,")
                .append("RI.ORG_CODE AS REC_INV_CODE,RI.ORG_NAME AS REC_INV_NAME,")
                .append("T.INS_LOT,T.DECISION_CODE,T.PROJECT_TEXT,T.MOVE_CAUSE,")
                .append("T.MOVE_CAUSE_DESC,T.RETURN_QUANTITY,T.REF_DOC,T.REF_DOC_ITEM ")
                .append("FROM  MTL_TRANSACTION_LINES T ");

        sb.append("  LEFT JOIN P_AUTH_ORG RW ON T.REC_WORK_ID = RW.ORG_ID")
                .append(" LEFT JOIN P_AUTH_ORG RI ON T.REC_INV_ID = RI.ORG_ID")
                .append(" LEFT JOIN P_AUTH_ORG I ON T.INV_ID = I.ORG_ID ")
                .append(" , P_AUTH_ORG               W,")
                .append("  BASE_MATERIAL_CODE    M ");
        //条件
        sb.append(" WHERE T.WORK_ID = W.ORG_ID ")
                .append(" AND T.MATERIAL_ID = M.ID ");
        if (!TextUtils.isEmpty(materialNum)) {
            sb.append(" AND M.MATERIAL_NUM = ?");
            selectionList.add(materialNum);
        }
        if (!TextUtils.isEmpty(refLineId)) {
            sb.append(" AND T.REF_LINE_ID = ?");
            selectionList.add(refLineId);
        }
        if (!TextUtils.isEmpty(refDoc)) {
            sb.append(" AND T.REF_DOC = ?");
            selectionList.add(refDoc);
        }

        if (refDocItem > 0) {
            sb.append(" AND T.REF_DOC_ITEM = ?");
            selectionList.add(String.valueOf(refDocItem));
        }

        if (!TextUtils.isEmpty(businessType) && "19_ZJ".equalsIgnoreCase(businessType)) {
            sb.append(" AND T.REF_DOC IS NOT NULL");
        }

        if (!TextUtils.isEmpty(businessType) && "19".equalsIgnoreCase(businessType)) {
            sb.append(" AND T.REF_DOC IS NULL");
        }

        sb.append(" AND T.TRANS_ID = ?");
        selectionList.add(refData.transId);

        lineSelections = new String[selectionList.size()];
        selectionList.toArray(lineSelections);

        ArrayList<RefDetailEntity> billDetailList = new ArrayList<>();
        RefDetailEntity item;

        cursor = db.rawQuery(sb.toString(), lineSelections);
        while (cursor.moveToNext()) {
            index = -1;
            item = new RefDetailEntity();
            item.transLineId = cursor.getString(++index);
            item.refLineId = cursor.getString(++index);
            item.workId = cursor.getString(++index);
            item.invId = cursor.getString(++index);
            item.recWorkId = cursor.getString(++index);
            item.recInvId = cursor.getString(++index);
            item.materialId = cursor.getString(++index);
            item.totalQuantity = cursor.getString(++index);
            item.materialNum = cursor.getString(++index);
            item.materialDesc = cursor.getString(++index);
            item.materialGroup = cursor.getString(++index);
            item.workCode = cursor.getString(++index);
            item.workName = cursor.getString(++index);
            item.invCode = cursor.getString(++index);
            item.invName = cursor.getString(++index);
            item.recWorkCode = cursor.getString(++index);
            item.recWorkName = cursor.getString(++index);
            item.recInvCode = cursor.getString(++index);
            item.recInvName = cursor.getString(++index);
            item.insLot = cursor.getString(++index);
            item.decisionCode = cursor.getString(++index);
            item.projectText = cursor.getString(++index);
            item.moveCause = cursor.getString(++index);
            item.moveCauseDesc = cursor.getString(++index);
            item.returnQuantity = cursor.getString(++index);
            item.refDoc = cursor.getString(++index);
            item.refDocItem = cursor.getInt(++index);
            billDetailList.add(item);
        }
        cursor.close();

        if (billDetailList.size() == 0) {
            return refData;
        }

        //获取仓位缓存
        clearStringBuffer();
        sb.append(" SELECT T.ID,T.TRANS_ID,T.TRANS_LINE_ID,T.LOCATION,")
                .append("L.BATCH_NUM,T.QUANTITY,T.REC_LOCATION,L.REC_BATCH_NUM,")
                .append("L.SPECIAL_FLAG,L.SPECIAL_NUM ")
                .append("FROM MTL_TRANSACTION_LINES_LOCATION T , MTL_TRANSACTION_LINES_SPLIT L")
                .append(" WHERE T.TRANS_LINE_SPLIT_ID = L.ID ")
                .append(" AND T.TRANS_LINE_ID = ?")
                .append(" ORDER BY T.LOCATION");
        for (RefDetailEntity data : billDetailList) {
            ArrayList<LocationInfoEntity> locations = new ArrayList<>();
            LocationInfoEntity locItem;
            cursor = db.rawQuery(sb.toString(), new String[]{data.transLineId});
            while (cursor.moveToNext()) {
                index = -1;
                locItem = new LocationInfoEntity();
                locItem.id = cursor.getString(++index);
                locItem.transId = cursor.getString(++index);
                locItem.transLineId = cursor.getString(++index);
                locItem.location = cursor.getString(++index);
                locItem.batchFlag = cursor.getString(++index);
                locItem.quantity = cursor.getString(++index);
                locItem.recLocation = cursor.getString(++index);
                locItem.recBatchFlag = cursor.getString(++index);
                locItem.specialInvFlag = cursor.getString(++index);
                locItem.specialInvNum = cursor.getString(++index);
                locItem.locationCombine = !TextUtils.isEmpty(locItem.specialInvFlag) ?
                        locItem.location + "_" + locItem.specialInvFlag + "_" + locItem.specialInvNum :
                        locItem.location;
                locations.add(locItem);
            }
            data.locationList = locations;
            cursor.close();
        }
        //最后确定是否有缓存
        boolean hashCache = false;
        for (RefDetailEntity detail : billDetailList) {
            if (detail.locationList != null && detail.locationList.size() > 0) {
                hashCache = true;
                break;
            }
        }
        if (!hashCache)
            return refData;
        refData.billDetailList = billDetailList;
        db.close();
        return refData;
    }
}
