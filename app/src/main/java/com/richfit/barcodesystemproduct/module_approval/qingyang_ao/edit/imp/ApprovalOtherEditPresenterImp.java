package com.richfit.barcodesystemproduct.module_approval.qingyang_ao.edit.imp;

import android.content.Context;

import com.richfit.barcodesystemproduct.base.base_edit.BaseEditPresenterImp;
import com.richfit.barcodesystemproduct.module_approval.qingyang_ao.edit.IApprovalOtherEditPresenter;
import com.richfit.barcodesystemproduct.module_approval.qingyang_ao.edit.IApprovalOtherEditView;
import com.richfit.common_lib.scope.ContextLife;

import javax.inject.Inject;

/**
 * Created by monday on 2016/11/29.
 */

public class ApprovalOtherEditPresenterImp extends BaseEditPresenterImp<IApprovalOtherEditView>
        implements IApprovalOtherEditPresenter {

    @Inject
    public ApprovalOtherEditPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }
}
