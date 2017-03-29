package com.richfit.domain.repository;

import com.richfit.domain.bean.ReferenceEntity;

/**
 * 该接口描述的是操作单据数据的协议。客户端获取本地单据数据，
 * 保存单据数据等操作需要实现该接口
 * Created by monday on 2017/3/29.
 */

public interface IReferenceServiceDao {
    /**
     * 获取采购订单单据数据
     *
     * @param refNum
     * @param refType
     * @param bizType
     * @param moveType
     * @param refLineId
     * @param userId
     * @return
     */
    ReferenceEntity getPoInfo(String refNum, String refType, String bizType,
                     String moveType, String refLineId, String userId);

    /**
     * 保存采购订单单据数据到本地
     *
     * @param refData
     * @param bizType
     * @param refType
     */
    void savePoInfo(ReferenceEntity refData, String bizType, String refType);

    /**
     * 获取应收清单的单据数据
     *
     * @param refNum
     * @param refType
     * @param bizType
     * @param moveType
     * @param refLineId
     * @param userId
     * @return
     */
    ReferenceEntity getInspectionInfo(String refNum, String refType, String bizType,
                             String moveType, String refLineId, String userId);

    /**
     * 保存验收清单单据数据到本地
     * @param refData
     * @param bizType
     * @param refType
     */
    void saveInspectionInfo(ReferenceEntity refData, String bizType, String refType);

    /**
     * 获取预留单据的单据数据
     *
     * @param refNum
     * @param refType
     * @param bizType
     * @param moveType
     * @param refLineId
     * @param userId
     * @return
     */
    ReferenceEntity getReservationInfo(String refNum, String refType, String bizType,
                              String moveType, String refLineId, String userId);

    /**
     * 保存预留单据到本地
     * @param refData
     * @param bizType
     * @param refType
     */
    void saveReservationInfo(ReferenceEntity refData, String bizType, String refType);

    /**
     * 获取交货单的单据数据
     *
     * @param refNum
     * @param refType
     * @param bizType
     * @param moveType
     * @param refLineId
     * @param userId
     * @return
     */
    ReferenceEntity getDeliveryInfo(String refNum, String refType, String bizType,
                           String moveType, String refLineId, String userId);

    /**
     * 保存交货单数据到本地
     * @param refData
     * @param bizType
     * @param refType
     */
    void saveDeliveryInfo(ReferenceEntity refData, String bizType, String refType);
}
