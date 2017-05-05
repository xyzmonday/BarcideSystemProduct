package com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_edit.imp;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_edit.IMSNEditPresenter;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_edit.IMSNEditView;
import com.richfit.barcodesystemproduct.base.base_edit.BaseEditPresenterImp;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.LocationInfoEntity;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.ResultEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
                        .map(refData -> calcTotalQuantity(refData))
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

    private ReferenceEntity calcTotalQuantity(ReferenceEntity refData) {
        List<RefDetailEntity> billDetailList = refData.billDetailList;
        for (RefDetailEntity target : billDetailList) {
            HashSet<String> sendLocationSet = new HashSet<>();
            List<LocationInfoEntity> locationList = target.locationList;
            if (locationList == null || locationList.size() == 0)
                return refData;
            //保存所有不重复的发出仓位
            if (locationList != null && locationList.size() > 0) {
                for (LocationInfoEntity loc : locationList) {
                    if (!TextUtils.isEmpty(loc.location)) {
                        sendLocationSet.add(loc.location);
                    }
                }
            }
            //计算发出仓位的所有接收仓位的数量作为该发出仓位的仓位数量
            HashMap<String, String> locQuantityMap = new HashMap<>();
            for (String sendLocation : sendLocationSet) {
                //将缓存中相同的发出仓位的所有接收仓位的仓位数量累加
                float totalQuantity = 0;
                for (LocationInfoEntity loc : locationList) {
                    if (sendLocation.equalsIgnoreCase(loc.location)) {
                        totalQuantity += UiUtil.convertToFloat(loc.quantity, 0.0F);
                    }
                }
                locQuantityMap.put(sendLocation, String.valueOf(totalQuantity));
            }
            //改变所有发出仓位的quantity
            for (LocationInfoEntity loc : locationList) {
                loc.quantity = locQuantityMap.get(loc.location);
            }

        }
        return refData;
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
