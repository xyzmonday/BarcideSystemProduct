package com.richfit.barcodesystemproduct.module_delivery.qinghai_dsn;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_dsn_collect.BaseDSNCollectFragment;

/**
 * Created by monday on 2017/3/27.
 */

public class QingHaiDSNCollectFragment extends BaseDSNCollectFragment {
    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected String getInvType() {
        return "01";
    }

    @Override
    protected String getInventoryQueryType() {
        return getString(R.string.inventoryQueryTypeSAPLocation);
    }
}
