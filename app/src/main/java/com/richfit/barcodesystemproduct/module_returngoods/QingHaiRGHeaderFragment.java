package com.richfit.barcodesystemproduct.module_returngoods;

import android.support.annotation.NonNull;
import android.view.View;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_ds_header.BaseDSHeaderFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_ds_header.imp.DSHeaderPresenterImp;

/**
 * Created by monday on 2017/2/23.
 */

public class QingHaiRGHeaderFragment extends BaseDSHeaderFragment<DSHeaderPresenterImp> {


    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected void initView() {
        llSuppier.setVisibility(View.VISIBLE);
        llCreator.setVisibility(View.GONE);
        super.initView();
    }

    @Override
    public boolean isNeedShowFloatingButton() {
        return false;
    }

    @NonNull
    @Override
    protected String getMoveType() {
        return "2";
    }

}
