package com.richfit.barcodesystemproduct.module_delivery.qinghai_dsn;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_dsn_header.BaseDSNHeaderFragment;

/**
 * Created by monday on 2017/3/27.
 */

public class QingHaiDSNHeaderFragment extends BaseDSNHeaderFragment {
    @Override
    protected int getOrgFlag() {
        return 0;
    }

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }
}
