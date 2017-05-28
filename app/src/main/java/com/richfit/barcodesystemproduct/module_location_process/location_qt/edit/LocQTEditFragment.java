package com.richfit.barcodesystemproduct.module_location_process.location_qt.edit;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.base.base_edit.BaseEditFragment;
import com.richfit.barcodesystemproduct.module_location_process.location_qt.edit.imp.LocQTEditPresenterImp;

/**
 * Created by monday on 2017/5/26.
 */

public class LocQTEditFragment extends BaseEditFragment<LocQTEditPresenterImp>
        implements ILocQTEditView {

    @Override
    protected int getContentId() {
        return R.layout.fragment_locqt_edit;
    }

    @Override
    public void initInjector() {

    }
}
