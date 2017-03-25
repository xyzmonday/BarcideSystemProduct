package com.richfit.barcodesystemproduct.module_delivery.qinghai_dsww;

import android.support.annotation.NonNull;
import android.view.View;

import com.richfit.barcodesystemproduct.module_delivery.baseheader.BaseDSHeaderFragment;

/**
 * 青海委外出库抬头界面
 * Created by monday on 2017/3/5.
 */

public class QingHaiDSWWHeaderFragment extends BaseDSHeaderFragment {

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
