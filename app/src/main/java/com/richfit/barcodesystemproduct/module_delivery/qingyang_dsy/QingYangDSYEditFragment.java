package com.richfit.barcodesystemproduct.module_delivery.qingyang_dsy;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_ds_edit.BaseDSEditFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_ds_edit.imp.DSEditPresenterImp;

/**
 * Created by monday on 2017/1/17.
 */

public class QingYangDSYEditFragment extends BaseDSEditFragment<DSEditPresenterImp> {
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
}
