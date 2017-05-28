package com.richfit.barcodesystemproduct.barcodesystem_sdk.rs.base_rsn_edit;

import com.richfit.barcodesystemproduct.base.base_edit.IBaseEditPresenter;

/**
 * Created by monday on 2017/3/2.
 */

public interface IRSNEditPresenter extends IBaseEditPresenter<IRSNEditView> {

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

}
