package com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_header;

import com.richfit.barcodesystemproduct.base.base_header.IBaseHeaderPresenter;

/**
 * Created by monday on 2016/11/20.
 */

public interface IMSNHeaderPresenter extends IBaseHeaderPresenter<IMSNHeaderView> {

    /**
     * 获取发出工厂列表
     */
    void getWorks(int flag);

    /**
     * 通过工厂id获取该工厂下的接收库存地点列表
     * @param workId
     */
    void getRecInvsByWorkId(String workId, int flag);

    void getSendInvsByWorkId(String workId, int flag);

    /**
     * 删除整单缓存
     * @param bizType：业务类型
     * @param userId:用户id
     */
    void deleteCollectionData(String refType, String bizType, String userId,
                              String companyCode);
}
