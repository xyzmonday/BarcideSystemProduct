package com.richfit.barcidesystemproduct.module_approval.qinghai_ao.header.imp;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.base.base_header.BaseHeaderPresenterImp;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.barcodesystemproduct.module_approval.qinghai_ao.header.IQingHaiAOHeaderPresenter;
import com.richfit.barcodesystemproduct.module_approval.qinghai_ao.header.IQingHaiAOHeaderView;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.utils.FileUtil;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.ReferenceEntity;

import javax.inject.Inject;

/**
 * Created by monday on 2017/2/28.
 */

public class QingHaiAOHeaderPresenterImp extends BaseHeaderPresenterImp<IQingHaiAOHeaderView>
        implements IQingHaiAOHeaderPresenter {

    @Inject
    public QingHaiAOHeaderPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void getReference(String refNum, String refType, String bizType,
                             String moveType, String userId) {
        mView = getView();

        if (TextUtils.isEmpty(refNum) && mView != null) {
            mView.getReferenceFail("单据号为空,请重新输入");
            return;
        }

        if ((TextUtils.isEmpty(refType) || "-1".equals(refType)) && mView != null) {
            mView.getReferenceFail("请选选择单据类型");
            return;
        }

        RxSubscriber<ReferenceEntity> subscriber = mRepository.getReference(refNum, refType, bizType, moveType,"", userId)
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

                    }
                });
        addSubscriber(subscriber);
    }

    /**
     * 删除整单验收缓存，注意服务端已经删除缓存的图片
     *
     * @param refNum:单号
     * @param transId：缓存抬头id
     * @param refCodeId：单据抬头id
     * @param bizType：业务类型
     * @param userId：用户id
     */
    @Override
    public void deleteCollectionData(String refNum, String transId, String refCodeId,
                                     String refType, String bizType, String userId,
                                     String companyCode) {
        mView = getView();

        RxSubscriber<String> subscriber =
                mRepository.deleteCollectionData(refNum, transId, refCodeId, refType, bizType, userId,
                        companyCode)
                        .doOnComplete(() -> mRepository.deleteInspectionImages(refNum, refCodeId, false))
                        .doOnComplete(() -> FileUtil.deleteDir(FileUtil.getImageCacheDir(mContext.getApplicationContext(), refNum, false)))
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new RxSubscriber<String>(mContext, "正在删除...") {
                            @Override
                            public void _onNext(String s) {

                            }


                            @Override
                            public void _onNetWorkConnectError(String message) {

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
}
