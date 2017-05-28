package com.richfit.barcodesystemproduct.module_delivery.qinghai_dsxs;

import android.support.annotation.NonNull;
import android.view.View;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_ds_header.BaseDSHeaderFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_ds_header.imp.DSHeaderPresenterImp;

/**
 * Created by monday on 2017/1/20.
 */

public class QingHaiDSXSHeaderFragment extends BaseDSHeaderFragment<DSHeaderPresenterImp> {

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    public void initView() {
        llCustomer.setVisibility(View.VISIBLE);
        super.initView();
    }


    @NonNull
    @Override
    protected String getMoveType() {
        return "1";
    }



}
