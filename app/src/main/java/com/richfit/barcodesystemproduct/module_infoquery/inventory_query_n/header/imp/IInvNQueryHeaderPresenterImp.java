package com.richfit.barcodesystemproduct.module_infoquery.inventory_query_n.header.imp;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.base.BasePresenter;
import com.richfit.barcodesystemproduct.module_infoquery.inventory_query_n.header.IInvNQueryHeaderPresenter;
import com.richfit.barcodesystemproduct.module_infoquery.inventory_query_n.header.IInvNQueryHeaderView;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.domain.bean.InvEntity;
import com.richfit.domain.bean.WorkEntity;
import java.util.ArrayList;
import javax.inject.Inject;

import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by monday on 2017/5/25.
 */

public class IInvNQueryHeaderPresenterImp extends BasePresenter<IInvNQueryHeaderView>
        implements IInvNQueryHeaderPresenter {

    IInvNQueryHeaderView mView;

    @Inject
    public IInvNQueryHeaderPresenterImp(@ContextLife("Activity") Context context) {
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
                        if (mView != null) {
                            mView.showWorks(works);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (mView != null) {
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
    public void getInvsByWorkId(String workId, int flag) {
        mView = getView();
        if (TextUtils.isEmpty(workId) && mView != null) {
            mView.loadInvsFail("请先选择接收工厂");
            return;
        }
        ResourceSubscriber<ArrayList<InvEntity>> subscriber = mRepository.getInvsByWorkId(workId, flag)
                .compose(TransformerHelper.io2main())
                .subscribeWith(new ResourceSubscriber<ArrayList<InvEntity>>() {
                    @Override
                    public void onNext(ArrayList<InvEntity> invs) {
                        if (mView != null) {
                            mView.showInvs(invs);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (mView != null) {
                            mView.loadInvsFail(t.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mView != null) {
                            mView.loadInvsComplete();
                        }
                    }
                });
        addSubscriber(subscriber);
    }

    @Override
    public void getInventoryInfo(String queryType, String workId, String invId, String workCode, String invCode, String storageNum, String materialNum, String materialId, String location, String batchFlag, String specialInvFlag, String specialInvNum, String invType, String deviceId) {

    }

}
