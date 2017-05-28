package com.richfit.barcodesystemproduct.barcodesystem_sdk.rs.base_rsn_header;

import com.richfit.barcodesystemproduct.base.base_header.IBaseHeaderView;
import com.richfit.domain.bean.WorkEntity;

import java.util.List;

/**
 * Created by monday on 2017/3/2.
 */

public interface IRSNHeaderView extends IBaseHeaderView {
    void showWorks(List<WorkEntity> works);

    void loadWorksFail(String message);

    void showAutoCompleteList(List<String> suppliers);

    void loadAutoCompleteFail(String message);

    void deleteCacheSuccess(String message);

    void deleteCacheFail(String message);
}
