package com.richfit.barcodesystemproduct.module_local.upload;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseArray;

import com.richfit.barcodesystemproduct.base.BasePresenter;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.ResultEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by monday on 2017/4/17.
 */

public class UploadPresenterImp extends BasePresenter<UploadContract.View>
        implements UploadContract.Presenter {
    int mTaskNum = -1;

    UploadContract.View mView;
    List<ReferenceEntity> mRefDatas;
    HashMap<String, Object> mExtraTransMap;
    SparseArray<String> mMessageArray;

    @Inject
    public UploadPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void onStart() {
        mRefDatas = new ArrayList<>();
        mExtraTransMap = new HashMap<>();
        mMessageArray = new SparseArray<>();
    }

    @Override
    public void readUploadData() {
        mView = getView();

        addSubscriber(mRepository.readTransferedData()
                .filter(list -> list != null && list.size() > 0)
                .flatMap(list -> Flowable.fromIterable(list))
                .filter(refData -> refData != null && refData.billDetailList != null && refData.billDetailList.size() > 0)
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

                    }
                }));

    }

    @Override
    public void uploadCollectedDataOffLine() {
        mView = getView();
        mTaskNum = -1;
        mMessageArray.clear();
        ResourceSubscriber<String> subscriber = Flowable.fromIterable(mRefDatas)
                .filter(refData -> refData != null && refData.billDetailList != null && refData.billDetailList.size() > 0)
                .map(refData -> wrapper2Results(refData, false))
                .flatMap(results -> mRepository.uploadCollectionDataOffline(results))
                .flatMap(a -> submitData2SAPInner(++mTaskNum))
                .compose(TransformerHelper.io2main())
                .subscribeWith(new ResourceSubscriber<String>() {
                    @Override
                    public void onNext(String message) {
                        if (mView != null) {
                            mView.uploadCollectDataSuccess(mTaskNum, message);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (mView != null) {
                            mView.uploadCollectDataFail(t.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mView != null) {
                            mView.uploadCollectDataComplete();
                        }
                    }
                });
        addSubscriber(subscriber);
    }

    private Flowable<String> submitData2SAPInner(int taskNum) {
        if (mRefDatas == null && taskNum < 0 && mTaskNum >= mRefDatas.size()) {
            return Flowable.empty();
        }
        final ReferenceEntity refData = mRefDatas.get(taskNum);
        final String bizType = refData.bizType;
        final String transId = refData.transId;
        final String refType = refData.refType;
        final String voucherDate = refData.voucherDate;
        final String userId = refData.recordCreator;
        final int inspectionType = refData.inspectionType;
        final String centerCost = refData.costCenter;
        final String projectNum = refData.projectNum;
        mExtraTransMap.clear();
        mExtraTransMap.put("centerCost", centerCost);
        mExtraTransMap.put("projectNum", projectNum);
        String transToSapFlag = null;
        switch (bizType) {
            case "00":
            case "01":
                break;
            case "11":// 采购入库-101
            case "12":// 采购入库-103
                break;
            case "13":// 采购入库-105(非必检)
                transToSapFlag = "05";
                break;
            case "19":// 委外入库
            case "19_ZJ":// 委外入库-组件
            case "110":// 采购入库-105(青海必检)
            case "21":// 销售出库
            case "23":// 委外发料
            case "24":// 其他出库-有参考
            case "38":// UB 351
            case "311":// UB 101
            case "45":// UB 352
            case "51":// 采购退货-161
                break;
            case "16":// 其他入库-无参考
            case "25":// 其他出库-无参考
            case "26":// 无参考-201
            case "27":// 无参考-221
            case "32":// 301(无参考)
            case "34":// 311(无参考)
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
        return mRepository.transferCollectionData(transId, bizType, refType,
                userId, voucherDate, transToSapFlag, mExtraTransMap);
    }

    /**
     * 将单据数据转换为Results
     *
     * @param refData
     * @return
     */
    private ArrayList<ResultEntity> wrapper2Results(final ReferenceEntity refData, boolean isSave) {
        if (mRefDatas == null) {
            mRefDatas = new ArrayList<>();
        }
        if (isSave) {
            mRefDatas.add(refData);
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
            switch (result.businessType) {
                case "00":
                case "01":
                    break;
                case "11":// 采购入库-101
                case "12":// 采购入库-103
                    result.businessTypeDesc = "采购入库-103";
                    break;
                case "13":// 采购入库-105(非必检)
                case "19":// 委外入库
                case "19_ZJ":// 委外入库-组件
                case "110":// 采购入库-105(青海必检)
                case "21":// 销售出库
                case "23":// 委外发料
                case "24":// 其他出库-有参考
                case "38":// UB 351
                case "311":// UB 101
                case "45":// UB 352
                case "51":// 采购退货-161
                    break;
                case "16":// 其他入库-无参考
                case "25":// 其他出库-无参考
                case "26":// 无参考-201
                case "27":// 无参考-221
                case "32":// 301(无参考)
                case "34":// 311(无参考)
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
            if (!TextUtils.isEmpty(result.refType)) {
                switch (result.refType) {
                    case "0":
                        result.refTypeDesc = "采购订单";
                        break;
                }
            }
            results.add(result);
        }
        return results;
    }


}
