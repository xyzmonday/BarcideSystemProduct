package com.richfit.barcodesystemproduct.module_delivery.qinghai_dsxs;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_ds_collect.BaseDSCollectFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_ds_collect.imp.DSCollectPresenterImp;

/**
 * Created by monday on 2017/1/20.
 */

public class QingHaiDSXSCollectFragment extends BaseDSCollectFragment<DSCollectPresenterImp> {

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

    @Override
    protected int getOrgFlag() {
        return getInteger(R.integer.orgNorm);
    }
}
