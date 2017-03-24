package com.richfit.barcidesystemproduct.module_check.qinghai_blind.collect;

import com.richfit.common_lib.IInterface.IPresenter;
import com.richfit.domain.bean.ResultEntity;

/**
 * Created by monday on 2017/3/3.
 */

public interface IBlindCollectPresenter extends IPresenter<IBlindCollectView> {
    /**
     * 获取单条盘点缓存
     *
     * @param checkId
     * @param queryType
     * @param materialNum
     */
    void getCheckTransferInfoSingle(String checkId, String location, String queryType,
                                    String materialNum, String bizType);

    /**
     * 保存本次盘点的数量
     * @param result
     */
    void uploadCheckDataSingle(ResultEntity result);
}
