package com.richfit.barcodesystemproduct.module_infoquery.inventory_query_y.header;

import com.richfit.barcodesystemproduct.base.BaseView;
import com.richfit.domain.bean.ReferenceEntity;

/**
 * Created by monday on 2017/5/25.
 */

public interface IInvYQueryHeaderView extends BaseView {

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

    void getReferenceComplete();

    /**
     * 请求单据数据，清除抬头必要的控件信息
     */
    void clearAllUI();
}
