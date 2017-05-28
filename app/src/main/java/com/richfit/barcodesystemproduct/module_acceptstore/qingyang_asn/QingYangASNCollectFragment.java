package com.richfit.barcodesystemproduct.module_acceptstore.qingyang_asn;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_asn_collect.BaseASNCollectFragment;

/**
 * 注意庆阳的无参考出库没有上架仓位，默认为barcode
 * Created by monday on 2016/11/27.
 */

public class QingYangASNCollectFragment extends BaseASNCollectFragment{

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }
}
