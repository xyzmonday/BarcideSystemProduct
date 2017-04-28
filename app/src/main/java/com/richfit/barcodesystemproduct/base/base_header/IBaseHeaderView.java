package com.richfit.barcodesystemproduct.base.base_header;

import com.richfit.barcodesystemproduct.base.BaseView;

/**
 * 所有抬头页面的View层接口
 * Created by monday on 2017/3/18.
 */

public interface IBaseHeaderView extends BaseView {
    /**
     * 上传数据成功后，清空抬头所有的信息
     */
    void clearAllUIAfterSubmitSuccess();

    void uploadEditedHeadDataFail(String message);
    void uploadEditedHeadComplete();
}
