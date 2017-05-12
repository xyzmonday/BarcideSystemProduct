package com.richfit.domain.repository;

import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.ResultEntity;

import java.util.List;

/**
 * Created by monday on 2017/3/29.
 */

public interface ICheckServiceDao {
    /**
     * 获取盘点抬头信息
     *
     * @param userId
     * @param bizType
     * @param checkLevel
     * @param checkSpecial
     * @param storageNum
     * @param workId
     * @param invId
     * @param checkNum
     * @return
     */
    ReferenceEntity getCheckInfo(String userId, String bizType, String checkLevel, String checkSpecial,
                                 String storageNum, String workId, String invId, String checkNum,String checkDate);


    /**
     * 删除盘点数据
     *
     * @param storageNum
     * @param workId
     * @param invId
     * @param checkId
     * @param userId
     * @param bizType
     * @return
     */
    boolean deleteCheckData(String storageNum, String workId, String invId, String checkId, String userId,
                            String bizType);

    /**
     * 获取盘点单条缓存
     *
     * @param checkId
     * @param materialId
     * @param materialNum
     * @param location
     * @param bizType
     * @return
     */
    List<InventoryEntity> getCheckTransferInfoSingle(String checkId, String materialId,
                                                     String materialNum, String location,
                                                     String bizType);

    /**
     * 获取盘点整单缓存
     *
     * @param checkId
     * @param materialNum
     * @param location
     * @param isPageQuery
     * @param pageNum
     * @param pageSize
     * @param bizType
     * @return
     */
    ReferenceEntity getCheckTransferInfo(String checkId, String materialNum, String location, String isPageQuery,
                                         int pageNum, int pageSize, String bizType);

    /**
     * 删除盘点单条缓存
     *
     * @param checkId
     * @param checkLineId
     * @param userId
     * @param bizType
     * @return
     */
    boolean deleteCheckDataSingle(String checkId, String checkLineId, String userId, String bizType);

    /**
     * 保存单条盘点结果
     * @param result
     * @return
     */
    boolean uploadCheckDataSingle(ResultEntity result);

    List<ReferenceEntity> readTransferedData();

    boolean setTransFlag(String transId,String transFlag);

    boolean uploadEditedHeadData(ResultEntity resultEntity);

    void deleteOfflineDataAfterUploadSuccess(String transId, String bizType, String refType, String userId);
}
