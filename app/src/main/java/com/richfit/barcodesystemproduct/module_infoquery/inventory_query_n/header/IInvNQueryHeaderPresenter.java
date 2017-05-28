package com.richfit.barcodesystemproduct.module_infoquery.inventory_query_n.header;

import com.richfit.common_lib.IInterface.IPresenter;

/**
 * Created by monday on 2017/5/25.
 */

public interface IInvNQueryHeaderPresenter extends IPresenter<IInvNQueryHeaderView> {

    /**
     * 获取发出工厂列表
     */
    void getWorks(int flag);

    /**
     * 通过工厂id获取该工厂下的接收库存地点列表
     * @param workId
     */
    void getInvsByWorkId(String workId, int flag);

    void getInventoryInfo(String queryType, String workId, String invId, String workCode, String invCode,
                          String storageNum, String materialNum, String materialId, String location, String batchFlag,
                          String specialInvFlag, String specialInvNum, String invType, String deviceId);
}
