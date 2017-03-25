package com.richfit.barcodesystemproduct.module_delivery.qinghai_dsxs;

import android.support.annotation.NonNull;
import android.view.View;

import com.richfit.barcodesystemproduct.module_delivery.baseheader.BaseDSHeaderFragment;

/**
 * Created by monday on 2017/1/20.
 */

public class QingHaiDSXSHeaderFragment extends BaseDSHeaderFragment {

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

    @Override
    public void initView() {
        llCustomer.setVisibility(View.VISIBLE);
        super.initView();
    }

}
