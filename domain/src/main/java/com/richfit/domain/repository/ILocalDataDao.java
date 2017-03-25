package com.richfit.domain.repository;

import android.support.annotation.NonNull;

import com.richfit.domain.bean.BizFragmentConfig;
import com.richfit.domain.bean.ImageEntity;
import com.richfit.domain.bean.InvEntity;
import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.MenuNode;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.ResultEntity;
import com.richfit.domain.bean.RowConfig;
import com.richfit.domain.bean.SimpleEntity;
import com.richfit.domain.bean.UserEntity;
import com.richfit.domain.bean.WorkEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 本地数据Dao需要实现的接口。
 * Created by monday on 2017/3/25.
 */

public interface ILocalDataDao {
    /**
     * 获取单据数据
     *
     * @param refNum
     * @param refType
     * @param bizType
     * @param moveType
     * @param refLineId
     * @param userId
     * @return
     */
    ReferenceEntity getReference(String refNum, String refType, String bizType,
                                 String moveType, String refLineId, String userId);

    /**
     * 保存单据数据到本地
     *
     * @param refData
     * @param bizType
     * @param refType
     */
    void saveReferenceInfo(ReferenceEntity refData, String bizType, String refType);

    /**
     * 删除整单缓存
     *
     * @param refNum
     * @param transId
     * @param refCodeId
     * @param refType
     * @param bizType
     * @param userId
     * @param companyCode
     * @return
     */
    boolean deleteCollectionData(String refNum, String transId, String refCodeId, String refType,
                                String bizType, String userId, String companyCode);

    /**
     * 获取整单缓存
     *
     * @param recordNum
     * @param refCodeId
     * @param bizType
     * @param refType
     * @param userId
     * @param workId
     * @param invId
     * @param recWorkId
     * @param recInvId
     * @return
     */
    ReferenceEntity getTransferInfo(String recordNum, String refCodeId, String bizType, String refType,
                                    String userId, String workId, String invId, String recWorkId, String recInvId);

    /**
     * 获取单条缓存
     *
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
    ReferenceEntity getTransferInfoSingle(String refCodeId, String refType, String bizType, String refLineId,
                                          String workId, String invId, String recWorkId, String recInvId,
                                          String materialNum, String batchFlag, String location, String refDoc, int refDocItem, String userId);

    /**
     * 删除单条缓存
     *
     * @param lineDeleteFlag
     * @param transId
     * @param transLineId
     * @param locationId
     * @param refType
     * @param bizType
     * @param refLineId
     * @param userId
     * @param position
     * @param companyCode
     * @return
     */
    String deleteCollectionDataSingle(String lineDeleteFlag, String transId, String transLineId,
                                      String locationId, String refType, String bizType, String refLineId,
                                      String userId, int position, String companyCode);

