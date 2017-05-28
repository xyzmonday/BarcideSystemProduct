package com.richfit.barcodesystemproduct.module_infoquery.inventory_query_y.header.imp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.base.BasePresenter;
import com.richfit.barcodesystemproduct.module_infoquery.inventory_query_y.header.IInvYQueryHeaderPresenter;
import com.richfit.barcodesystemproduct.module_infoquery.inventory_query_y.header.IInvYQueryHeaderView;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.ReferenceEntity;

import javax.inject.Inject;

/**
 * Created by monday on 2017/5/25.
 */

public class InvYQueryHeaderPresenterImp extends BasePresenter<IInvYQueryHeaderView>
        implements IInvYQueryHeaderPresenter {

    IInvYQueryHeaderView mView;

    @Inject
    public InvYQueryHeaderPresenterImp(@ContextLife("Activity") Context context) {
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
                                if(mView != null) {
                                    mView.getReferenceComplete();
                                }
                            }
                        });
        addSubscriber(subscriber);
    }
}
