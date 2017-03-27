package com.richfit.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.richfit.common_lib.exception.Exception;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.JsonUtil;
import com.richfit.common_lib.utils.L;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.LocationInfoEntity;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.ResultEntity;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * 物资入库操作数据库的Dao层。该类包括了有参考和无参考的所有离线出入库操作。
 * Created by monday on 2017/3/21.
 */

public class ASDao extends BaseDao {

    @Inject
    public ASDao(@ContextLife("Application") Context context) {
        super(context);
    }

    @Override
    public void saveReferenceInfo(ReferenceEntity refData, String bizType, String refType) {
        if (!TextUtils.isEmpty(refType)) {
            //如果单据类型不为空,那么需要判断是什么类型的单据
            switch (refType) {
                //采购订单
                case "0":
                    //2.插入抬头，如果抬头表里面有数据，那么更新，如果没有那么插入。
                    saveReferenceInfoInternal(refData, bizType, refType);
                    break;
            }
        }
    }

    /**
     * 获取单据数据
     *
     * @param refNum
     * @param bizType
     * @param refType
     * @return
     */
    @Override
    public ReferenceEntity getReference(final String refNum, final String refType, final String bizType, final String moveType,
                                        final String refLineId, final String userId) {
        if (!TextUtils.isEmpty(refType)) {
            switch (refType) {
                //采购订单
                case "0":
                    return getReferenceInfoInternal(refNum, bizType, refType);
            }
        }
        return null;
    }


    /**
     * 获取单条缓存
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
    public ReferenceEntity getTransferInfoSingle(String refCodeId, String refType, String bizType,
                                                 String refLineId, String workId, String invId,
                                                 String recWorkId, String recInvId, String materialNum,
                                                 String batchFlag, String location, String refDoc,
                                                 int refDocItem, String userId) {

        if (!TextUtils.isEmpty(refType)) {
            switch (refType) {
                //采购订单
                case "0":
                    return getTransferInfoSingleInternal(refCodeId, refType, bizType, refLineId, workId, invId,
                            recWorkId, recInvId, materialNum, refDoc, refDocItem, userId);
            }
        }
        return null;
    }

    /**
     * 保存单条缓存
     *
     * @param result
     * @return
     */
    @Override
    public boolean uploadCollectionDataSingle(ResultEntity result) {
        final String bizType = result.businessType;
        if (TextUtils.isEmpty(bizType)) {
            return false;
        }
        switch (bizType) {
            case "12":
                return uploadCollectionDataSingleInternal(result);

        }
        return false;
    }


