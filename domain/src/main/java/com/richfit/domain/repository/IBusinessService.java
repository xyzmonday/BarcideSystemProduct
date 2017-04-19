package com.richfit.domain.repository;

import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.ResultEntity;

import java.util.List;

/**
 * Created by monday on 2017/3/29.
 */

public interface IBusinessService {
    /**
     * 保存单条采集的数据
     * @param param
     * @return
     */
    boolean uploadBusinessDataSingle(ResultEntity param);


    /**
     * 删除整单缓存数据
     * @param refNum
     * @param transId
     * @param refCodeId
     * @param refType
     * @param bizType
     * @param userId
     * @param companyCode
     * @return
     */
    boolean deleteBusinessData(String refNum, String transId, String refCodeId,
                              String refType, String bizType, String userId,
                              String companyCode);

    /**
     * 删除单条缓存数据(包括了父子节点)
     * @param businessType
     * @param transLineId
     * @param transId
     * @return
     */
    boolean deleteBusinessDataByLineId(String businessType, String transId, String transLineId);

    /**
     * 删除单条缓存数据(仅仅针对子节点，如果没有父子节点，那么删除时当前节点的数据)
     * @param locationId
     * @param transLineId
     * @param transId
     * @return
     */
    boolean deleteBusinessDataByLocationId(String locationId, String transId, String transLineId);

    List<ReferenceEntity> readTransfredData();

    void deleteOfflineDataAfterUploadSuccess(String transId,String bizType,String refType,String userId);
}
