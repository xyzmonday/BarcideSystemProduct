package com.richfit.barcodesystemproduct.base.base_header;

import com.richfit.common_lib.IInterface.IPresenter;
import com.richfit.domain.bean.ResultEntity;

/**
 * 所有抬头界面的Presenter层接口
 * Created by monday on 2017/3/18.
 */

public interface IBaseHeaderPresenter <V extends IBaseHeaderView> extends IPresenter<V> {

    /**
     * 保存离线的抬头数据
     */
    void uploadEditedHeadData(ResultEntity resultEntity);
}
