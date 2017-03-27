package com.richfit.data.repository.local;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.richfit.common_lib.scope.Type;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.BizFragmentConfig;
import com.richfit.domain.bean.ImageEntity;
import com.richfit.domain.bean.InvEntity;
import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.MaterialEntity;
import com.richfit.domain.bean.MenuNode;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.ResultEntity;
import com.richfit.domain.bean.RowConfig;
import com.richfit.domain.bean.SimpleEntity;
import com.richfit.domain.bean.UserEntity;
import com.richfit.domain.bean.WorkEntity;
import com.richfit.domain.repository.ILocalDataDao;
import com.richfit.domain.repository.ILocalRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.Flowable;

/**
 * Created by monday on 2016/12/29.
 */

public class LocalRepositoryImp implements ILocalRepository {
    private ILocalDataDao mCommonDao;
    private ILocalDataDao mApprovalDao;
    private ILocalDataDao mASDao;

    @Inject
    public LocalRepositoryImp(@Type("CommonDao") ILocalDataDao commonDao,
                              @Type("ApprovalDao") ILocalDataDao approvalDao,
                              @Type("ASDao") ILocalDataDao asDao) {
        this.mCommonDao = commonDao;
        this.mApprovalDao = approvalDao;
        this.mASDao = asDao;
    }

    @Override
    public Flowable<String> deleteCollectionData(String refNum, String transId, String refCodeId, String refType, String bizType, String userId, String companyCode) {
        return Flowable.just("删除成功");
    }


    @Override
    public Flowable<ReferenceEntity> getCheckInfo(String userId, String bizType, String checkLevel, String checkSpecial, String storageNum, String workId, String invId, String checkNum) {
        return null;
    }

    @Override
    public Flowable<String> deleteCheckData(String storageNum, String workId, String invId, String checkId,
                                            String userId, String bizType) {
        return null;
    }

    @Override
    public Flowable<List<InventoryEntity>> getCheckTransferInfoSingle(String checkId, String materialId, String materialNum, String location, String bizType) {
        return null;
    }

    @Override
    public Flowable<ReferenceEntity> getCheckTransferInfo(String checkId, String materialNum, String location, String isPageQuery, int pageNum, int pageSize, String bizType) {
        return null;
    }

    @Override
    public Flowable<String> deleteCheckDataSingle(String checkId, String checkLineId, String userId, String bizType) {
        return null;
    }

    @Override
    public Flowable<MaterialEntity> getMaterialInfo(String queryType, String materialNum) {
        return null;
    }

    @Override
    public Flowable<String> transferCheckData(String checkId, String userId, String bizType) {
        return null;
    }

    @Override
    public Flowable<ArrayList<String>> readUserInfo(String userName, String password) {
        UserEntity userEntity = new UserEntity();
        userEntity.userName = userName;
        userEntity.password = password;
        return Flowable.just(userEntity)
                .flatMap(data -> Flowable.just(mCommonDao.readUserInfo(data.userName, data.password)));
    }

    @Override
    public void saveUserInfo(UserEntity userEntity) {
        mCommonDao.saveUserInfo(userEntity);
    }

    @Override
    public void saveExtraConfigInfo(List<RowConfig> configs) {
        mCommonDao.saveExtraConfigInfo(configs);
    }

    @Override
    public Flowable<ArrayList<RowConfig>> readExtraConfigInfo(String companyId, String bizType, String refType,
                                                              String configType) {
        RowConfig config = new RowConfig();
        config.companyId = companyId;
        config.businessType = bizType;
        config.refType = refType;
        config.configType = configType;
        return Flowable.just(config).flatMap(data ->
                Flowable.just(mCommonDao.readExtraConfigInfo(data.companyId,
                        data.businessType, data.refType, data.configType)));
    }

    @Override
    public Flowable<Map<String, Object>> readExtraDataSourceByDictionary(String propertyCode, String dictionaryCode) {
        return Flowable.just(dictionaryCode).flatMap(code -> {
            Map<String, Object> source = mCommonDao.readExtraDataSourceByDictionary(propertyCode, code);
            Map<String, Object> map = new HashMap<>();
            map.put(UiUtil.MD5(propertyCode), source);
            return Flowable.just(map);
        });
    }

