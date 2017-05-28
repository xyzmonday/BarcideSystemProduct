package com.richfit.barcodesystemproduct.module_approval.xinanGD_ao.detail.imp;

import android.content.Context;

import com.richfit.barcodesystemproduct.base.base_detail.BaseDetailPresenterImp;
import com.richfit.barcodesystemproduct.module_approval.xinanGD_ao.detail.IXiNanGDAODetailPresenter;
import com.richfit.barcodesystemproduct.module_approval.xinanGD_ao.detail.IXiNanGDAODetailView;
import com.richfit.common_lib.scope.ContextLife;

import javax.inject.Inject;

/**
 * Created by monday on 2017/5/26.
 */

public class IXiNanGDAODetailPresenterImp extends BaseDetailPresenterImp<IXiNanGDAODetailView>
        implements IXiNanGDAODetailPresenter {

    @Inject
    public IXiNanGDAODetailPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }
}
