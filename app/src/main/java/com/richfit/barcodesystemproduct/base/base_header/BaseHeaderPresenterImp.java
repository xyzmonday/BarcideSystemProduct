package com.richfit.barcodesystemproduct.base.base_header;

import android.content.Context;

import com.richfit.barcodesystemproduct.base.BasePresenter;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.L;
import com.richfit.domain.bean.ResultEntity;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

/**
 * Created by monday on 2017/3/18.
 */

public class BaseHeaderPresenterImp<V extends IBaseHeaderView> extends BasePresenter<V>
        implements IBaseHeaderPresenter<V> {

    protected V mView;


    @Inject
    public BaseHeaderPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    protected void onStart() {
        mView = getView();
        mSimpleRxBus.register(Boolean.class)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (mView != null) {
                            mView.clearAllUIAfterSubmitSuccess();
                        }
                    }
                });
    }

    @Override
    public void uploadEditedHeadData(ResultEntity resultEntity) {
        mView = getView();
        if (resultEntity == null) {
            mView.uploadEditedHeadDataFail("请先获取修改的数据");
            return;
        }
        mRepository.uploadEditedHeadData(resultEntity)
                .compose(TransformerHelper.io2main())
                .subscribeWith(new RxSubscriber<String>(mContext, "正在保存抬头修改的数据") {
                    @Override
                    public void _onNext(String s) {

                    }

                    @Override
                    public void _onNetWorkConnectError(String message) {
                        if (mView != null) {
                            mView.networkConnectError(Global.RETRY_SAVE_COLLECTION_DATA_ACTION);
                        }
                    }

                    @Override
                    public void _onCommonError(String message) {
                        if (mView != null) {
                            mView.uploadEditedHeadDataFail(message);
                        }
                    }

                    @Override
                    public void _onServerError(String code, String message) {
                        if (mView != null) {
                            mView.uploadEditedHeadDataFail(message);
                        }
                    }

                    @Override
                    public void _onComplete() {
                        if (mView != null) {
                            mView.uploadEditedHeadComplete();
                        }
                    }
                });
    }
}
