package com.richfit.barcodesystemproduct.module_approval.xinanGD_ao.edit.imp;

import android.content.Context;

import com.richfit.barcodesystemproduct.base.base_edit.BaseEditPresenterImp;
import com.richfit.barcodesystemproduct.module_approval.xinanGD_ao.edit.IXiNanGDAOEditPresenter;
import com.richfit.barcodesystemproduct.module_approval.xinanGD_ao.edit.IXiNanGDAOEditView;
import com.richfit.common_lib.scope.ContextLife;

import javax.inject.Inject;

/**
 * Created by monday on 2017/5/26.
 */

public class XiNanGDAOEditPresenterImp extends BaseEditPresenterImp<IXiNanGDAOEditView>
        implements IXiNanGDAOEditPresenter {

    @Inject
    public XiNanGDAOEditPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }
}
