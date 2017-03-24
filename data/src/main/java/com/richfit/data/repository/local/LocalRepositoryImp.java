package com.richfit.data.repository.local;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.richfit.common_lib.scope.Type;
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
import com.richfit.domain.repository.ILocalRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.Flowable;

/**
 * Created by monday on 2016/12/29.
 */

public class LocalRepositoryImp implements ILocalRepository {


    private ILocalRepository mCommonDao;
    private ILocalRepository mApprovalDao;
    private ILocalRepository mASDao;

    @Inject
    public LocalRepositoryImp(@Type("CommonDao") ILocalRepository commonDao,
                              @Type("ApprovalDao") ILocalRepository approvalDao,
                              @Type("ASDao") ILocalRepository asDao) {
        this.mCommonDao = commonDao;
        this.mApprovalDao = approvalDao;
        this.mASDao = asDao;
    }

    @Override
    public Flowable<String> deleteCollectionData(String refNum, String transId, String refCodeId, String refType, String bizType, String userId, String companyCode) {
        return null;
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
        return mCommonDao.readUserInfo(userName, password);
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
    public Flowable<ArrayList<RowConfig>> readExtraConfigInfo(String companyId, String bizType, String refType, String configType) {
        return mCommonDao.readExtraConfigInfo(companyId, bizType, refType, configType);
    }

    @Override
    public Flowable<Map<String, Object>> readExtraDataSourceByDictionary(String propertyCode, String dictionaryCode) {
        return mCommonDao.readExtraDataSourceByDictionary(propertyCode, dictionaryCode);

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
        return mCommonDao.saveBasicData(maps);
    }

    @Override
    public void updateExtraConfigTable(Map<String, Set<String>> map) {
        mCommonDao.updateExtraConfigTable(map);
    }

    @Override
    public Flowable<ArrayList<InvEntity>> getInvsByWorkId(String workId, int flag) {
        return mCommonDao.getInvsByWorkId(workId, flag);
    }

    @Override
    public Flowable<ArrayList<WorkEntity>> getWorks(int flag) {
        return mCommonDao.getWorks(flag);
    }

    @Override
    public Flowable<Boolean> checkWareHouseNum(String sendWorkId, String sendInvCode, String recWorkId,
                                               String recInvCode, int flag) {
        return mCommonDao.checkWareHouseNum(sendWorkId, sendInvCode, recWorkId, recInvCode, flag);
    }

    @Override
    public Flowable<ArrayList<SimpleEntity>> getSupplierList(String workCode, String keyWord, int defaultItemNum, int flag) {
        return mCommonDao.getSupplierList(workCode, keyWord, defaultItemNum, flag);
    }

    @Override
    public Flowable<ArrayList<SimpleEntity>> getCostCenterList(String workCode, String keyWord, int defaultItemNum, int flag) {
        return mCommonDao.getCostCenterList(workCode, keyWord, defaultItemNum, flag);
    }

    @Override
    public Flowable<ArrayList<SimpleEntity>> getProjectNumList(String workCode, String keyWord, int defaultItemNum, int flag) {
        return mCommonDao.getProjectNumList(workCode, keyWord, defaultItemNum, flag);
    }

    @Override
    public Flowable<Boolean> saveBizFragmentConfig(ArrayList<BizFragmentConfig> bizFragmentConfigs) {
        return mCommonDao.saveBizFragmentConfig(bizFragmentConfigs);
    }

    @Override
    public Flowable<ArrayList<BizFragmentConfig>> readBizFragmentConfig(String bizType, String refType, int fragmentType) {
        return mCommonDao.readBizFragmentConfig(bizType, refType, fragmentType);
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
        return mApprovalDao.deleteTakedImages(images, isLocal);
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
        return mCommonDao.getStorageNum(workId, workCode, invId, invCode);
    }

    @Override
    public Flowable<ArrayList<String>> getStorageNumList(int flag) {
        return mCommonDao.getStorageNumList(flag);
    }

    @Override
    public void saveMenuInfo(List<MenuNode> menus, String loginId, int mode) {
        mCommonDao.saveMenuInfo(menus, loginId, mode);
    }

    @Override
    public Flowable<ArrayList<MenuNode>> readMenuInfo(String loginId, int mode) {
        return mCommonDao.readMenuInfo(loginId, mode);
    }

    /**
     * 通过bizType和refType给出Dao层的具体实现
     *
     * @param bizType
     * @return
     */
    private ILocalRepository createDaoProxyFactory(String bizType) {
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
        return Flowable.just(bizType).flatMap(businessType ->
                createDaoProxyFactory(businessType).getReference(refNum, refType, businessType, moveType, refLineId, userId))
                .flatMap(refData -> {
                    if (refData == null) {
                        return Flowable.error(new Throwable("未获取到单据数据"));
                    }
                    return Flowable.just(refData);
                });
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

    @Override
    public Flowable<ReferenceEntity> getTransferInfo(String recordNum, String refCodeId, String bizType,
                                                     String refType, String userId, String workId, String invId, String recWorkId, String recInvId) {
        if (TextUtils.isEmpty(bizType)) {
            return Flowable.error(new Throwable("未获取到缓存"));
        }
        return Flowable.just(bizType)
                .flatMap(type -> createDaoProxyFactory(type).getTransferInfo(recordNum, refCodeId, type,
                        refType, userId, workId, invId, recWorkId, recInvId));
    }

    @Override
    public Flowable<ReferenceEntity> getTransferInfoSingle(String refCodeId, String refType, String bizType, String refLineId, String workId, String invId, String recWorkId, String recInvId, String materialNum,
                                                           String batchFlag, String location, String refDoc, int refDocItem, String userId) {
        if (TextUtils.isEmpty(bizType)) {
            return Flowable.error(new Throwable("未获取到缓存"));
        }
        return Flowable.just(bizType)
                .flatMap(type -> createDaoProxyFactory(type).getTransferInfoSingle(refCodeId, refType,
                        type, refLineId, workId, invId, recWorkId, recInvId, materialNum, "", "", refDoc, refDocItem,
                        userId));
    }

    @Override
    public Flowable<String> deleteCollectionDataSingle(String lineDeleteFlag, String transId, String transLineId, String locationId, String refType, String bizType, String refLineId, String userId, int position, String companyCode) {
        return null;
    }

    @Override
    public Flowable<String> uploadCollectionDataSingle(ResultEntity result) {
        if(TextUtils.isEmpty(result.businessType)) {
            return Flowable.error(new Throwable("保存出错，未获取到业务类型"));
        }
        return Flowable.just(result)
                .flatMap(res -> createDaoProxyFactory(res.businessType).uploadCollectionDataSingle(res));
    }

}
