package com.richfit.barcodesystemproduct.module_acceptstore.qinghai_105n;

import android.text.TextUtils;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_edit.BaseASEditFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_edit.imp.ASEditPresenterImp;

/**
 * Created by monday on 2017/2/20.
 */

public class QingHaiAS105NEditFragment extends BaseASEditFragment<ASEditPresenterImp> {


    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    public boolean checkCollectedDataBeforeSave() {
        final String location = getString(etLocation);
        if (TextUtils.isEmpty(location) || location.length() > 10) {
            showMessage("您输入的上架仓位不合理");
            return false;
        }
        return super.checkCollectedDataBeforeSave();
    }
}
