package com.richfit.barcodesystemproduct.module_infoquery.inventory_query_n.header;

import com.richfit.barcodesystemproduct.base.BaseView;
import com.richfit.domain.bean.InvEntity;
import com.richfit.domain.bean.WorkEntity;

import java.util.List;

/**
 * Created by monday on 2017/5/25.
 */

public interface IInvNQueryHeaderView extends BaseView {
    void showWorks(List<WorkEntity> works);

    void loadWorksFail(String message);

    void loadWorksComplete();

    void showInvs(List<InvEntity> recInvs);

    void loadInvsFail(String message);

    void loadInvsComplete();

    void queryInvInfoComplete();
}
