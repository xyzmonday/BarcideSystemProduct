package com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto101;

import android.support.annotation.NonNull;
import android.view.View;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_header.BaseASHeaderFragment;
import com.richfit.common_lib.utils.L;

/**
 * 青海移库转储101抬头界面(采购订单)。相当于UbSto101转储接收，业务与有参考入库，注意显示字段
 * Created by monday on 2017/2/15.
 */

public class QingHaiUbSto101HeaderFragment extends BaseASHeaderFragment {

    @Override
    protected void initView() {
        L.e("离线 QingHaiUbSto101HeaderFragment" );
        //需要显示发出工厂
        llSendWork.setVisibility(View.VISIBLE);
        super.initView();
    }

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected String getMoveType() {
        return "3";
    }

}
