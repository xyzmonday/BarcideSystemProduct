package com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_edit.imp;

import android.content.Context;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_edit.IMSNEditPresenter;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_edit.IMSNEditView;
import com.richfit.barcodesystemproduct.base.base_edit.BaseEditPresenterImp;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.ResultEntity;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by monday on 2016/11/22.
 */

public class MSNEditPresenterImp extends BaseEditPresenterImp<IMSNEditView>
        implements IMSNEditPresenter {

    @Inject
    public MSNEditPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }


    @Override
    public void getTransferInfoSingle(String bizType, String materialNum, String userId, String workId, String invId, String recWorkId,
                                      String recInvId, String batchFlag, String refDoc, int refDocIem) {
        mView = getView();
        RxSubscriber<ReferenceEntity> subscriber =
                mRepository.getTransferInfoSingle("", "", bizType, "",
                        workId, invId, recWorkId, recInvId, materialNum, batchFlag, "", refDoc, refDocIem, userId)
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new RxSubscriber<ReferenceEntity>(mContext) {
                            @Override
                            public void _onNext(ReferenceEntity refData) {
                                if (mView != null) {
                                    mView.onBindCommonUI(refData, batchFlag);
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
                                    mView.loadTransferSingleInfoFail(message);
                                }
                            }

                            @Override
                            public void _onServerError(String code, String message) {
                                if (mView != null) {
                                    mView.loadTransferSingleInfoFail(message);
                                }
                            }

                            @Override
                            public void _onComplete() {
                                if (mView != null) {
                                    mView.loadTransferSingleInfoComplete();
                                }
                            }
                        });
        addSubscriber(subscriber);
    }

    @Override
    public void getInventoryInfo(String queryType, String workId, String invId, String workCode,
                                 String invCode, String storageNum, String materialNum, String materialId, String location, String batchFlag,
                                 String specialInvFlag, String specialInvNum, String invType, String deviceId) {
        mView = getView();

        RxSubscriber<List<InventoryEntity>> subscriber =
                mRepository.getInventoryInfo(queryType, workId, invId, workCode, invCode, storageNum,
                        materialNum, materialId, "", "", batchFlag, location, specialInvFlag, specialInvNum, invType, deviceId)
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new RxSubscriber<List<InventoryEntity>>(mContext) {
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
                        });
        addSubscriber(subscriber);
    }

    @Override
    public void uploadCollectionDataSingle(ResultEntity result) {
        mView = getView();
        Flowable<String> flowable;
        //这里-1表示离线模式
        if (isLocal()) {
            flowable = Flowable.concat(mRepository.getLocationInfo("04", result.workId, result.invId, "", result.location),
                    mRepository.uploadCollectionDataSingle(result));
        } else {
            flowable = mRepository.uploadCollectionDataSingle(result);
        }
        ResourceSubscriber<String> subscriber =
                flowable.compose(TransformerHelper.io2main())
                        .subscribeWith(new RxSubscriber<String>(mContext) {
                            @Override
                            public void _onNext(String s) {

                            }

                            @Override
                            public void _onNetWorkConnectError(String message) {
                                if (mView != null) {
                                    mView.networkConnectError(Global.RETRY_SAVE_COLLECTION_DATA_ACTION);
                                }
                            }

                            @Override
                            public void _onCommonError(String message) {
                                if (mView != null) {
                                    mView.saveEditedDataFail(message);
                                }
                            }

                            @Override
                            public void _onServerError(String code, String message) {
                                if (mView != null) {
                                    mView.saveEditedDataFail(message);
                                }
                            }

                            @Override
                            public void _onComplete() {
                                if (mView != null) {
                                    mView.saveEditedDataSuccess("修改成功!");
                                }
                           }
                        });
        addSubscriber(subscriber);
    }
}
