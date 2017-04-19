package com.richfit.barcodesystemproduct.module_acceptstore.ww_component.edit;

import android.content.Context;

import com.richfit.barcodesystemproduct.base.base_edit.BaseEditPresenterImp;
import com.richfit.common_lib.scope.ContextLife;

import javax.inject.Inject;

/**
 * Created by monday on 2017/3/13.
 */

public class QingHaiWWCEditPresenterImp extends BaseEditPresenterImp<QingHaiWWCEditContract.IQingHaiWWCEditView>
        implements QingHaiWWCEditContract.IQingHaiWWCEditPreseneter {

    @Inject
    public QingHaiWWCEditPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }
}