    /**
     * 保存单条数据
     *
     * @param result
     * @return
     */
    boolean uploadCollectionDataSingle(ResultEntity result);

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
                                 String storageNum, String workId, String invId, String checkNum);

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
     * 读取用户登陆信息
     * @param userName
     * @param password
     * @return
     */
    ArrayList<String> readUserInfo(String userName, String password);

    /**
     * 保存用户登陆信息
     * @param userEntity
     */
    void saveUserInfo(UserEntity userEntity);

    /**
     * 保存扩展字段配置信息
     * @param configs
     */
    void saveExtraConfigInfo(List<RowConfig> configs);

    /**
     * 读取扩展字段配置信息
     * @param companyCode
     * @param bizType
     * @param refType
     * @param configType
     * @return
     */
    ArrayList<RowConfig> readExtraConfigInfo(String companyCode, String bizType, String refType,
                                             String configType);

    /**
     * 读取扩展字段字典数据
     * @param propertyCode
     * @param dictionaryCode
     * @return
     */
    Map<String, Object> readExtraDataSourceByDictionary(@NonNull String propertyCode, @NonNull String dictionaryCode);

    /**
     * 获取该类基础数据上一次下载的日期
     * @param queryType
     * @return
     */
    String getLoadBasicDataTaskDate(String queryType);

    /**
     * 保存该类基础数据本次下载的日期
     * @param queryType
     * @param queryDate
     */
    void saveLoadBasicDataTaskDate(String queryType, String queryDate);

    /**
     * 保存基础数据
     * @param maps
     * @return
     */
    int saveBasicData(List<Map<String, Object>> maps);

    /**
     * 跟新扩展字段基础数据表字段
     * @param map
     */
    void updateExtraConfigTable(Map<String, Set<String>> map);

    /**
     * 获取该工厂下的所有库存地点
     * @param workId
     * @param flag
     * @return
     */
    ArrayList<InvEntity> getInvsByWorkId(String workId, int flag);

    /**
     * 获取工厂列表
     * @param flag
     * @return
     */
    ArrayList<WorkEntity> getWorks(int flag);


    /**
     * 检查接收库位和发出库位实发在同一个仓库
     * @param sendWorkId
     * @param sendInvCode
     * @param recWorkId
     * @param recInvCode
     * @param flag
     * @return
     */
    boolean checkWareHouseNum(String sendWorkId, String sendInvCode, String recWorkId, String recInvCode, int flag);

    /**
     * 获取供应商列表
     * @param workCode
     * @param keyWord
     * @param defaultItemNum
     * @param flag
     * @return
     */
    ArrayList<SimpleEntity> getSupplierList(String workCode, String keyWord, int defaultItemNum, int flag);

    /**
     * 获取成本中心列表
     * @param workCode
     * @param keyWord
     * @param defaultItemNum
     * @param flag
     * @return
     */
    ArrayList<SimpleEntity> getCostCenterList(String workCode, String keyWord, int defaultItemNum, int flag);

    /**
     * 获取项目好列表
     * @param workCode
     * @param keyWord
     * @param defaultItemNum
     * @param flag
     * @return
     */
    ArrayList<SimpleEntity> getProjectNumList(String workCode, String keyWord, int defaultItemNum, int flag);

    /**
     * 保存页面配置信息
     * @param bizFragmentConfigs
     * @return
     */
    boolean saveBizFragmentConfig(ArrayList<BizFragmentConfig> bizFragmentConfigs);

    /**
     * 读取页面配置信息
     * @param bizType
     * @param refType
     * @param fragmentType
     * @return
     */
    ArrayList<BizFragmentConfig> readBizFragmentConfig(String bizType, String refType, int fragmentType);

    /**
     * 删除该张单据的所有验收图片
     * @param refNum
     * @param refCodeId
     * @param isLocal
     */
    void deleteInspectionImages(String refNum, String refCodeId, boolean isLocal);

    /**
     * 删除该条验收明细下的所有图片
     * @param refNum
     * @param refLineNum
     * @param refLineId
     * @param isLocal
     */
    void deleteInspectionImagesSingle(String refNum, String refLineNum, String refLineId, boolean isLocal);

    /**
     * 拍照界面，删除指定的图片集合
     * @param images
     * @param isLocal
     * @return
     */
    boolean deleteTakedImages(ArrayList<ImageEntity> images, boolean isLocal);

    /**
     * 保存拍照获取的图片
     * @param images
     * @param refNum
     * @param refLineId
     * @param takePhotoType
     * @param imageDir
     * @param isLocal
     */
    void saveTakedImages(ArrayList<ImageEntity> images, String refNum, String refLineId, int takePhotoType,
                         String imageDir, boolean isLocal);

    /**
     * 读取该张验收单的所有图片信息
     * @param refNum
     * @param isLocal
     * @return
     */
    ArrayList<ImageEntity> readImagesByRefNum(String refNum, boolean isLocal);

    /**
     * 获取该库位下的仓库号
     * @param workId
     * @param workCode
     * @param invId
     * @param invCode
     * @return
     */
    String getStorageNum(String workId, String workCode, String invId, String invCode);

    /**
     * 获取仓库号列表
     * @param flag
     * @return
     */
    ArrayList<String> getStorageNumList(int flag);

    /**
     * 保存菜单信息
     * @param menus
     * @param loginId
     * @param mode
     */
    void saveMenuInfo(List<MenuNode> menus, String loginId, int mode);

    /**
     * 读取菜单信息
     * @param loginId
     * @param mode
     * @return
     */
    ArrayList<MenuNode> readMenuInfo(String loginId, int mode);


}