package com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_header.imp;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.base.base_header.BaseHeaderPresenterImp;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_header.IMSNHeaderPresenter;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_header.IMSNHeaderView;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.domain.bean.InvEntity;
import com.richfit.domain.bean.WorkEntity;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by monday on 2016/11/20.
 */

public class MSNHeaderPresenterImp extends BaseHeaderPresenterImp<IMSNHeaderView>
        implements IMSNHeaderPresenter {


    @Inject
    public MSNHeaderPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void getWorks(int flag) {
        mView = getView();
        ResourceSubscriber<ArrayList<WorkEntity>> subscriber = mRepository.getWorks(flag)
                .compose(TransformerHelper.io2main())
                .subscribeWith(new ResourceSubscriber<ArrayList<WorkEntity>>() {
                    @Override
                    public void onNext(ArrayList<WorkEntity> works) {
                        if(mView != null) {
                            mView.showWorks(works);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if(mView != null) {
                            mView.loadWorksFail(t.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        addSubscriber(subscriber);
    }

    @Override
    public void getRecInvsByWorkId(String workId,int flag) {
        mView = getView();
        if(TextUtils.isEmpty(workId) && mView != null) {
            mView.loadRecInvsFail("请先选择接收工厂");
            return;
        }
        ResourceSubscriber<ArrayList<InvEntity>> subscriber = mRepository.getInvsByWorkId(workId,flag)
                .compose(TransformerHelper.io2main())
                .subscribeWith(new ResourceSubscriber<ArrayList<InvEntity>>() {
                    @Override
                    public void onNext(ArrayList<InvEntity> invs) {
                        if(mView != null) {
                            mView.showRecInvs(invs);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if(mView != null) {
                            mView.loadRecInvsFail(t.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        addSubscriber(subscriber);
    }

    @Override
    public void getSendInvsByWorkId(String workId,int flag) {
        mView = getView();
        if(TextUtils.isEmpty(workId) && mView != null) {
            mView.loadSendInvsFail("请先选择发出工厂");
            return;
        }
        ResourceSubscriber<ArrayList<InvEntity>> subscriber =
                mRepository.getInvsByWorkId(workId,flag)
                .compose(TransformerHelper.io2main())
                .subscribeWith(new ResourceSubscriber<ArrayList<InvEntity>>() {
                    @Override
                    public void onNext(ArrayList<InvEntity> invs) {
                        if(mView != null) {
                            mView.showSendInvs(invs);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if(mView != null) {
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
    public void deleteCollectionData(String refType,String bizType, String userId,
                                     String companyCode) {
        mView = getView();
        RxSubscriber<String> subscriber = mRepository.deleteCollectionData("","","",refType,bizType,
                userId,companyCode)
                .compose(TransformerHelper.io2main())
                .subscribeWith(new RxSubscriber<String>(mContext, "正在删除缓存...") {
                    @Override
                    public void _onNext(String message) {
                        if (mView != null) {
                            mView.deleteCacheSuccess(message);
                        }
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

                    }
                });
        addSubscriber(subscriber);
    }
}
