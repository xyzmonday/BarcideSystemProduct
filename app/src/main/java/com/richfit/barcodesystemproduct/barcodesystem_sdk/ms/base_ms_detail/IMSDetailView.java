package com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_ms_detail;

import com.richfit.barcodesystemproduct.base.base_detail.IBaseDetailView;

/**
 * Created by monday on 2017/2/10.
 */

public interface IMSDetailView<T> extends IBaseDetailView<T>{
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