    /**
     * 保存103-入库的单据数据
     *
     * @param refData
     * @param bizType
     * @param refType
     */
    private void saveReferenceInfoInternal(ReferenceEntity refData, String bizType, String refType) {
        SQLiteDatabase db = getWritableDB();
        try {
            db.execSQL(createSqlForWriteHeader(bizType, refType), new Object[]{refData.refCodeId, refData.recordNum
                    , refData.voucherDate, refData.recordCreator, refData.supplierNum,
                    refData.supplierDesc, refData.workCode, "FZD", "1", "Y"});

            //3.插入明细数据
            final String sql = createSqlForWriteDetail(bizType, refType);
            for (RefDetailEntity lineData : refData.billDetailList) {
                db.execSQL(sql, new Object[]{lineData.refLineId, refData.refCodeId,
                        bizType, refType, lineData.lineNum, lineData.workId, lineData.materialId, lineData.materialNum, lineData.materialDesc,
                        lineData.materialGroup, lineData.unit, lineData.actQuantity, lineData.orderQuantity});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    /**
     * 读取103-出库单据数据
     *
     * @param refNum
     * @param bizType
     * @param refType
     * @return
     */
    private ReferenceEntity getReferenceInfoInternal(String refNum, String bizType, String refType) {
        ReferenceEntity refData = new ReferenceEntity();
        SQLiteDatabase db = getWritableDB();
        try {
            Cursor headerCursor = db.rawQuery(createSqlForReadHeader(bizType, refType), new String[]{refNum});
            int index = -1;
            while (headerCursor.moveToNext()) {
                refData.refCodeId = headerCursor.getString(++index);
                refData.recordNum = headerCursor.getString(++index);
                refData.supplierNum = headerCursor.getString(++index);
                refData.supplierDesc = headerCursor.getString(++index);
                refData.recordCreator = headerCursor.getString(++index);
                refData.workId = headerCursor.getString(++index);
                refData.workCode = headerCursor.getString(++index);
                refData.workName = headerCursor.getString(++index);
            }
            headerCursor.close();

            //如果未获取到抬头的refCodeId，那么直接返回null
            final String refCodeId = refData.refCodeId;
            if (TextUtils.isEmpty(refCodeId)) {
                return refData;
            }
            //开始查询缓存和明细
            //2. 查询缓存
            Cursor transCursor = db.rawQuery("select id from MTL_TRANSACTION_HEADERS where ref_code_id = ?",
                    new String[]{refCodeId});
            while (transCursor.moveToNext()) {
                refData.transId = transCursor.getString(0);
            }

            //3. 获取明细数据
            Cursor detailCursor = db.rawQuery(createSqlForReadDetail(bizType, refType), new String[]{refData.refCodeId, bizType});
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
            e.printStackTrace();
            return refData;
        } finally {
            db.close();
        }
        L.e("读取到的明细数据" + (JsonUtil.object2Json(refData)));
        return refData;
    }

    /**
     * 注意这里的userId的作用是，无参考时作为查询条件保证每一个用户的业务互不干扰
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
     * @param refDoc
     * @param refDocItem
     * @param userId
     * @return
     */
    private ReferenceEntity getTransferInfoSingleInternal(String refCodeId, String refType, String bizType, String refLineId,
                                                          String workId, String invId, String recWorkId, String recInvId, String materialNum,
                                                          String refDoc, int refDocItem, String userId) {
        ReferenceEntity refData = new ReferenceEntity();
        SQLiteDatabase db = getWritableDB();
        try {
            Cursor headerCursor;
            if (!TextUtils.isEmpty(refType)) {
                headerCursor = db.rawQuery(createSqlForReadHeaderTransSingle(bizType, refType),
                        new String[]{refCodeId});
            } else {
                headerCursor = db.rawQuery(createSqlForReadDetail(bizType, refType),
                        new String[]{refCodeId, userId});
            }
            int index = -1;
            while (headerCursor.moveToNext()) {
                refData.transId = headerCursor.getString(++index);
                refData.refCodeId = headerCursor.getString(++index);
                refData.voucherDate = headerCursor.getString(++index);
            }
            headerCursor.close();
            if (TextUtils.isEmpty(refData.transId)) {
                //没有缓存
                return refData;
            }
            //读取明细数据
            Cursor lineCursor = db.rawQuery(createSqlForReadDetailTransSingle(bizType, refType), new String[]{refData.transId});
            ArrayList<RefDetailEntity> details = new ArrayList<>();
            RefDetailEntity item;
            index = -1;
            while (lineCursor.moveToNext()) {
                item = new RefDetailEntity();
                item.workId = lineCursor.getString(++index);
                item.workCode = lineCursor.getString(++index);
                item.workName = lineCursor.getString(++index);
                item.invId = lineCursor.getString(++index);
                item.invCode = lineCursor.getString(++index);
                item.invName = lineCursor.getString(++index);
                item.transLineId = lineCursor.getString(++index);
                item.transId = lineCursor.getString(++index);
                item.refLineId = lineCursor.getString(++index);
                item.lineNum = lineCursor.getString(++index);
                item.materialId = lineCursor.getString(++index);
                item.totalQuantity = lineCursor.getString(++index);
                details.add(item);
            }
            lineCursor.close();
            //读取仓位级缓存(注意这里需要满足第三级有数据才能认为有缓存，所以明细数据在第三级读取完后才能连接抬头)
            if (details == null || details.size() == 0)
                return refData;
            for (RefDetailEntity detail : details) {
                ArrayList<LocationInfoEntity> locations = new ArrayList<>();
                LocationInfoEntity locItem;
                index = -1;
                Cursor locCursor = db.rawQuery(createSqlForReadLocTransSingle(bizType, refType), new String[]{
                        refData.transId, detail.transLineId});
                while (lineCursor.moveToNext()) {
                    locItem = new LocationInfoEntity();
                    locItem.id = locCursor.getString(++index);
                    locItem.location = locCursor.getString(++index);
                    locItem.batchFlag = locCursor.getString(++index);
                    locItem.quantity = locCursor.getString(++index);
                    locItem.recLocation = locCursor.getString(++index);
                    locItem.recBatchFlag = locCursor.getString(++index);
                    locations.add(locItem);
                }
                detail.locationList = locations;
                locCursor.close();
            }
            //最后确定是否有缓存
            boolean hashCache = false;
            for (RefDetailEntity detail : details) {
                if (detail.locationList != null && detail.locationList.size() > 0) {
                    hashCache = true;
                    break;
                }
            }
            if (!hashCache)
                return refData;
            refData.billDetailList = details;
        } catch (Exception e) {
            e.printStackTrace();
            return refData;
        } finally {
            db.close();
        }
        return refData;
    }

    /**
     * 保存单条缓存
     *
     * @param result
     * @return
     */
    private boolean uploadCollectionDataSingleInternal(ResultEntity result) {
        SQLiteDatabase db = getWritableDB();
        //检查必要的字段
        final String bizType = result.businessType;
        final String refType = result.refType;
        final String refCodeId = result.refCodeId;
        final String materialId = result.materialId;
        if (TextUtils.isEmpty(bizType) || TextUtils.isEmpty(refCodeId) || TextUtils.isEmpty(materialId)) {
            return false;
        }
        //1. 保存抬头(由于保存的数据没有缓存头的主键Id,所以需要先查询是否存在)
        try {
            L.e("refCodeId = " + refCodeId);
            Cursor headerCursor = db.rawQuery(createSqlForWriteHeaderTransSingle(bizType, refType),
                    new String[]{refCodeId});
            String transId = null;
            long row = -1;
            while (headerCursor.moveToNext()) {
                transId = headerCursor.getString(0);
            }
            headerCursor.close();

            //如果不存在，那么直接插入一条数据
            ContentValues cv;
            if (TextUtils.isEmpty(transId)) {
                cv = new ContentValues();
                //随机生成一个主键
                transId = UiUtil.getUUID();
                cv.put("id",transId);
                cv.put("ref_code_id", refCodeId);
                cv.put("biz_type", bizType);
                cv.put("ref_type", refType);
                cv.put("voucher_date", result.voucherDate);
                cv.put("created_by", result.userId);
                row = db.insert("mtl_transaction_headers", null, cv);
            } else {
                //如果存在那么更新该行数据
                cv = new ContentValues();
                cv.put("voucher_date", result.voucherDate);
                cv.put("created_by", result.userId);
                row = db.update("mtl_transaction_headers", cv, "id = ?", new String[]{transId});
            }
            if (row <= 0)
                return false;

            //2. 保存明细行
            if (cv == null) {
                cv = new ContentValues();
            }
            cv.clear();

            final String refLineId = result.refLineId;
            String transLineId = "";
            if (TextUtils.isEmpty(refLineId)) {
                //注意无参考没有行id，此时要用work_id ,inv_id, material_id来查找行id
            } else {
                Cursor detailCursor = db.rawQuery(createSqlForWriteDetailTransSingle(bizType, refType),
                        new String[]{transId, refLineId});
                while (detailCursor.moveToNext()) {
                    transLineId = detailCursor.getString(0);
                }
                detailCursor.close();
                if (TextUtils.isEmpty(transLineId)) {
                    //如果还没有缓存行
                    transLineId = UiUtil.getUUID();
                    cv.put("id", transLineId);
                    cv.put("trans_id", transId);
                    cv.put("ref_line_id", refLineId);
                    cv.put("line_num", result.refLineNum);
                    cv.put("work_id", result.workId);
                    cv.put("inv_id", result.invId);
                    cv.put("material_id", result.materialId);
                    cv.put("ref_doc", result.refDoc);
                    cv.put("ref_doc_item", result.refDocItem);
                    cv.put("created_by", result.userId);
                    cv.put("quantity", result.quantity);
                    db.insert("MTL_TRANSACTION_LINES", null, cv);
                } else {
                    //如果有那么更新即可
                    db.execSQL("update MTL_TRANSACTION_LINES set quantity = quantity +  ? where id = ?",
                            new Object[]{refLineId});
                }
            }

            //3. 插入仓位级的缓存
            Cursor locCursor = db.rawQuery(createSqlForWriteLocTransSingle(bizType, refType), new String[]{transId, transLineId});
            String locationId = "";
            cv.clear();
            while (locCursor.moveToNext()) {
                locationId = locCursor.getString(0);
            }
            if (TextUtils.isEmpty(locationId)) {
                cv.put("id", UiUtil.getUUID());
                cv.put("trans_id",transId);
                cv.put("trans_line_id",transLineId);
                cv.put("location", result.location);
                cv.put("batch_num", result.batchFlag);
                cv.put("quantity", result.quantity);
                cv.put("rec_location", result.recLocation);
                cv.put("rec_batch_num", result.recBatchFlag);
                db.insert("MTL_TRANSACTION_LINES_LOCATION", null, cv);
            } else {
                db.execSQL("update MTL_TRANSACTION_LINES_LOCATION set quantity = quantity + " + result.quantity +
                        " where trans_id = ? and trans_line_id = ?", new Object[]{transId, transLineId});
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            L.e("保存缓存出错 = " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }
}