    @Override
    public String getLoadBasicDataTaskDate(@NonNull String queryType) {
        return mCommonDao.getLoadBasicDataTaskDate(queryType);
    }

    @Override
    public void saveLoadBasicDataTaskDate(@NonNull String queryType, @NonNull String queryDate) {
        mCommonDao.saveLoadBasicDataTaskDate(queryType, queryDate);
    }

    @Override
    public Flowable<Integer> saveBasicData(List<Map<String, Object>> maps) {
        return Flowable.just(maps).flatMap(data ->
                Flowable.just(mCommonDao.saveBasicData(data)));
    }

    @Override
    public void updateExtraConfigTable(Map<String, Set<String>> map) {
        mCommonDao.updateExtraConfigTable(map);
    }

    @Override
    public Flowable<ArrayList<InvEntity>> getInvsByWorkId(String workId, int flag) {
        return Flowable.just(workId)
                .flatMap(id -> {
                    ArrayList<InvEntity> invs = mCommonDao.getInvsByWorkId(id, flag);
                    if (invs == null || invs.size() == 0) {
                        return Flowable.error(new Throwable("未获到库存地点"));
                    }
                    return Flowable.just(invs);
                });
    }

    @Override
    public Flowable<ArrayList<WorkEntity>> getWorks(int flag) {
        return Flowable.just(flag).flatMap(type -> Flowable.just(mCommonDao.getWorks(type)));
    }

    @Override
    public Flowable<Boolean> checkWareHouseNum(String sendWorkId, String sendInvCode, String recWorkId,
                                               String recInvCode, int flag) {
        return Flowable.just(sendWorkId).flatMap(id -> {
            if (!mCommonDao.checkWareHouseNum(id, sendInvCode, recWorkId, recInvCode, flag)) {
                return Flowable.error(new Throwable("您选择的发出库位与接收库位不隶属于同一个ERP系统仓库号"));
            }
            return Flowable.just(true);
        });
    }

    @Override
    public Flowable<ArrayList<SimpleEntity>> getSupplierList(String workCode, String keyWord, int defaultItemNum, int flag) {
        return Flowable.just(keyWord)
                .flatMap(code -> {
                    ArrayList<SimpleEntity> list = mCommonDao.getSupplierList(workCode, code, defaultItemNum, flag);
                    if (list == null || list.size() == 0) {
                        return Flowable.error(new Throwable("未获取到供应商,请检查是否您选择的工厂是否正确或者是否在设置界面同步过供应商"));
                    }
                    return Flowable.just(list);
                });
    }

    @Override
    public Flowable<ArrayList<SimpleEntity>> getCostCenterList(String workCode, String keyWord, int defaultItemNum, int flag) {
        return Flowable.just(keyWord).flatMap(key -> {
            ArrayList<SimpleEntity> list = mCommonDao.getCostCenterList(workCode, key, defaultItemNum, flag);
            if (list == null || list.size() == 0) {
                return Flowable.error(new Throwable("未获取到成本中心,请检查是否您选择的工厂是否正确或者是否在设置界面同步过成本中心"));
            }
            return Flowable.just(list);
        });
    }

    @Override
    public Flowable<ArrayList<SimpleEntity>> getProjectNumList(String workCode, String keyWord, int defaultItemNum, int flag) {
        return Flowable.just(keyWord).flatMap(key -> {
            ArrayList<SimpleEntity> list = mCommonDao.getProjectNumList(workCode, key, defaultItemNum, flag);
            if (list == null || list.size() == 0) {
                return Flowable.error(new Throwable("未获取到项目编号,请检查是否您选择的工厂是否正确或者是否在设置界面同步过项目编号"));
            }
            return Flowable.just(list);
        });
    }

