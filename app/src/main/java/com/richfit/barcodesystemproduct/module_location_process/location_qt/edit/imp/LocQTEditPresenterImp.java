package com.richfit.barcodesystemproduct.module_location_process.location_qt.edit.imp;

import android.content.Context;

import com.richfit.barcodesystemproduct.base.base_edit.BaseEditPresenterImp;
import com.richfit.barcodesystemproduct.module_location_process.location_qt.edit.ILocQTEditPreseneter;
import com.richfit.barcodesystemproduct.module_location_process.location_qt.edit.ILocQTEditView;
import com.richfit.common_lib.scope.ContextLife;

import javax.inject.Inject;

/**
 * Created by monday on 2017/5/26.
 */

public class LocQTEditPresenterImp extends BaseEditPresenterImp<ILocQTEditView>
        implements ILocQTEditPreseneter {

    @Inject
    public LocQTEditPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }
}
