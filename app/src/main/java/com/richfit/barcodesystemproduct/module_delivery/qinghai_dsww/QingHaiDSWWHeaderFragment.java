package com.richfit.barcodesystemproduct.module_delivery.qinghai_dsww;

import android.support.annotation.NonNull;
import android.view.View;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_ds_header.BaseDSHeaderFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_ds_header.imp.DSHeaderPresenterImp;

/**
 * 青海委外出库抬头界面
 * Created by monday on 2017/3/5.
 */

public class QingHaiDSWWHeaderFragment extends BaseDSHeaderFragment<DSHeaderPresenterImp> {

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected void initView() {
        llSuppier.setVisibility(View.VISIBLE);
        super.initView();
    }

    @NonNull
    @Override
    protected String getBizType() {
        return "23";
    }

    @NonNull
    @Override
    protected String getMoveType() {
        return "2";
    }


}
