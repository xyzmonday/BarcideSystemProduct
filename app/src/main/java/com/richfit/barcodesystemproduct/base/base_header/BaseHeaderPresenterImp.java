package com.richfit.barcodesystemproduct.base.base_header;

import android.content.Context;

import com.richfit.barcodesystemproduct.base.BasePresenter;
import com.richfit.common_lib.scope.ContextLife;

import javax.inject.Inject;

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
        super.onStart();
        mView = getView();
        mSimpleRxBus.toFlowable().subscribe(event -> {
            if (mView != null) {
                mView.clearAllUIAfterSubmitSuccess();
            }
        });

    }
}
