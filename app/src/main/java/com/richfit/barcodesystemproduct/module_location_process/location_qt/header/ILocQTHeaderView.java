package com.richfit.barcodesystemproduct.module_location_process.location_qt.header;

import com.richfit.barcodesystemproduct.base.base_header.IBaseHeaderView;
import com.richfit.domain.bean.ReferenceEntity;

/**
 * Created by monday on 2017/5/25.
 */

public interface ILocQTHeaderView extends IBaseHeaderView {

    /**
     * 读取单据数据
     *
     * @param refData:单据数据
     */
    void getReferenceSuccess(ReferenceEntity refData);

    /**
     * 读取单据数据失败
     *
     * @param message
     */
    void getReferenceFail(String message);

    /**
     * 请求单据数据，清除抬头必要的控件信息
     */
    void clearAllUI();

}
