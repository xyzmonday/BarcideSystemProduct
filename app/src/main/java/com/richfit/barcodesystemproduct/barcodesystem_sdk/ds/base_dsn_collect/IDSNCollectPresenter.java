package com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_dsn_collect;

import com.richfit.common_lib.IInterface.IPresenter;
import com.richfit.domain.bean.ResultEntity;

/**
 * Created by monday on 2017/2/23.
 */

public interface IDSNCollectPresenter extends IPresenter<IDSNCollectView> {

    /**
     * 获取发出库存地点列表
     *
     * @param workId
     */
    void getInvsByWorks(String workId, int flag);

    /**
     * 获取数据采集界面的缓存
     *
     * @param bizType：业务类型
     * @param materialNum：物资编码
     * @param userId：用户id
     * @param workId：发出工厂id
     * @param recWorkId：接收工厂id
     * @param recInvId：接收库存点id
     * @param batchFlag：发出批次
     */
    void getTransferInfoSingle(String bizType, String materialNum, String userId, String workId,
                               String invId, String recWorkId, String recInvId, String batchFlag,
                               String refDoc, int refDocItem);

    /**
     * 获取库存信息
     *
     * @param queryType:查询类型
     * @param workId：发出工厂id
     * @param invId:发出库位id
     * @param materialId:发出物料id
     * @param location：发出仓位
     * @param batchFlag：发出批次
     * @param invType：库存类型
     */
    void getInventoryInfo(String queryType, String workId, String invId, String workCode,
                          String invCode, String storageNum, String materialNum, String materialId,
                          String location, String batchFlag, String specialInvFag, String specialInvNum,
                          String invType, String deviceId);

    /**
     * 保存本次采集的数据
     *
     * @param result:用户采集的数据(json格式)
     */
    void uploadCollectionDataSingle(ResultEntity result);
}
