package com.richfit.barcodesystemproduct.module_local.loaddown;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.base.BasePresenter;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.MenuNode;
import com.richfit.domain.bean.ReferenceEntity;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by monday on 2017/3/21.
 */

public class LoadLocalRefDataPresenterImp extends BasePresenter<LoadLocalRefDataContract.View>
        implements LoadLocalRefDataContract.Presenter {

    LoadLocalRefDataContract.View mView;

    @Inject
    public LoadLocalRefDataPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    /**
     * 获取单据数据，获取成功后保存到本地数据
     *
     * @param refNum:单据号
     * @param refType:单据类型
     * @param bizType:业务类型
     * @param moveType:移动类型
     * @param refLineId
     * @param userId:用户Id
     */
    @Override
    public void getReferenceInfo(String refNum, String refType, String bizType,
                                 String moveType, String refLineId, String userId) {
        mView = getView();
        if (TextUtils.isEmpty(refNum) && mView != null) {
            mView.getReferenceInfoFail("单据号为空,请重新输入");
            return;
        }

        if ((TextUtils.isEmpty(refType) || "-1".equals(refType)) && mView != null) {
            mView.getReferenceInfoFail("请选选择单据类型");
            return;
        }
        RxSubscriber<ReferenceEntity> subscriber =
                mRepository.getReference(refNum, refType, bizType, moveType, refLineId, userId)
                        .filter(refData -> refData != null && refData.billDetailList != null && refData.billDetailList.size() > 0)
                        .doOnNext(refData -> mRepository.saveReferenceInfo(refData,bizType,refType))
                        .compose(TransformerHelper.io2main())
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new RxSubscriber<ReferenceEntity>(mContext, "正在获取单据数据...") {
                            @Override
                            public void _onNext(ReferenceEntity data) {
                                if (mView != null) {
                                    mView.getReferenceInfoSuccess(data);
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
                                    mView.getReferenceInfoFail(message);
                                }
                            }

                            @Override
                            public void _onServerError(String code, String message) {
                                if (mView != null) {
                                    mView.getReferenceInfoFail(message);
                                }
                            }

                            @Override
                            public void _onComplete() {
                                if (mView != null) {
                                    mView.getReferenceInfoComplete();
                                }
                            }
                        });
        addSubscriber(subscriber);
    }

    @Override
    public void readMenuInfo(String loginId, int mode) {
        mView = getView();
        ResourceSubscriber<ArrayList<MenuNode>> subscriber = mRepository.getMenuInfo(loginId, mode)
                .compose(TransformerHelper.io2main())
                .subscribeWith(new ResourceSubscriber<ArrayList<MenuNode>>() {
                    @Override
                    public void onNext(ArrayList<MenuNode> menuNodes) {
                        if (mView != null) {
                            mView.readMenuInfoSuccess(menuNodes);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (mView != null) {
                            mView.readMenuInfoFail(t.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        addSubscriber(subscriber);
    }
}
