package com.richfit.barcodesystemproduct.module_returnstore.qingyang_rsy;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_edit.BaseASEditFragment;
import com.richfit.barcodesystemproduct.module_returnstore.qingyang_rsy.imp.QingYangRSYEditPresenterImp;

/**
 * Created by monday on 2017/3/29.
 */

public class QingYangRSYEditFragment extends BaseASEditFragment<QingYangRSYEditPresenterImp> {

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }


    @Override
    protected void initView() {
        actQuantityName.setText("应退数量");
        quantityName.setText("实退数量");
        super.initView();
    }
}
