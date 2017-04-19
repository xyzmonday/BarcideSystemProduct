package com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_edit.imp;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_edit.IASEditPresenter;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_edit.IASEditView;
import com.richfit.barcodesystemproduct.base.base_edit.BaseEditPresenterImp;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.ResultEntity;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.subscribers.ResourceSubscriber;

/**
 * 在数据保存修改的数据之前检查上架仓位是否存在
 * Created by monday on 2016/11/19.
 */

public class ASEditPresenterImp extends BaseEditPresenterImp<IASEditView>
        implements IASEditPresenter {

    @Inject
    public ASEditPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void uploadCollectionDataSingle(ResultEntity result) {
        mView = getView();
        Flowable<String> flowable;
        if (!TextUtils.isEmpty(result.location) && "barcode".equalsIgnoreCase(result.location)) {
            //意味着上架
            flowable = mRepository.uploadCollectionDataSingle(result);
        } else {
            flowable = Flowable.concat(mRepository.getLocationInfo("04", result.workId, result.invId, "", result.location),
                    mRepository.uploadCollectionDataSingle(result));
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
