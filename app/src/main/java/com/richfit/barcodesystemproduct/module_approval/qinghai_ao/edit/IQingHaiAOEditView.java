package com.richfit.barcodesystemproduct.module_approval.qinghai_ao.edit;

import com.richfit.barcodesystemproduct.base.base_edit.IBaseEditView;
import com.richfit.domain.bean.InvEntity;

import java.util.List;

/**
 * Created by monday on 2017/3/1.
 */

public interface IQingHaiAOEditView extends IBaseEditView {

    void showInvs(List<InvEntity> invs);

    void loadInvsFail(String message);
}
