package com.richfit.data.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.JsonUtil;
import com.richfit.common_lib.utils.L;
import com.richfit.domain.bean.LocationInfoEntity;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Flowable;

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
    public Flowable<ReferenceEntity> getReference(final String refNum, final String refType,
                                                  final String bizType, final String moveType,
                                                  final String refLineId, final String userId) {
        if (!TextUtils.isEmpty(refType)) {
            switch (refType) {
                //采购订单
                case "0":
                    return Flowable.just(refNum)
                            .flatMap(num -> Flowable.just(getReferenceInfoInternal(num, bizType, refType)))
                            .flatMap(refData -> processError(refData,"未获取到单据数据"));
            }
        }
        return Flowable.error(new Throwable("未获取到单据数据"));
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
    public Flowable<ReferenceEntity> getTransferInfoSingle(String refCodeId, String refType, String bizType, String refLineId, String workId, String invId, String recWorkId, String recInvId, String materialNum,
                                                           String batchFlag, String location, String refDoc, int refDocItem, String userId) {

        if (!TextUtils.isEmpty(refType)) {
            switch (refType) {
                //采购订单
                case "0":
                    return Flowable.just(bizType)
                            .flatMap(type -> Flowable.just(getTransferInfoSingleInternal(refCodeId, refType,
                                    type, refLineId, workId, invId, recWorkId, recInvId, materialNum,
                                    refDoc, refDocItem, userId)))
                            .flatMap(refData -> processError(refData,"未获取到缓存"));
            }
        }
        return Flowable.error(new Throwable("未获取到缓存"));
    }


    /**
     * 统一处理读取单据数据可能发生的错误
     *
     * @param refData
     * @return
     */
    private Flowable<ReferenceEntity> processError(ReferenceEntity refData,final String errorMsg) {
        if (refData == null || refData.billDetailList == null || refData.billDetailList.size() == 0) {
            return Flowable.error(new Throwable(errorMsg));
        }
        return Flowable.just(refData);
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
                headerCursor = db.rawQuery(createSqlForReadHeaderTransSingle(bizType, refType), new String[]{refCodeId});
            } else {
                headerCursor = db.rawQuery(createSqlForReadDetail(bizType, refType), new String[]{refCodeId, userId});
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
                item.refLineId = lineCursor.getString(++index);
                item.lineNum = lineCursor.getString(++index);
                item.materialId = lineCursor.getString(++index);
                item.materialNum = lineCursor.getString(++index);
                item.materialGroup = lineCursor.getString(++index);
                item.materialDesc = lineCursor.getString(++index);
                item.unit = lineCursor.getString(++index);
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
                if(detail.locationList != null && detail.locationList.size() > 0) {
                    hashCache = true;
                    break;
                }
            }
            if(!hashCache)
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
}
