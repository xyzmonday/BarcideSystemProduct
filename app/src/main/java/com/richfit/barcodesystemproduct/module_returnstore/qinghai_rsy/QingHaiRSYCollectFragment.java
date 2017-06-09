package com.richfit.barcodesystemproduct.module_returnstore.qinghai_rsy;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_collect.BaseASCollectFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_collect.imp.ASCollectPresenterImp;

/**
 * Created by monday on 2017/2/27.
 */

public class QingHaiRSYCollectFragment extends BaseASCollectFragment<ASCollectPresenterImp> {


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
        etLocation.setOnRichAutoEditTouchListener((view, location) -> getTransferSingle(getString(etBatchFlag), location));
    }

    @Override
    protected int getOrgFlag() {
        return 0;
    }
}
