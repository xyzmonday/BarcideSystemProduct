package com.richfit.barcodesystemproduct.module_approval.qinghai_ao.edit;

import com.richfit.barcodesystemproduct.base.base_edit.IBaseEditPresenter;
import com.richfit.domain.bean.ResultEntity;

/**
 * Created by monday on 2017/3/1.
 */

public interface IQingHaiAOEditPresenter extends IBaseEditPresenter<IQingHaiAOEditView> {

    void getInvsByWorkId(String workId, int flag);

}
