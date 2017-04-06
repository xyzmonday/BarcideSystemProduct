package com.richfit.barcodesystemproduct.module_movestore.qingyang_301n;

import android.text.TextUtils;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_edit.BaseMSNEditFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_edit.imp.MSNEditPresenterImp;

/**
 * Created by monday on 2017/2/8.
 */

public class QingYangMSN301EditFragment extends BaseMSNEditFragment<MSNEditPresenterImp> {

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected String getInvType() {
        return "0";
    }

    @Override
    public boolean checkCollectedDataBeforeSave() {
        final String recLocation = getString(etRecLoc);
        if (!TextUtils.isEmpty(recLocation) && recLocation.length() != 11) {
            showMessage("修改失败,请先检查接收仓位是否合理");
            return false;
        }
        return super.checkCollectedDataBeforeSave();
    }

    @Override
    protected String getInventoryQueryType() {
        return getString(R.string.inventoryQueryTypePrecise);
    }
}
