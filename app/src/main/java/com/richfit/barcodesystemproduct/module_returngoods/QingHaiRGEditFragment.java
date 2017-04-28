package com.richfit.barcodesystemproduct.module_returngoods;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_ds_edit.BaseDSEditFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_ds_edit.imp.DSEditPresenterImp;

/**
 * Created by monday on 2017/2/23.
 */

public class QingHaiRGEditFragment extends BaseDSEditFragment<DSEditPresenterImp> {

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
        return getString(R.string.inventoryQueryTypeSAPLocation);
    }
}
