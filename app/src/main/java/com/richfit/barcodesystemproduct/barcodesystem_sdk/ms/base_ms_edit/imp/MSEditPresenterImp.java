package com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_ms_edit.imp;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_ms_edit.IMSEditPresenter;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_ms_edit.IMSEditView;
import com.richfit.barcodesystemproduct.base.base_edit.BaseEditPresenterImp;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.RefDetailEntity;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by monday on 2017/2/13.
 */

public class MSEditPresenterImp extends BaseEditPresenterImp<IMSEditView>
        implements IMSEditPresenter {

    @Inject
    public MSEditPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void getInventoryInfo(String queryType, String workId, String invId, String workCode, String invCode, String storageNum,
                                 String materialNum, String materialId, String location, String batchFlag,
                                 String specialInvFlag, String specialInvNum, String invType, String deviceId) {
        mView = getView();
        RxSubscriber<List<InventoryEntity>> subscriber = null;
        if ("04".equals(queryType)) {
            subscriber = mRepository.getStorageNum(workId, workCode, invId, invCode)
                    .filter(num -> !TextUtils.isEmpty(num))
                    .flatMap(num -> mRepository.getInventoryInfo(queryType, workId, invId,
                            workCode, invCode, num, materialNum, materialId, "", "", batchFlag, location, specialInvFlag, storageNum, invType, deviceId))
                    .compose(TransformerHelper.io2main())
                    .subscribeWith(new InventorySubscriber(mContext, "正在获取库存"));

        } else {
            subscriber = mRepository.getInventoryInfo(queryType, workId, invId,
                    workCode, invCode, storageNum, materialNum, materialId, "", "", batchFlag, location,
                    specialInvFlag, storageNum, invType, deviceId)
                    .compose(TransformerHelper.io2main())
                    .subscribeWith(new InventorySubscriber(mContext, "正在获取库存"));
        }
        addSubscriber(subscriber);
    }

    class InventorySubscriber extends RxSubscriber<List<InventoryEntity>> {

        public InventorySubscriber(Context context, String msg) {
            super(context, msg);
        }

        @Override
        public void _onNext(List<InventoryEntity> list) {
            if (mView != null) {
                mView.showInventory(list);
            }
        }

        @Override
        public void _onNetWorkConnectError(String message) {
            if (mView != null) {
                mView.networkConnectError(Global.RETRY_LOAD_INVENTORY_ACTION);
            }
        }

        @Override
        public void _onCommonError(String message) {
            if (mView != null) {
                mView.loadInventoryFail(message);
            }
        }

        @Override
        public void _onServerError(String code, String message) {
            if (mView != null) {
                mView.loadInventoryFail(message);
            }
        }

        @Override
        public void _onComplete() {

        }
    }

    @Override
    public void getTransferInfoSingle(String refCodeId, String refType, String bizType, String refLineId,
                                      String batchFlag, String location, String refDoc, int refDocItem, String userId) {
        mView = getView();
        RxSubscriber<RefDetailEntity> subscriber =
                mRepository.getTransferInfoSingle(refCodeId, refType, bizType, refLineId,
                        "", "", "", "", "", batchFlag, location, refDoc, refDocItem, userId)
                        .filter(refData -> refData != null && refData.billDetailList != null)
                        .flatMap(refData -> getMatchedLineData(refLineId, refData))
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new RxSubscriber<RefDetailEntity>(mContext) {
                            @Override
                            public void _onNext(RefDetailEntity cache) {
                                //获取缓存数据
                                if (mView != null) {
                                    mView.onBindCache(cache, batchFlag, location);
                                }
                            }

                            @Override
                            public void _onNetWorkConnectError(String message) {
                                if (mView != null) {
                                    mView.networkConnectError(Global.RETRY_LOAD_SINGLE_CACHE_ACTION);
                                }
                            }

                            @Override
                            public void _onCommonError(String message) {
                                if (mView != null) {
                                    mView.loadCacheFail(message);
                                }
                            }

                            @Override
                            public void _onServerError(String code, String message) {
                                if (mView != null) {
                                    mView.loadCacheFail(message);
                                }
                            }

                            @Override
                            public void _onComplete() {
                            }
                        });
        addSubscriber(subscriber);
    }
}
