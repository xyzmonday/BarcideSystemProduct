package com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_dsn_detail;

import com.richfit.barcodesystemproduct.base.base_detail.IBaseDetailPresenter;

import java.util.Map;

/**
 * Created by monday on 2017/2/23.
 */

public interface IDSNDetailPresenter extends IBaseDetailPresenter<IDSNDetailView> {
 /*增加一个寄售转自有的业务，该业务仅仅针对出库移库相关的业务才有*/
    /**
     * 寄售转自有业务。
     *
     * @param transId
     * @param bizType
     * @param refType
     * @param userId
     * @param voucherDate
     * @param transToSapFlag
     * @param extraHeaderMap
     * @param submitFlag
     */
    void turnOwnSupplies(String transId, String bizType, String refType, String userId, String voucherDate,
                        String transToSapFlag, Map<String, Object> extraHeaderMap, int submitFlag);
}
