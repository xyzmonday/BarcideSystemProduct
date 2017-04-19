package com.richfit.barcodesystemproduct.module_approval.qinghai_ao.edit.imp;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.base.base_edit.BaseEditPresenterImp;
import com.richfit.barcodesystemproduct.module_approval.qinghai_ao.edit.IQingHaiAOEditPresenter;
import com.richfit.barcodesystemproduct.module_approval.qinghai_ao.edit.IQingHaiAOEditView;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.domain.bean.InvEntity;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by monday on 2017/3/1.
 */

public class QingHaiAOEditPresenterImp extends BaseEditPresenterImp<IQingHaiAOEditView>
        implements IQingHaiAOEditPresenter {

    @Inject
    public QingHaiAOEditPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void getInvsByWorkId(String workId,int flag) {
        mView = getView();
        if(TextUtils.isEmpty(workId) && mView != null) {
            mView.loadInvsFail("工厂Id为空");
            return;
        }
        ResourceSubscriber<ArrayList<InvEntity>> subscriber =
                mRepository.getInvsByWorkId(workId,flag)
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new ResourceSubscriber<ArrayList<InvEntity>>() {
                            @Override
                            public void onNext(ArrayList<InvEntity> invs) {
                                if(mView != null) {
                                    mView.showInvs(invs);
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                if(mView != null) {
                                    mView.loadInvsFail(t.getMessage());
                                }
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
        addSubscriber(subscriber);
    }
}
