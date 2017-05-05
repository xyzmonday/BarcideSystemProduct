package com.richfit.domain.repository;

import android.support.annotation.NonNull;

import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.MaterialEntity;
import com.richfit.domain.bean.MenuNode;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.ResultEntity;
import com.richfit.domain.bean.RowConfig;
import com.richfit.domain.bean.UserEntity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;

/**
 * 数据仓库顶级接口，离线和在线都必须实现该接口
 * Created by monday on 2016/12/29.
 */

public interface IRepository {

    /**
     * 用户登录
     *
     * @param userName：登录名
     * @param password：登录密码
     */
    Flowable<UserEntity> Login(String userName, String password);
    /**
     * 下载额外字段的配置信息
     *
     * @param companyId:公司Id
     * @return
     */
    Flowable<ArrayList<RowConfig>> loadExtraConfig(String companyId);

    /**
     * 获取单据数据
     *
     * @param refNum：单号
     * @param refType:单据类型
     * @param refLineId:单据行Id,该参数在委外入库时用到
     * @param bizType：业务类型
     * @param moveType:移动类型
     * @param userId：用户loginId
     */
    Flowable<ReferenceEntity> getReference(@NonNull String refNum, @NonNull String refType,
                                           @NonNull String bizType, @NonNull String moveType,
                                           @NonNull String refLineId, @NonNull String userId);

    /**
     * 抬头界面删除整单缓存
     *
     * @param refNum：单据号
     * @param transId：缓存id(在验收的中可能是inspectionId)
     * @param bizType:业务类型
     * @return
     */
    Flowable<String> deleteCollectionData(String refNum, String transId, String refCodeId,
                                          String refType, String bizType, String userId,
                                          String companyCode);

    /**
     * 数据明细界面获取整单缓存
     *
     * @param recordNum:单据号
     * @param refCodeId:单据id
     * @param refType:单据类型
     * @param bizType：业务类型
     * @param userId：用户id
     * @param workId：发出工厂id
     * @param invId:发出库存地点id
     * @param recWorkId：接收工厂id
     * @param recInvId：接搜库存地点id
     */
    Flowable<ReferenceEntity> getTransferInfo(String recordNum, String refCodeId, String bizType, String refType, String userId,
                                              String workId, String invId, String recWorkId, String recInvId);

    /**
     * 数据采集界面获取单条缓存。
     *
     * @param refCodeId：单据id
     * @param refType：单据类型
     * @param bizType：业务类型
     * @param refLineId：单据行id
     * @param workId:工厂id
     * @param invId:库存地点id
     * @param materialNum:物资编码
     * @param batchFlag:批次
     * @param location：仓位
     */
    Flowable<ReferenceEntity> getTransferInfoSingle(String refCodeId, String refType, String bizType,
                                                    String refLineId, String workId, String invId,
                                                    String recWorkId, String recInvId,
                                                    String materialNum, String batchFlag, String location,
                                                    String refDoc, int refDocItem, String userId);


    /**
     * 明细界面删除单个子节点(或者说单条缓存)
     *
     * @param lineDeleteFlag：是否删除整行(true/false)
     * @param transId：缓存抬头id
     * @param transLineId：缓存行明细id
     * @param locationId：缓存仓位id
     * @param refType:单据类型
     * @param bizType：业务类型
     * @param position：仓位
     */
    Flowable<String> deleteCollectionDataSingle(String lineDeleteFlag, String transId, String transLineId, String locationId,
                                                String refType, String bizType, String refLineId, String userId, int position,
                                                String companyCode);

    /**
     * 数据采界面，上传采集的数据
     *
     * @param result:用户本次采集的数据(json格式)
     * @return
     */
    Flowable<String> uploadCollectionDataSingle(ResultEntity result);

    /**
     * 保存盘点结果
     *
     * @param result
     * @return
     */
    Flowable<String> uploadCheckDataSingle(ResultEntity result);
    /**
     * 获取盘点头数据。
     *
     * @param userId
     * @param bizType
     * @param checkLevel:盘点级别         01:仓位级别 02:仓库级别
     * @param checkSpecial:特殊寄售标识
     * @param storageNum:仓库号
     * @param workId
     * @param invId
     * @param checkNum:对于有参考盘点需要的盘点单号
     * @return
     */
    Flowable<ReferenceEntity> getCheckInfo(String userId, String bizType, String checkLevel, String checkSpecial,
                                           String storageNum, String workId, String invId, String checkNum,String checkDate);

    /**
     * 删除整个盘点单
     *
     * @param checkId：单据抬头id
     * @param userId：用户id
     * @return
     */
    Flowable<String> deleteCheckData(String storageNum, String workId, String invId, String checkId, String userId, String bizType);

    /**
     * 盘点数据采集界面获取单条缓存
     *
     * @param checkId:盘点单id
     * @param materialNum：物料id
     * @param location：仓位
     * @return
     */
    Flowable<List<InventoryEntity>> getCheckTransferInfoSingle(String checkId, String materialId,
                                                               String materialNum, String location, String bizType);

    /**
     * 获取整单盘点缓存
     *
     * @param checkId：盘点id
     * @param materialNum：物料编码
     * @param location：仓位
     * @param isPageQuery：是否分页查询
     * @param pageNum：页码
     * @param pageSize：每页多少行
     * @return
     */
    Flowable<ReferenceEntity> getCheckTransferInfo(String checkId, String materialNum, String location,
                                                   String isPageQuery, int pageNum, int pageSize, String bizType);

    /**
     * 删除单条盘点数据
     *
     * @param checkId：抬头id
     * @param checkLineId：行id
     * @param userId：用户id
     * @return
     */
    Flowable<String> deleteCheckDataSingle(String checkId, String checkLineId, String userId, String bizType);

    /**
     * 获取物料信息
     *
     * @param queryType   01
     * @param materialNum
     * @return
     */
    Flowable<MaterialEntity> getMaterialInfo(String queryType, String materialNum);

    /**
     * 保存本次盘点的结果
     *
     * @param checkId
     * @return
     */
    Flowable<String> transferCheckData(String checkId, String userId, String bizType);

    Flowable<String> getLocationInfo(String queryType, String workId, String invId, String storageNum, String location);

    /**
     * 获取用户操作的菜单列表
     */
    Flowable<ArrayList<MenuNode>> getMenuInfo(String loginId, int mode);
}
