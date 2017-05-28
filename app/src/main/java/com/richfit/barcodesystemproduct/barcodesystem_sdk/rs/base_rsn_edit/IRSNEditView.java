package com.richfit.barcodesystemproduct.barcodesystem_sdk.rs.base_rsn_edit;

import com.richfit.barcodesystemproduct.base.base_edit.IBaseEditView;
import com.richfit.domain.bean.ReferenceEntity;

/**
 * Created by monday on 2017/3/2.
 */

public interface IRSNEditView extends IBaseEditView {

    /**
     * 输入物料获取缓存后，刷新界面
     *
     * @param refData
     * @param batchFlag
     */
    void onBindCommonUI(ReferenceEntity refData, String batchFlag);

    void loadTransferSingleInfoFail(String message);

    void loadTransferSingeInfoComplete();
}
