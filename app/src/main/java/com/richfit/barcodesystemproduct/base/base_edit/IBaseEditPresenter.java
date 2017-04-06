package com.richfit.barcodesystemproduct.base.base_edit;

import com.richfit.common_lib.IInterface.IPresenter;
import com.richfit.domain.bean.ResultEntity;

/**
 * Created by monday on 2017/4/6.
 */

public interface IBaseEditPresenter  <V extends IBaseEditView> extends IPresenter<V>  {
    /**
     * 保存本次采集的数据
     * @param result:用户采集的数据(json格式)
     */
    void uploadCollectionDataSingle(ResultEntity result);
}
