package com.richfit.barcodesystemproduct.module_local.upload.imp;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseArray;

import com.richfit.barcodesystemproduct.base.base_detail.BaseDetailPresenterImp;
import com.richfit.barcodesystemproduct.module_local.upload.UploadContract;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.L;
import com.richfit.domain.bean.RefDetailEntity;
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
 * Created by monday on 2017/4/17.
 */

public class UploadPresenterImp extends BaseDetailPresenterImp<UploadContract.View>
        implements UploadContract.Presenter {

    protected static final String BIZTYPE_DESC_KEY = "bizTypeDesc";
    protected static final String REFTYPE_DESC_KEY = "refTypeDesc";
    protected static final String TRANSTOSAPFLAG_KEY = "transTosapFlag";
    protected int mTaskNum = -1;

    UploadContract.View mView;
    List<ReferenceEntity> mRefDatas;
    /*返回给用户的信息*/
    SparseArray<UploadMsgEntity> mMessageArray;
    /*过账额外的字段信息*/
    HashMap<String, Object> mExtraTransMap;
    /*需要回传的总数*/
    int mTotalUploadDataNum;
    UploadMsgEntity info;

    @Inject
    public UploadPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void onStart() {
        mRefDatas = new ArrayList<>();
        mMessageArray = new SparseArray<>();
        mExtraTransMap = new HashMap<>();
    }

    @Override
    public void readUploadData(int bizType) {
        mView = getView();
        mRefDatas.clear();
        addSubscriber(mRepository.readTransferedData(bizType)
                .filter(list -> list != null && list.size() > 0)
                .flatMap(list -> Flowable.fromIterable(list))
                .filter(refData -> refData != null && refData.billDetailList != null && refData.billDetailList.size() > 0)
                .map(refData -> wrapper2Results(refData, true))
                .compose(TransformerHelper.io2main())
                .subscribeWith(new ResourceSubscriber<ArrayList<ResultEntity>>() {

                    @Override
                    protected void onStart() {
                        super.onStart();
                        if(mView != null) {
                            mView.startReadUploadData();
                        }
                    }

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

    /**
     * 上传出入库离线数据
     */
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
                        .filter(refData -> refData != null && refData.billDetailList != null && refData.billDetailList.size() > 0)
                        .map(refData -> wrapper2Results(refData, false))
                        .flatMap(results -> {
                            mTaskNum++;
                            mMessageArray.get(mTaskNum).taskId = mTaskNum;
                            return mRepository.uploadCollectionDataOffline(results);
                        })
                        .flatMap(message -> submitData2SAPInner(message))
                        .doOnComplete(() -> mRepository.deleteOfflineDataAfterUploadSuccess("", "0", "", ""))
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
    public void resetStateAfterUpload() {
        mRefDatas.clear();
        mTaskNum = -1;
        info = null;
        mMessageArray.clear();
    }

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
        final String bizType = refData.bizType;
        final String transId = refData.transId;
        final String refType = refData.refType;
        final String voucherDate = refData.voucherDate;
        final String userId = refData.recordCreator;
        final String centerCost = refData.costCenter;
        final String projectNum = refData.projectNum;
        final int inspectionType = refData.inspectionType;
        mExtraTransMap.clear();
        mExtraTransMap.put("centerCost", centerCost);
        mExtraTransMap.put("projectNum", projectNum);
        mExtraTransMap.put("inspectionType",inspectionType);
        HashMap<String, String> map = getDescByCode(bizType, refType);
        String transToSapFlag = map.get(TRANSTOSAPFLAG_KEY);
        mMessageArray.get(mTaskNum).materialDoc = materialDoc;
        if (TextUtils.isEmpty(transToSapFlag)) {
            //表示该业务不需要转储
            return mRepository.setTransFlag(bizType, transId).flatMap(a -> Flowable.just("完成!"));
        }
        return mRepository.setTransFlag(bizType, transId).flatMap(a -> Flowable.just("完成!")).zipWith(mRepository.transferCollectionData(transId, bizType, refType,
                userId, voucherDate, transToSapFlag, mExtraTransMap), (s1, s2) -> s2);
    }

    /**
     * 将单据数据转换为Results
     *
     * @param refData
     * @return
     */
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
            info.refType = refData.refType;
            info.transId = refData.transId;
            info.refNum = refData.recordNum;
            info.refCodeId = refData.refCodeId;
            info.workId = refData.workId;
            info.invId = refData.invId;
            info.recWorkId = refData.recWorkId;
            info.recInvId = refData.recWorkId;

            info.bizTypeDesc = map.get(BIZTYPE_DESC_KEY);
            info.refTypeDesc = map.get(REFTYPE_DESC_KEY);
            mMessageArray.put(mTotalUploadDataNum++, info);
            info.totalTaskNum = mTotalUploadDataNum;
        }

        ArrayList<ResultEntity> results = new ArrayList<>();
        final List<RefDetailEntity> details = refData.billDetailList;
        ResultEntity result = null;
        RefDetailEntity item = null;
        for (int i = 0, size = details.size(); i < size; i++) {
            item = details.get(i);
            result = new ResultEntity();
            result.transId = refData.transId;
            result.voucherDate = refData.voucherDate;
            result.refCodeId = refData.refCodeId;
            result.refCode = refData.recordNum;
            result.businessType = refData.bizType;
            result.refType = refData.refType;
            result.moveType = refData.moveType;
            result.invType = refData.invType;
            result.supplierId = refData.supplierId;
            result.supplierNum = refData.supplierNum;
            result.userId = refData.recordCreator;
            //明细
            result.transLineId = item.transLineId;
            result.locationId = item.locationId;
            result.transLineSplitId = item.transLineSplitId;
            result.refLineId = item.refLineId;
            result.refLineNum = item.lineNum;
            result.workId = item.workId;
            result.invId = item.invId;
            result.recWorkId = item.recWorkId;
            result.recInvId = item.recInvId;
            result.materialId = item.materialId;
            result.refDoc = item.refDoc;
            result.refDocItem = item.refDocItem;
            result.insLot = item.insLot;
            result.returnQuantity = item.returnQuantity;
            result.moveCause = item.moveCause;
            result.moveCauseDesc = item.moveCauseDesc;
            result.decisionCode = item.decisionCode;
            result.projectText = item.projectText;
            result.quantity = item.quantity;
            result.recQuantity = item.recQuantity;
            result.location = item.location;
            result.recLocation = item.recLocation;
            result.batchFlag = item.batchFlag;
            result.recBatchFlag = item.recBatchFlag;
            result.specialConvert = item.specialConvert;
            result.materialNum = item.materialNum;
            result.materialDesc = item.materialDesc;
            result.materialGroup = item.materialGroup;
            result.workCode = item.workCode;
            result.invCode = item.invCode;
            result.modifyFlag = "N";

            result.businessTypeDesc = map.get(BIZTYPE_DESC_KEY);
            result.refTypeDesc = map.get(REFTYPE_DESC_KEY);
            results.add(result);
        }
        return results;
    }

    protected HashMap<String, String> getDescByCode(String businessType, String refType) {
        HashMap<String, String> map = new HashMap<>();
        String businessTypeDesc = null;
        String refTypeDesc = null;
        String transToSapFlag = null;
        switch (businessType) {
            case "C01":
                businessTypeDesc = "明盘-无参考";
                transToSapFlag = "";
                break;
            case "C02":
                businessTypeDesc = "盲盘-无参考";
                transToSapFlag = "";
                break;
            case "00":
            case "01":
                businessTypeDesc = "外观验收";
                transToSapFlag = "";
                break;
            case "11":// 采购入库-101
            case "12":// 采购入库-103
                businessTypeDesc = "采购入库-103";
                break;
            case "13":// 采购入库-105(非必检)
                businessTypeDesc = "采购入库-105(非必检)";
                transToSapFlag = "05";
                break;
            case "19":// 委外入库
            case "19_ZJ":// 委外入库-组件
            case "110":// 采购入库-105(青海必检)
            case "21":// 销售出库
            case "23":// 委外发料
            case "24":// 其他出库-有参考
            case "38":// UB 351
                businessTypeDesc = "351转储发出-有参考";
                transToSapFlag = "05";
                break;
            case "311":// UB 101
                businessTypeDesc = "101转储接收-有参考";
                transToSapFlag = "05";
                break;
            case "45":// UB 352
            case "51":// 采购退货-161
                break;
            case "16":// 其他入库-无参考
            case "25":// 其他出库-无参考
            case "26":// 无参考-201
            case "27":// 无参考-221
            case "32":// 301(无参考)
            case "34":// 311(无参考)
                businessTypeDesc = "311移库-无参考";
                transToSapFlag = "05";
                break;
            case "44":// 其他退库-无参考
            case "46":// 无参考-202
            case "47":// 无参考-222
            case "71":// 代管料入库
            case "72":// 代管料出库
            case "73":// 代管料退库
            case "74":// 代管料调拨
            case "91":// 代管料入库-HRM
            case "92":// 代管料出库-HRM
            case "93":// 代管料退库-HRM
            case "94":// 代管料调拨-HRM
                break;
        }
        if (!TextUtils.isEmpty(refType)) {
            switch (refType) {
                case "0":
                    refTypeDesc = "采购订单";
                    break;
            }
        }
        map.put(BIZTYPE_DESC_KEY, businessTypeDesc);
        map.put(REFTYPE_DESC_KEY, refTypeDesc);
        map.put(TRANSTOSAPFLAG_KEY, transToSapFlag);
        return map;
    }
}
