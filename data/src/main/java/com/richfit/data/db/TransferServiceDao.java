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
     * 获取无仓库的整单缓存
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

        return null;
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

        //查询抬头缓存
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
                .append("T.RETURN_QUANTITY,T.REF_DOC,T.REF_DOC_ITEM,")
                .append("T.REF_DOC || '_' || T.REF_DOC_ITEM AS LINE_NUM_105 ")
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
            item.lineNum105 = cursor.getString(++index);

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

    /**
     * 获取验收的单条缓存
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
    public ReferenceEntity getInspectTransferInfoSingle(String refCodeId, String refType, String bizType, String refLineId, String workId, String invId, String recWorkId, String recInvId, String materialNum, String batchFlag, String location, String refDoc, int refDocItem, String userId) {
        return null;
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
    public ReferenceEntity getBusinessTransferInfoSingle(String refCodeId, String refType, String bizType, String refLineId, String workId, String invId, String recWorkId, String recInvId, String materialNum, String batchFlag, String location, String refDoc, int refDocItem, String userId) {
        return null;
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
                .append("W.ORG_CODE AS WORK_CODE,W.ORG_NAME AS WORK_NAME,")
                .append("I.ORG_CODE AS INV_CODE,I.ORG_NAME AS INV_NAME,")
                .append("RW.ORG_CODE AS REC_WORK_CODE,RW.ORG_NAME AS REC_WORK_NAME,")
                .append("RI.ORG_CODE AS REC_INV_CODE,RI.ORG_NAME AS REC_INV_NAME,")
                .append("T.INS_LOT,T.DECISION_CODE,T.PROJECT_TEXT,T.MOVE_CAUSE,")
                .append("T.MOVE_CAUSE_DESC,T.RETURN_QUANTITY,T.REF_DOC,T.REF_DOC_ITEM,")
                .append("T.REF_DOC || '_' || T.REF_DOC_ITEM AS LINE_NUM_105 ")
                .append("FROM  MTL_TRANSACTION_LINES T ");
        //二级单位的组织机构
//        sb.append("  LEFT JOIN P_AUTH_ORG2 RW ON T.REC_WORK_ID = RW.ID")
//                .append("  LEFT JOIN P_AUTH_ORG RI ON T.REC_INV_ID = RI.ID, ")
//                .append("  P_AUTH_ORG2               W,")
//                .append("  P_AUTH_ORG2               I,");

        sb.append("  LEFT JOIN P_AUTH_ORG RW ON T.REC_WORK_ID = RW.ORG_ID")
                .append(" LEFT JOIN P_AUTH_ORG RI ON T.REC_INV_ID = RI.ORG_ID")
                .append(" LEFT JOIN P_AUTH_ORG I ON T.INV_ID = I.ORG_ID ")
                .append(" , P_AUTH_ORG               W");
        //条件
        sb.append(" WHERE T.WORK_ID = W.ORG_ID ");
        if (!TextUtils.isEmpty(materialNum)) {
            sb.append(" AND T.MATERIAL_NUM = ?");
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
//            item.specialInvFlag = cursor.getString(++index);
//            item.specialInvNum = cursor.getString(++index);
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
            item.lineNum105 = cursor.getString(++index);
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
