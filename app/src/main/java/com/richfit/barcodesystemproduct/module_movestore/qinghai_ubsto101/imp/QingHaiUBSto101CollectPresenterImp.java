package com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto101.imp;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_collect.imp.ASCollectPresenterImp;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;

import javax.inject.Inject;

import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by monday on 2017/2/15.
 */

public class QingHaiUBSto101CollectPresenterImp extends ASCollectPresenterImp {

    @Inject
    public QingHaiUBSto101CollectPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void checkLocation(String queryType, String workId, String invId, String batchFlag, String location) {
        mView = getView();
        if (TextUtils.isEmpty(workId) && mView != null) {
            mView.checkLocationFail("工厂为空");
            return;
        }

        if (TextUtils.isEmpty(invId) && mView != null) {
            mView.checkLocationFail("库存地点为空");
            return;
        }

        ResourceSubscriber<String> subscriber = mRepository.getLocationInfo(queryType, workId, invId,"", location)
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
}
