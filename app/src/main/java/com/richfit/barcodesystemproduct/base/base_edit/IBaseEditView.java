package com.richfit.barcodesystemproduct.base.base_edit;

import com.richfit.barcodesystemproduct.base.BaseView;

/**
 * Created by monday on 2017/4/6.
 */

public interface IBaseEditView extends BaseView {
    void saveEditedDataSuccess(String message);
    void saveEditedDataFail(String message);
}
