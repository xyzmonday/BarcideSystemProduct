package com.richfit.barcodesystemproduct.module_location_process.location_qt.collect;

import com.richfit.common_lib.IInterface.IPresenter;

/**
 * Created by monday on 2017/5/26.
 */

public interface ILocQTCollectPresenter extends IPresenter<ILocQTCollectView>{

    /**
     * 获取库存信息
     *
     * @param workId:工厂id
     * @param invId：库存地点id
     * @param materialId：物料id
     * @param location：仓位
     * @param batchFlag:批次
     * @param invType：库存类型
     */
    void getInventoryInfo(String queryType, String workId, String invId, String workCode, String invCode,
                          String storageNum, String materialNum, String materialId, String location, String batchFlag,
                          String specialInvFlag, String specialInvNum, String invType, String deviceId);

    /**
     * 获取单条缓存
     *
     * @param refCodeId：单据id
     * @param refType：单据类型
     * @param bizType：业务类型
     * @param refLineId：单据行id
     * @param batchFlag:批次
     * @param location：仓位
     */
    void getTransferInfoSingle(String refCodeId, String refType, String bizType, String refLineId,String materialNum,
                               String batchFlag, String location, String refDoc, int refDocItem, String userId);

    void checkLocation(String queryType, String workId, String invId, String batchFlag, String location);
}
