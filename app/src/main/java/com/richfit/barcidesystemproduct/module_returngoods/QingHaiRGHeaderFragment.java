package com.richfit.barcidesystemproduct.module_returngoods;

import android.support.annotation.NonNull;
import android.view.View;

import com.richfit.barcodesystemproduct.module_delivery.baseheader.BaseDSHeaderFragment;

/**
 * Created by monday on 2017/2/23.
 */

public class QingHaiRGHeaderFragment extends BaseDSHeaderFragment {


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
    protected String getBizType() {
        return "51";
    }

    @NonNull
    @Override
    protected String getMoveType() {
        return "2";
    }

}
