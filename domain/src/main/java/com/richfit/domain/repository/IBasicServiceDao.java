package com.richfit.domain.repository;

import android.support.annotation.NonNull;

import com.richfit.domain.bean.BizFragmentConfig;
import com.richfit.domain.bean.InvEntity;
import com.richfit.domain.bean.MaterialEntity;
import com.richfit.domain.bean.MenuNode;
import com.richfit.domain.bean.RowConfig;
import com.richfit.domain.bean.SimpleEntity;
import com.richfit.domain.bean.UserEntity;
import com.richfit.domain.bean.WorkEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 本地数据Dao需要实现的接口。该接口描述的是基础数据，用户登陆，
 * 模块菜单，页面配置等与本地数据库操作的协议
 * Created by monday on 2017/3/25.
 */

public interface IBasicServiceDao {

    /**
     * 下载额外字段的配置信息
     *
     * @param companyId:公司Id
     * @return
     */
    ArrayList<RowConfig> loadExtraConfig(String companyId);

    /**
     * 读取用户登陆信息
     *
     * @param userName
     * @param password
     * @return
     */
    ArrayList<String> readUserInfo(String userName, String password);

    UserEntity login(String userName, String password);

    /**
     * 保存用户登陆信息
     *
     * @param userEntity
     */
    void saveUserInfo(UserEntity userEntity);

    /**
     * 保存扩展字段配置信息
     *
     * @param configs
     */
    void saveExtraConfigInfo(List<RowConfig> configs);

    /**
     * 读取扩展字段配置信息
     *
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
     *
     * @param propertyCode
     * @param dictionaryCode
     * @return
     */
    Map<String, Object> readExtraDataSourceByDictionary(@NonNull String propertyCode, @NonNull String dictionaryCode);

    /**
     * 获取该类基础数据上一次下载的日期
     *
     * @param queryType
     * @return
     */
    String getLoadBasicDataTaskDate(String queryType);

    /**
     * 保存该类基础数据本次下载的日期
     *
     * @param queryTypes
     * @param queryDate
     */
    void saveLoadBasicDataTaskDate(String queryDate,List<String> queryTypes);

    /**
     * 保存基础数据
     *
     * @param maps
     * @return
     */
    int saveBasicData(List<Map<String, Object>> maps);

    /**
     * 跟新扩展字段基础数据表字段
     *
     * @param map
     */
    void updateExtraConfigTable(Map<String, Set<String>> map);

    /**
     * 获取该工厂下的所有库存地点
     *
     * @param workId
     * @param flag
     * @return
     */
    ArrayList<InvEntity> getInvsByWorkId(String workId, int flag);

    /**
     * 获取工厂列表
     *
     * @param flag
     * @return
     */
    ArrayList<WorkEntity> getWorks(int flag);


    /**
     * 检查接收库位和发出库位实发在同一个仓库
     *
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
     *
     * @param workCode
     * @param keyWord
     * @param defaultItemNum
     * @param flag
     * @return
     */
    ArrayList<SimpleEntity> getSupplierList(String workCode, String keyWord, int defaultItemNum, int flag);

    /**
     * 获取成本中心列表
     *
     * @param workCode
     * @param keyWord
     * @param defaultItemNum
     * @param flag
     * @return
     */
    ArrayList<SimpleEntity> getCostCenterList(String workCode, String keyWord, int defaultItemNum, int flag);

    /**
     * 获取项目好列表
     *
     * @param workCode
     * @param keyWord
     * @param defaultItemNum
     * @param flag
     * @return
     */
    ArrayList<SimpleEntity> getProjectNumList(String workCode, String keyWord, int defaultItemNum, int flag);

    /**
     * 保存页面配置信息
     *
     * @param bizFragmentConfigs
     * @return
     */
    boolean saveBizFragmentConfig(ArrayList<BizFragmentConfig> bizFragmentConfigs);

    /**
     * 读取页面配置信息
     *
     * @param bizType
     * @param refType
     * @param fragmentType
     * @return
     */
    ArrayList<BizFragmentConfig> readBizFragmentConfig(String bizType, String refType, int fragmentType, int mode);


    /**
     * 获取该库位下的仓库号
     *
     * @param workId
     * @param workCode
     * @param invId
     * @param invCode
     * @return
     */
    String getStorageNum(String workId, String workCode, String invId, String invCode);

    /**
     * 获取仓库号列表
     *
     * @param flag
     * @return
     */
    ArrayList<String> getStorageNumList(int flag);

    /**
     * 保存菜单信息
     *
     * @param menus
     * @param loginId
     * @param mode
     */
    ArrayList<MenuNode> saveMenuInfo(ArrayList<MenuNode> menus, String loginId, int mode);

    /**
     * 读取菜单信息
     *
     * @param loginId
     * @param mode
     * @return
     */
    ArrayList<MenuNode> getMenuInfo(String loginId, int mode);

    /**
     * 读取该用户离线模式的所有业务类型
     *
     * @param loginId
     * @return
     */
    ArrayList<MenuNode> readMenuInfo(String loginId);

    /**
     * 检查仓位是否存在
     *
     * @param queryType
     * @param workId
     * @param invId
     * @param storageNum
     * @param location
     * @return
     */
    boolean getLocationInfo(String queryType, String workId, String invId, String storageNum, String location);

    MaterialEntity getMaterialInfo(String queryType, String materialNum);
}