    @Override
    public Flowable<Boolean> saveBizFragmentConfig(ArrayList<BizFragmentConfig> bizFragmentConfigs) {
        return Flowable.just(bizFragmentConfigs)
                .flatMap(configs -> Flowable.just(mCommonDao.saveBizFragmentConfig(configs)));
    }

    @Override
    public Flowable<ArrayList<BizFragmentConfig>> readBizFragmentConfig(String bizType, String refType, int fragmentType) {
        return Flowable.just(bizType)
                .flatMap(type -> {
                    ArrayList<BizFragmentConfig> fragmentConfigs = mCommonDao.readBizFragmentConfig(type, refType, fragmentType);
                    if (fragmentConfigs == null || fragmentConfigs.size() == 0) {
                        return Flowable.error(new Throwable("未获取到配置信息"));
                    }
                    return Flowable.just(fragmentConfigs);
                });
    }

    @Override
    public void deleteInspectionImages(String refNum, String refCodeId, boolean isLocal) {
        mApprovalDao.deleteInspectionImages(refNum, refCodeId, isLocal);
    }

    @Override
    public void deleteInspectionImagesSingle(String refNum, String refLineNum, String refLineId, boolean isLocal) {
        mApprovalDao.deleteInspectionImagesSingle(refNum, refLineNum, refLineId, isLocal);
    }

    @Override
    public Flowable<String> deleteTakedImages(ArrayList<ImageEntity> images, boolean isLocal) {
        return Flowable.just(images).flatMap(imgs -> {
            boolean flag = mApprovalDao.deleteTakedImages(imgs, isLocal);
            if (!flag) {
                return Flowable.error(new Throwable("删除图片失败"));
            }
            return Flowable.just("删除图片成功");
        });
    }

    @Override
    public void saveTakedImages(ArrayList<ImageEntity> images, String refNum, String refLineId,
                                int takePhotoType, String imageDir, boolean isLocal) {
        mApprovalDao.saveTakedImages(images, refNum, refLineId, takePhotoType, imageDir, isLocal);
    }

    @Override
    public ArrayList<ImageEntity> readImagesByRefNum(String refNum, boolean isLocal) {
        return mApprovalDao.readImagesByRefNum(refNum, isLocal);
    }

    @Override
    public Flowable<String> getStorageNum(String workId, String workCode, String invId, String invCode) {
        return Flowable.just(workId)
                .flatMap(id->{
                    String storageNum = mCommonDao.getStorageNum(id, workCode, invId, invCode);
                    if (TextUtils.isEmpty(storageNum)) {
                        return Flowable.error(new Throwable("未获取到仓库号"));
                    }
                    return Flowable.just(storageNum);
                });
    }

    @Override
    public Flowable<ArrayList<String>> getStorageNumList(int flag) {
        return Flowable.just(flag)
                .flatMap(state -> {
                    final ArrayList<String> list = mCommonDao.getStorageNumList(state);
                    if (list == null || list.size() <= 1) {
                        return  Flowable.error(new Throwable("未查询到仓库列表"));
                    }
                    return Flowable.just(list);
                });
    }

    @Override
    public void saveMenuInfo(List<MenuNode> menus, String loginId, int mode) {
        mCommonDao.saveMenuInfo(menus, loginId, mode);
    }

    @Override
    public Flowable<ArrayList<MenuNode>> readMenuInfo(String loginId, int mode) {
        return Flowable.just(loginId)
                .flatMap(id->{
                    ArrayList<MenuNode> menuNodes = mCommonDao.readMenuInfo(id, mode);
                    if (menuNodes == null || menuNodes.size() == 0) {
                        return Flowable.error(new Throwable("未获取到菜单信息"));
                    }
                    return Flowable.just(menuNodes);
                });
    }

    /**
     * 通过bizType和refType给出Dao层的具体实现
     *
     * @param bizType
     * @return
     */
    private ILocalDataDao createDaoProxyFactory(String bizType) {
        switch (bizType) {
            case "12":
                //103-物资出库
                return mASDao;
            default:
                return mCommonDao;
        }
    }

