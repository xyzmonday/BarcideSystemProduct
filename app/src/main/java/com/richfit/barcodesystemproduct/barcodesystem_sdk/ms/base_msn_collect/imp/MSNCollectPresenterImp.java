package com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_collect.imp;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_collect.IMSNCollectPresenter;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_collect.IMSNCollectView;
import com.richfit.barcodesystemproduct.base.BasePresenter;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.L;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.InvEntity;
import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.LocationInfoEntity;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.ResultEntity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by monday on 2016/11/20.
 */

public class MSNCollectPresenterImp extends BasePresenter<IMSNCollectView>
        implements IMSNCollectPresenter {

    protected IMSNCollectView mView;

    @Inject
    public MSNCollectPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void getSendInvsByWorks(String workId, int flag) {
        mView = getView();
        ResourceSubscriber<ArrayList<InvEntity>> subscriber =
                mRepository.getInvsByWorkId(workId, flag)
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new ResourceSubscriber<ArrayList<InvEntity>>() {
                            @Override
                            public void onNext(ArrayList<InvEntity> list) {
                                if (mView != null) {
                                    mView.showSendInvs(list);
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                if (mView != null) {
                                    mView.loadSendInvsFail(t.getMessage());
                                }
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
        addSubscriber(subscriber);
    }

    @Override
    public void getTransferInfoSingle(String bizType, String materialNum, String userId, String workId,
                                      String invId, String recWorkId, String recInvId, String batchFlag,
                                      String refDoc, int refDocItem) {
        mView = getView();
        RxSubscriber<ReferenceEntity> subscriber = mRepository.getTransferInfoSingle("", "", bizType, "",
                workId, invId, recWorkId, recInvId, materialNum, batchFlag, "", refDoc, refDocItem, userId)
                .filter(refData -> refData != null && refData.billDetailList.size() > 0)
                .map(refData -> {
                    float totalQuantity = 0;
                    totalQuantity = 0;
                    List<RefDetailEntity> billDetailList = refData.billDetailList;
                    for (RefDetailEntity target : billDetailList) {
                        List<LocationInfoEntity> locationList = target.locationList;
                        if (locationList != null && locationList.size() > 0) {
                            for (LocationInfoEntity loc : locationList) {
                                totalQuantity += UiUtil.convertToFloat(loc.quantity, 0.0f);
                                L.e("totalQuantity = " + totalQuantity);
                            }
                            for (LocationInfoEntity loc : locationList) {
                                loc.quantity = String.valueOf(totalQuantity);
                            }
                        }
                    }
                    return refData;
                })
                .compose(TransformerHelper.io2main())
                .subscribeWith(new RxSubscriber<ReferenceEntity>(mContext, "正在获取缓存信息...") {
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
    public void checkLocation(String queryType, String workId, String invId, String batchFlag,
                              String location) {
        mView = getView();
        if (TextUtils.isEmpty(workId) && mView != null) {
            mView.checkLocationFail("工厂为空");
            return;
        }

        if (TextUtils.isEmpty(invId) && mView != null) {
            mView.checkLocationFail("库存地点为空");
            return;
        }

        ResourceSubscriber<String> subscriber =
                mRepository.getLocationInfo(queryType, workId, invId, "", location)
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new ResourceSubscriber<String>() {
                            @Override
                            public void onNext(String s) {

                            }

                            @Override
                            public void onError(Throwable t) {
                                if (mView != null) {
                                    mView.checkLocationFail(t.getMessage());
                                }
                            }

                            @Override
                            public void onComplete() {
                                if (mView != null) {
                                    mView.checkLocationSuccess(batchFlag, location);
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
                mRepository.getInventoryInfo(queryType, workId, invId, workCode, invCode, storageNum, materialNum,
                        materialId, "", "", batchFlag, location, specialInvFlag, specialInvNum, invType, deviceId)
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
                                if (mView != null) {
                                    mView.loadInventoryComplete();
                                }
                            }
                        });
        addSubscriber(subscriber);
    }

    @Override
    public void getInventoryInfoOnRecLocation(String queryType, String workId, String invId, String workCode,
                                              String invCode, String storageNum, String materialNum,
                                              String materialId, String location, String batchFlag,
                                              String specialInvFlag, String specialInvNum, String invType,
                                              String deviceId) {
        mView = getView();

        RxSubscriber<List<String>> subscriber =
                mRepository.getInventoryInfo(queryType, workId, invId, workCode, invCode, storageNum, materialNum,
                        materialId, "", "", batchFlag, location, specialInvFlag, specialInvNum, invType, deviceId)
                        .filter(list -> list != null && list.size() > 0)
                        .map(list -> convert2Strings(list))
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new RxSubscriber<List<String>>(mContext) {
                            @Override
                            public void _onNext(List<String> list) {
                                if (mView != null) {
                                    mView.showRecLocations(list);
                                }
                            }

                            @Override
                            public void _onNetWorkConnectError(String message) {
                                if (mView != null) {
                                    mView.networkConnectError(Global.RETRY_LOAD_REC_INVENTORY_ACTION);
                                }
                            }

                            @Override
                            public void _onCommonError(String message) {
                                if (mView != null) {
                                    mView.loadRecLocationsFail(message);
                                }
                            }

                            @Override
                            public void _onServerError(String code, String message) {
                                if (mView != null) {
                                    mView.loadRecLocationsFail(message);
                                }
                            }

                            @Override
                            public void _onComplete() {

                            }
                        });
        addSubscriber(subscriber);
    }

    private ArrayList<String> convert2Strings(List<InventoryEntity> list) {
        ArrayList<String> tmp = new ArrayList<>();
        for (InventoryEntity item : list) {
            tmp.add(item.location);
        }
        return tmp;
    }

    @Override
    public void uploadCollectionDataSingle(ResultEntity result) {
        mView = getView();
        ResourceSubscriber<String> subscriber =
                mRepository.uploadCollectionDataSingle(result)
                        .compose(TransformerHelper.io2main())
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
                                    mView.saveCollectedDataFail(message);
                                }
                            }

                            @Override
                            public void _onServerError(String code, String message) {
                                if (mView != null) {
                                    mView.saveCollectedDataFail(message);
                                }
                            }

                            @Override
                            public void _onComplete() {
                                if (mView != null) {
                                    mView.saveCollectedDataSuccess();
                                }
                            }
                        });
        addSubscriber(subscriber);
    }

    @Override
    public void checkWareHouseNum(boolean isOpenWM, String sendWorkId, String sendInvCode,
                                  String recWorkId, String recInvCode, int flag) {
        mView = getView();
        mRepository.checkWareHouseNum(sendWorkId, sendInvCode, recWorkId, recInvCode, flag)
                .compose(TransformerHelper.io2main())
                .subscribe(new ResourceSubscriber<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {

                    }

                    @Override
                    public void onError(Throwable t) {
                        if (mView != null) {
                            mView.checkWareHouseFail(t.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mView != null) {
                            mView.checkWareHouseSuccess();
                        }
                    }
                });
    }

    @Override
    public void getDeviceInfo(String deviceId) {
        mView = getView();

        mRepository.getDeviceInfo(deviceId)
                .compose(TransformerHelper.io2main())
                .subscribeWith(new ResourceSubscriber<ResultEntity>() {
                    @Override
                    public void onNext(ResultEntity result) {
                        if (mView != null) {
                            mView.getDeviceInfoSuccess(result);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (mView != null) {
                            mView.getDeviceInfoFail(t.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mView != null) {
                            mView.getDeviceInfoComplete();
                        }
                    }
                });
    }
}
