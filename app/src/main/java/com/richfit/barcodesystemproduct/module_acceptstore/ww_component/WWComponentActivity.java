package com.richfit.barcodesystemproduct.module_acceptstore.ww_component;

import com.richfit.barcodesystemproduct.module.main.BaseMainActivity;

/**
 * Created by monday on 2017/3/10.
 */

public class WWComponentActivity extends BaseMainActivity<WWCComponentPresenterImp> {

    @Override
    public void initInjector() {
        mActivityComponent.inject(this);
    }
}
