package com.richfit.domain.repository;

import com.richfit.domain.bean.ReferenceEntity;

/**
 * 获取本地缓存接口
 * Created by monday on 2017/3/29.
 */

public interface ITransferServiceDao {



    /**
     * 获取无参考业务的整单缓存
     * @param refCodeId
     * @param refType
     * @param bizType
     * @param workId
     * @param invId
     * @param recWorkId
     * @param recInvId
     * @param userId
     * @return
     */
    ReferenceEntity getBusinessTransferInfo(String recordNum, String refCodeId, String bizType,
                                            String refType, String userId, String workId,
                                            String invId, String recWorkId, String recInvId);

    /**
     * 获取有参考业务的整单缓存
     * @param refCodeId
     * @param refType
     * @param bizType
     * @param workId
     * @param invId
     * @param recWorkId
     * @param recInvId
     * @param userId
     * @return
     */
    ReferenceEntity getBusinessTransferInfoRef(String recordNum, String refCodeId, String bizType,
                                               String refType, String userId, String workId,
                                               String invId, String recWorkId, String recInvId);
    /**
     *  获取验收业务的缓存
     * @param refCodeId
     * @param refType
     * @param bizType
     * @param refLineId
     * @param workId
     * @param invId
     * @param recWorkId
     * @param recInvId
     * @param materialNum
     * @param batchFlag
     * @param location
     * @param refDoc
     * @param refDocItem
     * @param userId
     * @return
     */
    ReferenceEntity getInspectTransferInfoSingle(String refCodeId, String refType, String bizType, String refLineId, String workId, String invId, String recWorkId, String recInvId, String materialNum,
                                                 String batchFlag, String location, String refDoc, int refDocItem, String userId);
    /**
     * 获取无参考业务的单条缓存
     * @param refCodeId
     * @param refType
     * @param bizType
     * @param refLineId
     * @param workId
     * @param invId
     * @param recWorkId
     * @param recInvId
     * @param materialNum
     * @param batchFlag
     * @param location
     * @param refDoc
     * @param refDocItem
     * @param userId
     * @return
     */
    ReferenceEntity getBusinessTransferInfoSingle(String refCodeId, String refType, String bizType, String refLineId,
                                                  String workId, String invId, String recWorkId, String recInvId,
                                                  String materialNum, String batchFlag, String location, String refDoc, int refDocItem, String userId);

    /**
     * 获取有参考业务的单条缓存
     * @param refCodeId
     * @param refType
     * @param bizType
     * @param refLineId
     * @param workId
     * @param invId
     * @param recWorkId
     * @param recInvId
     * @param materialNum
     * @param batchFlag
     * @param location
     * @param refDoc
     * @param refDocItem
     * @param userId
     * @return
     */
    ReferenceEntity getBusinessTransferInfoSingleRef(String refCodeId, String refType, String bizType, String refLineId,
                                                     String workId, String invId, String recWorkId, String recInvId,
                                                     String materialNum, String batchFlag, String location, String refDoc, int refDocItem, String userId);


}
