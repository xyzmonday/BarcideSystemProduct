package com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_edit;


import com.richfit.barcodesystemproduct.base.BaseView;

/**
 * Created by monday on 2016/11/19.
 */

public interface IASEditView extends BaseView {
    void saveEditedDataSuccess(String message);
    void saveEditedDataFail(String message);
}
