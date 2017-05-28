package com.richfit.barcodesystemproduct.module_acceptstore.qingyang_asn;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_asn_header.BaseASNHeaderFragment;

/**
 * 无参考物资入库抬头(这里先不在封装成基类)
 * Created by monday on 2016/11/16.
 */

public class QingYangASNHeaderFragment extends BaseASNHeaderFragment {

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

}
