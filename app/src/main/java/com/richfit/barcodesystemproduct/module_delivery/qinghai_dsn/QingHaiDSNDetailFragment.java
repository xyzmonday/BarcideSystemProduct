package com.richfit.barcodesystemproduct.module_delivery.qinghai_dsn;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_dsn_detail.BaseDSNDetailFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_dsn_detail.imp.DSNDetailPresenterImp;

/**
 * Created by monday on 2017/3/27.
 */

public class QingHaiDSNDetailFragment extends BaseDSNDetailFragment<DSNDetailPresenterImp> {
    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }
}
