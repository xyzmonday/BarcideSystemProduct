package com.richfit.barcidesystemproduct.module_delivery.qinghai_dsn.detail;

import com.richfit.barcodesystemproduct.base.base_detail.IBaseDetailView;

/**
 * Created by monday on 2017/2/23.
 */

public interface IQingHaiDSNDetailView<T> extends IBaseDetailView<T> {
    /**
     * 寄售转自有成功
     */
    void turnOwnSuppliesSuccess();

    /**
     * 寄售转自有失败
     * @param message
     */
    void turnOwnSuppliesFail(String message);
}
