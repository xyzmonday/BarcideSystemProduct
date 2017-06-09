package com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_header.imp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_header.IASHeaderPresenter;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_header.IASHeaderView;
import com.richfit.barcodesystemproduct.base.base_header.BaseHeaderPresenterImp;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.ReferenceEntity;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by monday on 2016/11/11.
 */

public class ASHeaderPresenterImp extends BaseHeaderPresenterImp<IASHeaderView>
        implements IASHeaderPresenter {

    IASHeaderView mView;

    @Inject
    public ASHeaderPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }


    @Override
    public void getReference(@NonNull String refNum, @NonNull String refType, @NonNull String bizType,
                             @NonNull String moveType, @NonNull String refLineId, @NonNull String userId) {
        mView = getView();

        if (TextUtils.isEmpty(refNum) && mView != null) {
            mView.getReferenceFail("单据号为空,请重新输入");
            return;
        }

        if ((TextUtils.isEmpty(refType) || "-1".equals(refType)) && mView != null) {
            mView.getReferenceFail("请选选择单据类型");
            return;
        }
        RxSubscriber<ReferenceEntity> subscriber =
                mRepository.getReference(refNum, refType, bizType, moveType, refLineId, userId)
                        .filter(refData -> refData != null && refData.billDetailList != null && refData.billDetailList.size() > 0)
                        .flatMap(refData -> Flowable.just(addBatchManagerStatus(refData)))
                        .map(refData -> addTreeInfo(refData))
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new RxSubscriber<ReferenceEntity>(mContext) {
                            @Override
                            public void _onNext(ReferenceEntity refData) {
                                if (mView != null) {
                                    mView.getReferenceSuccess(refData);
                                }
                            }

                            @Override
                            public void _onNetWorkConnectError(String message) {
                                if (mView != null) {
                                    mView.networkConnectError(Global.RETRY_LOAD_REFERENCE_ACTION);
                                }
                            }

                            @Override
                            public void _onCommonError(String message) {
                                if (mView != null) {
                                    mView.getReferenceFail(message);
                                }
                            }

                            @Override
                            public void _onServerError(String code, String message) {
                                if (mView != null) {
                                    mView.getReferenceFail(message);
                                }
                            }

                            @Override
                            public void _onComplete() {

                            }
                        });
        addSubscriber(subscriber);
    }


    @Override
    public void deleteCollectionData(String refNum, String transId, String refCodeId, String refType, String bizType,
                                     String userId, String companyCode) {
        mView = getView();
        ResourceSubscriber<String> subscriber = mRepository.deleteCollectionData(refNum, transId, refCodeId,
                refType, bizType, userId, companyCode)
                .compose(TransformerHelper.io2main())
                .subscribeWith(new RxSubscriber<String>(mContext) {
                    @Override
                    public void _onNext(String s) {

                    }

                    @Override
                    public void _onNetWorkConnectError(String message) {
                        if (mView != null) {
                            mView.networkConnectError(Global.RETRY_DELETE_TRANSFERED_CACHE_ACTION);
                        }
                    }


                    @Override
                    public void _onCommonError(String message) {
                        if (mView != null) {
                            mView.deleteCacheFail(message);
                        }
                    }

                    @Override
                    public void _onServerError(String code, String message) {
                        if (mView != null) {
                            mView.deleteCacheFail(message);
                        }
                    }

                    @Override
                    public void _onComplete() {
                        if (mView != null) {
                            mView.deleteCacheSuccess();
                        }
                    }
                });
        addSubscriber(subscriber);
    }

    @Override
    public void getTransferInfo(final ReferenceEntity refData, String refCodeId, String bizType, String refType) {
        mView = getView();
        ResourceSubscriber<ReferenceEntity> subscriber = mRepository.getTransferInfo("", refCodeId, bizType, refType, "", "", "", "", "")
                .zipWith(Flowable.just(refData), (cache, data) -> createHeaderByCache(cache, data))
                .compose(TransformerHelper.io2main())
                .subscribeWith(new ResourceSubscriber<ReferenceEntity>() {

                    @Override
                    public void onNext(ReferenceEntity data) {

                    }

                    @Override
                    public void onError(Throwable t) {
                        if (mView != null) {
                            mView.getTransferInfoFail(t.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mView != null) {
                            mView.bindCommonHeaderUI();
                        }
                    }
                });
        addSubscriber(subscriber);
    }

    /**
     * 将抬头的缓存写入单据中
     *
     * @param cache：缓存单据数据
     * @param data：原始单据数据
     * @return
     */
    private ReferenceEntity createHeaderByCache(ReferenceEntity cache, ReferenceEntity data) {
        data.voucherDate = cache.voucherDate;
        return data;
    }
}
