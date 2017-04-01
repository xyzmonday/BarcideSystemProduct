package com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_header;


import com.richfit.barcodesystemproduct.base.base_header.IBaseHeaderView;
import com.richfit.domain.bean.InvEntity;
import com.richfit.domain.bean.WorkEntity;

import java.util.List;

/**
 * 无参考移库抬头view标准
 * Created by monday on 2016/11/20.
 */

public interface IMSNHeaderView extends IBaseHeaderView {
    void showWorks(List<WorkEntity> works);
    void loadWorksFail(String message);

    void showSendInvs(List<InvEntity> sendInvs);
    void loadSendInvsFail(String message);

    void showRecInvs(List<InvEntity> recInvs);
    void loadRecInvsFail(String message);

    void deleteCacheSuccess(String message);
    void deleteCacheFail(String message);

    /**
     * 数据上传成功后，清除抬头控件的信息
     */
    void clearAllUI();
}
