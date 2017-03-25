package com.richfit.barcodesystemproduct.module_delivery.qinghai_dsn.detail.imp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.base.base_detail.BaseDetailPresenterImp;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.barcodesystemproduct.module.edit.EditActivity;
import com.richfit.barcodesystemproduct.module_delivery.qinghai_dsn.detail.IQingHaiDSNDetailPresenter;
import com.richfit.barcodesystemproduct.module_delivery.qinghai_dsn.detail.IQingHaiDSNDetailView;
import com.richfit.common_lib.rxutils.RetryWhenNetworkException;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.LocationInfoEntity;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by monday on 2017/2/23.
 */

public class QingHaiDSNDetailPresenterImp extends BaseDetailPresenterImp<IQingHaiDSNDetailView>
        implements IQingHaiDSNDetailPresenter {


    @Inject
    public QingHaiDSNDetailPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    /**
     * 注意获取无参考的整单缓存时，单据数据refData为null
     * @param refData：抬头界面获取的单据数据
     * @param refCodeId：单据id
     * @param bizType:业务类型
     * @param refType：单据类型
     * @param userId
     * @param workId
     * @param invId
     * @param recWorkId
     * @param recInvId
     */
    @Override
    public void getTransferInfo(ReferenceEntity refData,String refCodeId, String bizType, String refType, String userId, String workId,
                                String invId, String recWorkId, String recInvId) {
        mView = getView();
        ResourceSubscriber<ArrayList<RefDetailEntity>> subscriber =
                mRepository.getTransferInfo("", refCodeId, bizType, refType, userId, workId, invId,
                        recWorkId, recInvId)
                        .map(data -> trans2Detail(data))
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new ResourceSubscriber<ArrayList<RefDetailEntity>>() {
                            @Override
                            public void onNext(ArrayList<RefDetailEntity> list) {
                                if (mView != null) {
                                    mView.showNodes(list);
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                if (mView != null) {
                                    mView.setRefreshing(false, t.getMessage());
                                }
                            }

                            @Override
                            public void onComplete() {
                                if (mView != null) {
                                    mView.setRefreshing(true, "获取明细缓存成功");
                                }
                            }
                        });
        addSubscriber(subscriber);
    }

    @Override
    public void deleteNode(String lineDeleteFlag, String transId, String transLineId, String locationId,
                           String refType, String bizType, int position, String companyCode) {
        RxSubscriber<String> subscriber = mRepository.deleteCollectionDataSingle(lineDeleteFlag, transId, transLineId,
                locationId, refType, bizType, "", "", position, companyCode)
                .compose(TransformerHelper.io2main())
                .subscribeWith(new RxSubscriber<String>(mContext) {
                    @Override
                    public void _onNext(String s) {

                    }

                    @Override
                    public void _onNetWorkConnectError(String message) {

                    }

                    @Override
                    public void _onCommonError(String message) {
                        if (mView != null) {
                            mView.deleteNodeFail(message);
                        }
                    }

                    @Override
                    public void _onServerError(String code, String message) {
                        if (mView != null) {
                            mView.deleteNodeFail(message);
                        }
                    }

                    @Override
                    public void _onComplete() {
                        if (mView != null) {
                            mView.deleteNodeSuccess(position);
                        }
                    }
                });
        addSubscriber(subscriber);
    }


    @Override
    public void editNode(ArrayList<String> sendLocations, ArrayList<String> recLocations,
                         ReferenceEntity refData, RefDetailEntity node,
                         String companyCode, String bizType, String refType, String subFunName, int position) {

        Intent intent = new Intent(mContext, EditActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString(Global.EXTRA_LOCATION_ID_KEY, node.locationId);
        //物料
        bundle.putString(Global.EXTRA_MATERIAL_NUM_KEY, node.materialNum);

        bundle.putString(Global.EXTRA_MATERIAL_ID_KEY, node.materialId);

        //入库子菜单类型
        bundle.putString(Global.EXTRA_BIZ_TYPE_KEY, bizType);
        bundle.putString(Global.EXTRA_REF_TYPE_KEY, refType);

        //入库的子菜单的名称
        bundle.putString(Global.EXTRA_TITLE_KEY, subFunName + "-明细修改");

        //移库数量
        bundle.putString(Global.EXTRA_QUANTITY_KEY, node.quantity);

        //库存地点
        bundle.putString(Global.EXTRA_INV_CODE_KEY, node.invCode);
        bundle.putString(Global.EXTRA_INV_ID_KEY, node.invId);

        //下架仓位
        bundle.putString(Global.EXTRA_LOCATION_KEY, node.location);

        //批次
        bundle.putString(Global.EXTRA_BATCH_FLAG_KEY, node.batchFlag);

        //额外字段的数据
        bundle.putSerializable(Global.LOCATION_EXTRA_MAP_KEY, (Serializable) node.mapExt);

        //下架仓位集合
        bundle.putStringArrayList(Global.EXTRA_LOCATION_LIST_KEY, sendLocations);

        intent.putExtras(bundle);

        Activity activity = (Activity) mContext;
        activity.startActivity(intent);
    }

    @Override
    public void submitData2BarcodeSystem(String transId, String bizType, String refType, String userId, String voucherDate,
                                         Map<String, Object> flagMap, Map<String, Object> extraHeaderMap) {
        mView = getView();
        RxSubscriber<String> subscriber = Flowable.concat(mRepository.uploadCollectionData("", transId, bizType, refType, -1, voucherDate, "", userId),
                mRepository.transferCollectionData(transId, bizType, refType, userId, voucherDate, flagMap, extraHeaderMap))
                .retryWhen(new RetryWhenNetworkException(3, 3000))
                .doOnError(str -> SPrefUtil.saveData(bizType + refType, "0"))
                .doOnComplete(() -> SPrefUtil.saveData(bizType + refType, "1"))
                .compose(TransformerHelper.io2main())
                .subscribeWith(new RxSubscriber<String>(mContext, "正在过账...") {
                    @Override
                    public void _onNext(String message) {
                        if (mView != null) {
                            mView.showTransferedVisa(message);
                        }
                    }

                    @Override
                    public void _onNetWorkConnectError(String message) {
                        if (mView != null) {
                            mView.networkConnectError(Global.RETRY_TRANSFER_DATA_ACTION);
                        }
                    }

                    @Override
                    public void _onCommonError(String message) {
                        if (mView != null) {
                            mView.submitBarcodeSystemFail(message);
                        }
                    }

                    @Override
                    public void _onServerError(String code, String message) {
                        if (mView != null) {
                            mView.submitBarcodeSystemFail(message);
                        }
                    }

                    @Override
                    public void _onComplete() {
                        if (mView != null) {
                            mView.submitBarcodeSystemSuccess();
                        }
                    }
                });
        addSubscriber(subscriber);
    }

    @Override
    public void submitData2SAP(String transId, String bizType, String refType, String userId,
                               String voucherDate, Map<String, Object> flagMap, Map<String, Object> extraHeaderMap) {
        mView = getView();
        RxSubscriber<String> subscriber = mRepository.transferCollectionData(transId, bizType, refType, userId, voucherDate, flagMap, extraHeaderMap)
                .retryWhen(new RetryWhenNetworkException(3, 3000))
                .doOnComplete(() -> SPrefUtil.saveData(bizType + refType, "0"))
                .compose(TransformerHelper.io2main())
                .subscribeWith(new RxSubscriber<String>(mContext, "正在上传数据...") {
                    @Override
                    public void _onNext(String message) {
                        if (mView != null) {
                            mView.showInspectionNum(message);
                        }
                    }

                    @Override
                    public void _onNetWorkConnectError(String message) {
                        if (mView != null) {
                            mView.networkConnectError(Global.RETRY_UPLOAD_DATA_ACTION);
                        }
                    }

                    @Override
                    public void _onCommonError(String message) {
                        if (mView != null && !TextUtils.isEmpty(message)) {
                            mView.submitSAPFail(message.split("_"));
                        }
                    }

                    @Override
                    public void _onServerError(String code, String message) {
                        if (mView != null) {
                            mView.submitSAPFail(new String[]{message});
                        }
                    }

                    @Override
                    public void _onComplete() {
                        if (mView != null) {
                            mView.submitSAPSuccess();
                        }
                    }
                });
        addSubscriber(subscriber);
    }

    @Override
    public void turnOwnSupplies(String transId, String bizType, String refType, String userId,
                                String voucherDate, Map<String, Object> flagMap,
                                Map<String, Object> extraHeaderMap, int submitFlag) {
        mView = getView();
        RxSubscriber<String> subscriber = mRepository.transferCollectionData(transId, bizType, refType, Global.USER_ID, voucherDate, flagMap, extraHeaderMap)
                .compose(TransformerHelper.io2main())
                .subscribeWith(new RxSubscriber<String>(mContext, "正在上传数据...") {
                    @Override
                    public void _onNext(String s) {

                    }

                    @Override
                    public void _onNetWorkConnectError(String message) {
                        if (mView != null) {
                            mView.turnOwnSuppliesFail(message);
                        }
                    }

                    @Override
                    public void _onCommonError(String message) {
                        if (mView != null) {
                            mView.turnOwnSuppliesFail(message);
                        }
                    }

                    @Override
                    public void _onServerError(String code, String message) {
                        if (mView != null) {
                            mView.turnOwnSuppliesFail(message);
                        }
                    }

                    @Override
                    public void _onComplete() {
                        if (mView != null) {
                            mView.turnOwnSuppliesSuccess();
                        }
                    }
                });
        addSubscriber(subscriber);
    }



    /**
     * 将服务器返回的三层结构的单据数据，转换成父节点的明细数据
     *
     * @return
     */
    private ArrayList<RefDetailEntity> trans2Detail(ReferenceEntity refData) {
        ArrayList<RefDetailEntity> datas = new ArrayList<>();
        List<RefDetailEntity> billDetailList = refData.billDetailList;
        for (RefDetailEntity target : billDetailList) {
            List<LocationInfoEntity> locationList = target.locationList;
            if (locationList != null && locationList.size() > 0) {
                for (LocationInfoEntity loc : locationList) {
                    RefDetailEntity data = new RefDetailEntity();
                    //父节点的数据
                    data.materialId = target.materialId;
                    data.materialNum = target.materialNum;
                    data.materialDesc = target.materialDesc;
                    data.materialGroup = target.materialGroup;
                    data.unit = target.unit;
                    data.price = target.price;
                    data.totalQuantity = target.totalQuantity;
                    data.transLineId = target.transLineId;
                    data.invId = target.invId;
                    data.invCode = target.invCode;
                    //子节点的数据
                    data.transId = loc.transId;
                    data.transLineId = loc.transLineId;
                    data.location = loc.location;
                    data.batchFlag = loc.batchFlag;
                    data.quantity = loc.quantity;
                    data.recLocation = loc.recLocation;
                    data.recBatchFlag = loc.recBatchFlag;
                    data.specialInvFlag = loc.specialInvFlag;
                    data.specialInvNum = loc.specialInvNum;
                    data.locationId = loc.id;

                    //额外字段信息
                    data.mapExt = UiUtil.copyMap(target.mapExt, loc.mapExt);
                    datas.add(data);
                }
            }
        }
        return datas;
    }

}
