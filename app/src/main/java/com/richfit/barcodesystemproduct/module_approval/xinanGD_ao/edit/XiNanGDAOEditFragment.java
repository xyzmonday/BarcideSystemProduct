package com.richfit.barcodesystemproduct.module_approval.xinanGD_ao.edit;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.base.base_edit.BaseEditFragment;
import com.richfit.barcodesystemproduct.module_approval.xinanGD_ao.edit.imp.XiNanGDAOEditPresenterImp;

/**
 * Created by monday on 2017/5/26.
 */

public class XiNanGDAOEditFragment extends BaseEditFragment<XiNanGDAOEditPresenterImp>
        implements IXiNanGDAOEditView {

    @Override
    protected int getContentId() {
        return R.layout.fragment_xinangd_ao_edit;
    }

    @Override
    public void initInjector() {

    }
}
