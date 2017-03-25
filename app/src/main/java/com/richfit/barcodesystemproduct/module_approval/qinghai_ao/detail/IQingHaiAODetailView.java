package com.richfit.barcodesystemproduct.module_approval.qinghai_ao.detail;

import com.richfit.barcodesystemproduct.base.base_detail.IBaseDetailView;

import java.util.List;

/**
 * Created by monday on 2017/2/28.
 */

public interface IQingHaiAODetailView<T> extends IBaseDetailView<T> {
    /**
     * 显示明细。注意这里需要给出单据的抬头的TransId
     *
     * @param nodes
     */
    void showNodes(List<T> nodes, String transId);
}
