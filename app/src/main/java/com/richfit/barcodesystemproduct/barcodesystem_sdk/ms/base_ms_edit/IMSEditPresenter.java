package com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_ms_edit;

import com.richfit.barcodesystemproduct.base.base_edit.IBaseEditPresenter;

/**
 * Created by monday on 2017/2/13.
 */

public interface IMSEditPresenter extends IBaseEditPresenter<IMSEditView> {

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
    void getInventoryInfo(String queryType, String workId, String invId,
                          String workCode, String invCode, String storageNum,
                          String materialNum, String materialId,
                          String location, String batchFlag, String specialInvFlag,
                          String specialInvNum, String invType, String deviceId);

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
    void getTransferInfoSingle(String refCodeId, String refType, String bizType, String refLineId,
                               String batchFlag, String location, String refDoc, int refDocItem, String userId);
}