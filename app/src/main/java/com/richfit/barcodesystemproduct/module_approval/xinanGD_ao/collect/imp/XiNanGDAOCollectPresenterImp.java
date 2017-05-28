package com.richfit.barcodesystemproduct.module_approval.xinanGD_ao.collect.imp;

import android.content.Context;

import com.richfit.barcodesystemproduct.base.BasePresenter;
import com.richfit.barcodesystemproduct.module_approval.xinanGD_ao.collect.IXiNanGDAOCollectPresenter;
import com.richfit.barcodesystemproduct.module_approval.xinanGD_ao.collect.IXiNanGDAOCollectView;
import com.richfit.common_lib.scope.ContextLife;

import javax.inject.Inject;

/**
 * Created by monday on 2017/5/26.
 */

public class XiNanGDAOCollectPresenterImp extends BasePresenter<IXiNanGDAOCollectView>
        implements IXiNanGDAOCollectPresenter {

    @Inject
    public XiNanGDAOCollectPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }
}
