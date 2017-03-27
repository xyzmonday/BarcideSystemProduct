package com.richfit.barcodesystemproduct.module_delivery.qingyang_dsy;

import android.support.annotation.NonNull;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_ds_header.BaseDSHeaderFragment;

/**
 * Created by monday on 2017/1/17.
 */

public class QingYangDSYHeaderFragment extends BaseDSHeaderFragment {

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected String getBizType() {
        return mBizType;
    }

    @NonNull
    @Override
    protected String getMoveType() {
        return "1";
    }
}
