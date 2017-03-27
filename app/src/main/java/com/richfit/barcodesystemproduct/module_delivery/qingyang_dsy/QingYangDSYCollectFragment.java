package com.richfit.barcodesystemproduct.module_delivery.qingyang_dsy;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_ds_collect.BaseDSCollectFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_ds_collect.imp.DSCollectPresenterImp;

/**
 * Created by monday on 2017/1/17.
 */

public class QingYangDSYCollectFragment extends BaseDSCollectFragment<DSCollectPresenterImp> {

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected String getInvType() {
        return getString(R.string.invTypeNorm);
    }

    @Override
    protected String getInventoryQueryType() {
        return getString(R.string.inventoryQueryTypeSAPTotal);
    }

    @Override
    protected int getOrgFlag() {
        return getInteger(R.integer.orgNorm);
    }
}
