package com.richfit.barcidesystemproduct.module_acceptstore.baseedit;


import com.richfit.barcodesystemproduct.base.BaseView;

/**
 * Created by monday on 2016/11/19.
 */

public interface IASEditView extends BaseView {
    void saveEditedDataSuccess(String message);
    void saveEditedDataFail(String message);
}