    /**
     * 获取单据数据
     *
     * @param refNum：单号
     * @param refType:单据类型
     * @param bizType：业务类型
     * @param moveType:移动类型
     * @param refLineId:单据行Id,该参数在委外入库时用到
     * @param userId：用户loginId
     * @return
     */
    @Override
    public Flowable<ReferenceEntity> getReference(final String refNum, final String refType,
                                                  final String bizType, final String moveType,
                                                  final String refLineId, final String userId) {
        return Flowable.just(bizType)
                .flatMap(data -> Flowable.just(createDaoProxyFactory(data)
                        .getReference(refNum, refType, data, moveType, refLineId, userId)))
                .flatMap(refData -> processReferenceError(refData, "未获取到单据数据"));
    }


    /**
     * 保存单据数据。这里LocalRepositoryImp作为Controller，将不同的业务和单据类型的
     * 单据数据分别通过不同的Dao层保存到数据中去。
     *
     * @param refData:原始单据数据
     * @param bizType:业务类型
     * @param refType:单据类型
     */
    @Override
    public void saveReferenceInfo(ReferenceEntity refData, String bizType, String refType) {
        if (TextUtils.isEmpty(bizType)) {
            return;
        }
        createDaoProxyFactory(bizType).saveReferenceInfo(refData, bizType, refType);
    }

    /**
     * 获取单条缓存
     */
    @Override
    public Flowable<ReferenceEntity> getTransferInfo(String recordNum, String refCodeId, String bizType,
                                                     String refType, String userId, String workId, String invId, String recWorkId, String recInvId) {
        if (TextUtils.isEmpty(bizType)) {
            return Flowable.error(new Throwable("未获取到缓存"));
        }
        return Flowable.just(bizType)
                .flatMap(type -> Flowable.just(createDaoProxyFactory(type).getTransferInfo(recordNum, refCodeId, type,
                        refType, userId, workId, invId, recWorkId, recInvId)))
                .flatMap(refData -> processReferenceError(refData, "未获取到缓存"));
    }

    @Override
    public Flowable<ReferenceEntity> getTransferInfoSingle(String refCodeId, String refType, String bizType, String refLineId, String workId, String invId, String recWorkId, String recInvId, String materialNum,
                                                           String batchFlag, String location, String refDoc, int refDocItem, String userId) {
        if (TextUtils.isEmpty(bizType)) {
            return Flowable.error(new Throwable("未获取到缓存"));
        }
        return Flowable.just(bizType)
                .flatMap(type -> Flowable.just(createDaoProxyFactory(type).getTransferInfoSingle(refCodeId, refType,
                        type, refLineId, workId, invId, recWorkId, recInvId, materialNum, "", "", refDoc, refDocItem,
                        userId)))
                .flatMap(refData -> processReferenceError(refData, "未获取到缓存"));
    }

    @Override
    public Flowable<String> deleteCollectionDataSingle(String lineDeleteFlag, String transId, String transLineId, String locationId, String refType, String bizType, String refLineId, String userId, int position, String companyCode) {
        return null;
    }

    @Override
    public Flowable<String> uploadCollectionDataSingle(ResultEntity result) {
        if (TextUtils.isEmpty(result.businessType)) {
            return Flowable.error(new Throwable("保存出错,未获取到业务类型"));
        }
        return Flowable.just(result)
                .flatMap(res -> Flowable.just(createDaoProxyFactory(res.businessType).uploadCollectionDataSingle(res)))
                .flatMap(flag -> {
                    if (!flag.booleanValue()) {
                        return Flowable.error(new Throwable("保存出错"));
                    }
                    return Flowable.just("保存成功");
                });
    }

    /**
     * 统一处理读取单据数据可能发生的错误
     *
     * @param refData
     * @return
     */
    protected Flowable<ReferenceEntity> processReferenceError(ReferenceEntity refData, final String errorMsg) {
        if (refData == null || refData.billDetailList == null || refData.billDetailList.size() == 0) {
            return Flowable.error(new Throwable(errorMsg));
        }
        return Flowable.just(refData);
    }
}
