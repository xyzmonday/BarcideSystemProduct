package com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto351;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_ms_edit.BaseMSEditFragment;

/**
 * Created by monday on 2017/2/13.
 */

public class QingHaiUbSto351EditFragment extends BaseMSEditFragment {


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
