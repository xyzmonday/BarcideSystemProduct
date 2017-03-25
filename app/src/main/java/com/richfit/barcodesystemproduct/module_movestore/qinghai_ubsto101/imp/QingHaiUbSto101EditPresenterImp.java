package com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto101.imp;

import android.content.Context;

import com.richfit.common_lib.scope.ContextLife;
import com.richfit.barcodesystemproduct.module_acceptstore.baseedit.imp.ASEditPresenterImp;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.ResultEntity;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by monday on 2017/2/15.
 */

public class QingHaiUbSto101EditPresenterImp extends ASEditPresenterImp{

    @Inject
    public QingHaiUbSto101EditPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void uploadCollectionDataSingle(ResultEntity result) {
        mView = getView();
        ResourceSubscriber<String> subscriber =
                Flowable.concat(mRepository.getLocationInfo("04", result.workId, result.invId, result.location),
                        mRepository.uploadCollectionDataSingle(result))
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new RxSubscriber<String>(mContext) {
                            @Override
                            public void _onNext(String s) {

                            }

                            @Override
                            public void _onNetWorkConnectError(String message) {
                                if (mView != null) {
                                    mView.networkConnectError(Global.RETRY_EDIT_DATA_ACTION);
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
                                    mView.saveEditedDataSuccess("修改成功");
                                }
                            }
                        });
        addSubscriber(subscriber);
    }
}
