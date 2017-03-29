package com.richfit.barcodesystemproduct.module_returnstore.qingyang_rsy;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_collect.BaseASCollectFragment;
import com.richfit.barcodesystemproduct.module_returnstore.qingyang_rsy.imp.QingYangRSYCollectPresenterImp;

/**
 * Created by monday on 2017/3/29.
 */

public class QingYangRSYCollectFragment extends BaseASCollectFragment<QingYangRSYCollectPresenterImp> {

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected void initView() {
        tvActQuantityName.setText("应退数量");
        tvQuantityName.setText("实退数量");
        super.initView();
    }

    @Override
    public void initEvent() {
        super.initEvent();
        etLocation.setOnRichEditTouchListener((view, location) -> getTransferSingle(getString(etBatchFlag), location));
    }

    @Override
    protected int getOrgFlag() {
        return 0;
    }
}
