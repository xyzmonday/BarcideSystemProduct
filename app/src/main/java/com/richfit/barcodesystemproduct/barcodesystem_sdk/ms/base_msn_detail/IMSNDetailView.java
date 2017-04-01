package com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_detail;

import com.richfit.barcodesystemproduct.base.base_detail.IBaseDetailView;

/**
 * Created by monday on 2016/11/20.
 */

public interface IMSNDetailView<T> extends IBaseDetailView<T> {
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
