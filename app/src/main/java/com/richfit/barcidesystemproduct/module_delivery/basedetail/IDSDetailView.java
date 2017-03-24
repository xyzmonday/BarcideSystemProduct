package com.richfit.barcidesystemproduct.module_delivery.basedetail;


import com.richfit.barcodesystemproduct.base.base_detail.IBaseDetailView;

/**
 * Created by monday on 2016/11/20.
 */

public interface IDSDetailView<T> extends IBaseDetailView<T> {

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
