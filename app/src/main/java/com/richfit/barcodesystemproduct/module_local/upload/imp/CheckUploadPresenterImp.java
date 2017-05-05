package com.richfit.barcodesystemproduct.module_local.upload.imp;

import android.content.Context;

import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.L;
import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.ResultEntity;
import com.richfit.domain.bean.UploadMsgEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by monday on 2017/5/2.
 */

public class CheckUploadPresenterImp extends UploadPresenterImp {

    @Inject
    public CheckUploadPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }


    @Override
    public void uploadCollectedDataOffLine() {
        mView = getView();
        mTaskNum = -1;
        info = null;
        if (mRefDatas != null && mRefDatas.size() == 0) {
            mView.uploadCollectDataComplete();
            return;
        }
        ResourceSubscriber<String> subscriber =
                Flowable.fromIterable(mRefDatas)
                        .filter(refData -> refData != null && refData.checkList != null && refData.checkList.size() > 0)
                        .map(refData -> wrapper2Results(refData, false))
                        .flatMap(results -> {
                            mTaskNum++;
                            mMessageArray.get(mTaskNum).taskId = mTaskNum;
                            return mRepository.uploadCheckDataOffline(results);
                        })
                        .flatMap(message -> submitData2SAPInner(message))
                        .doOnComplete(() -> mRepository.deleteOfflineDataAfterUploadSuccess("", "", "", ""))
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new ResourceSubscriber<String>() {

                            @Override
                            protected void onStart() {
                                //注意这里必须回到supper.onStart方法
                                super.onStart();
                                if (mView != null) {
                                    mView.startUploadData(mTotalUploadDataNum);
                                }
                            }

                            @Override
                            public void onNext(String transNum) {
                                L.e("onNext mTaskNum = " + mTaskNum + ";info = " + info);
                                UploadMsgEntity info = mMessageArray.get(mTaskNum);
                                if (mView != null && info != null) {
                                    info.transNum = transNum;
                                    mView.uploadCollectDataSuccess(info);
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                UploadMsgEntity info = mMessageArray.get(mTaskNum);
                                L.e("onError mTaskNum = " + mTaskNum + ";info = " + info);
                                if (mView != null && info != null) {
                                    info.errorMsg = t.getMessage();
                                    info.isEror = true;
                                    mView.uploadCollectDataFail(mMessageArray.get(mTaskNum));
                                }
                            }

                            @Override
                            public void onComplete() {
                                L.e("onComplete");
                                if (mView != null) {
                                    mView.uploadCollectDataComplete();
                                }
                            }
                        });
        addSubscriber(subscriber);
    }

    @Override
    public void readUploadData(int bizType) {
        mView = getView();
        mRefDatas.clear();
        addSubscriber(mRepository.readTransferedData(bizType)
                .filter(list -> list != null && list.size() > 0)
                .flatMap(list -> Flowable.fromIterable(list))
                .filter(refData -> refData != null && refData.checkList != null && refData.checkList.size() > 0)
                .map(refData -> wrapper2Results(refData, true))
                .compose(TransformerHelper.io2main())
                .subscribeWith(new ResourceSubscriber<ArrayList<ResultEntity>>() {
                    @Override
                    public void onNext(ArrayList<ResultEntity> results) {
                        if (mView != null) {
                            mView.showUploadData(results);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (mView != null) {
                            mView.readUploadDataFail(t.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mView != null) {
                            mView.readUploadDataComplete();
                        }
                    }
                }));

    }


    @Override
    /**
     * 根据不同的业务进行转储
     *
     * @param materialDoc:物料凭证
     * @return
     */
    protected Flowable<String> submitData2SAPInner(String materialDoc) {

        if (mRefDatas == null && mTaskNum < 0 && mTaskNum >= mRefDatas.size()) {
            return Flowable.just("完成!");
        }

        final ReferenceEntity refData = mRefDatas.get(mTaskNum);
        final String checkId = refData.checkId;
        final String bizType = refData.bizType;
        return mRepository.setTransFlag(bizType, checkId).flatMap(a -> Flowable.just("完成!"));
    }



    /**
     * 将单据数据转换为Results
     *
     * @param refData
     * @return
     */
    @Override
    protected ArrayList<ResultEntity> wrapper2Results(final ReferenceEntity refData, boolean isSave) {
        if (mRefDatas == null) {
            mRefDatas = new ArrayList<>();
        }
        final HashMap<String, String> map = getDescByCode(refData.bizType, refData.refType);
        if (isSave) {
            mRefDatas.add(refData);
            //记录所有的抬头信息
            info = new UploadMsgEntity();
            info.bizType = refData.bizType;
            info.checkId = refData.checkId;
            info.checkLevel = refData.checkLevel;
            info.specialFlag = refData.specialFlag;
            info.storageNum = refData.storageNum;
            info.checkNum = refData.checkNum;
            info.workCode = refData.workCode;
            info.invCode = refData.invCode;
            info.bizTypeDesc = map.get(BIZTYPE_DESC_KEY);
            info.refTypeDesc = map.get(REFTYPE_DESC_KEY);
            mMessageArray.put(mTotalUploadDataNum++, info);
            info.totalTaskNum = mTotalUploadDataNum;
        }

        ArrayList<ResultEntity> results = new ArrayList<>();
        final List<InventoryEntity> details = refData.checkList;
        ResultEntity result = null;
        InventoryEntity item = null;
        for (int i = 0, size = details.size(); i < size; i++) {
            item = details.get(i);
            result = new ResultEntity();
            result.checkId = refData.checkId;
            result.voucherDate = refData.voucherDate;
            result.storageNum = refData.storageNum;
            result.workId = refData.workId;
            result.invId = refData.invId;
            result.invType = refData.invType;
            result.userId = refData.userId;
            result.checkLevel = refData.checkLevel;
            result.businessType = refData.bizType;
            result.voucherDate = refData.voucherDate;
            result.workCode = refData.workCode;
            result.invCode = refData.invCode;
            result.checkNum = refData.checkNum;
            //明细
            result.specialInvFlag = item.specialInvFlag;
            result.specialInvNum = item.specialInvNum;
            result.checkLineId = item.checkLineId;
            result.location = item.location;
            result.quantity = item.quantity;
            result.batchFlag = item.batchFlag;
            result.materialId = item.materialId;
            result.materialNum = item.materialNum;
            result.materialGroup = item.materialGroup;
            result.materialDesc = item.materialDesc;
            result.unit = item.unit;
            result.modifyFlag = "N";
            result.businessTypeDesc = map.get(BIZTYPE_DESC_KEY);
            result.refTypeDesc = map.get(REFTYPE_DESC_KEY);
            results.add(result);
        }
        return results;
    }
}