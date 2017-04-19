package com.richfit.data.repository.local;

import android.support.annotation.NonNull;
import android.text.TextUtils;

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
import com.richfit.domain.repository.IBasicServiceDao;
import com.richfit.domain.repository.IBusinessService;
import com.richfit.domain.repository.ICheckServiceDao;
import com.richfit.domain.repository.IInspectionServiceDao;
import com.richfit.domain.repository.ILocalRepository;
import com.richfit.domain.repository.IReferenceServiceDao;
import com.richfit.domain.repository.ITransferServiceDao;

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

    private IBasicServiceDao mBasicServiceDao;
    private IInspectionServiceDao mInspectionServiceDao;
    private IBusinessService mBusinessServiceDao;
    private IReferenceServiceDao mReferenceServiceDao;
    private ITransferServiceDao mTransferServiceDao;
    private ICheckServiceDao mCheckServiceDao;

    @Inject
    public LocalRepositoryImp(IBasicServiceDao basicServiceDao,
                              IInspectionServiceDao inspectionServiceDao,
                              IBusinessService businessServiceDao,
                              IReferenceServiceDao referenceServiceDao,
                              ITransferServiceDao transferServiceDao,
                              ICheckServiceDao checkServiceDao) {

        this.mBasicServiceDao = basicServiceDao;
        this.mInspectionServiceDao = inspectionServiceDao;
        this.mBusinessServiceDao = businessServiceDao;
        this.mReferenceServiceDao = referenceServiceDao;
        this.mTransferServiceDao = transferServiceDao;
        this.mCheckServiceDao = checkServiceDao;
    }


    @Override
    public Flowable<ReferenceEntity> getCheckInfo(String userId, String bizType, String checkLevel,
                                                  String checkSpecial, String storageNum, String workId,
                                                  String invId, String checkNum) {
        return Flowable.just(userId)
                .flatMap(id -> {
                    ReferenceEntity refData = mCheckServiceDao.getCheckInfo(id, bizType, checkLevel, checkSpecial,
                            storageNum, workId, invId, checkNum);
                    if (refData == null || TextUtils.isEmpty(refData.checkId)) {
                        return Flowable.error(new Throwable("盘点初始化失败"));
                    }
                    return Flowable.just(refData);
                });
    }

    @Override
    public Flowable<String> deleteCheckData(String storageNum, String workId, String invId, String checkId,
                                            String userId, String bizType) {
        return Flowable.just(userId)
                .flatMap(id -> {
                    if (mCheckServiceDao.deleteCheckData(storageNum, workId, invId, checkId, id, bizType)) {
                        return Flowable.just("删除成功");
                    } else {
                        return Flowable.error(new Throwable("删除失败"));
                    }
                });
    }

    @Override
    public Flowable<List<InventoryEntity>> getCheckTransferInfoSingle(String checkId, String materialId, String materialNum, String location, String bizType) {
        return Flowable.just(checkId)
                .flatMap(id -> {
                    List<InventoryEntity> list = mCheckServiceDao.getCheckTransferInfoSingle(id, materialId, materialNum, location, bizType);
                    if (list != null && list.size() > 0) {
                        return Flowable.just(list);
                    }
                    return Flowable.error(new Throwable("未获取到缓存"));
                });
    }

    @Override
    public Flowable<ReferenceEntity> getCheckTransferInfo(String checkId, String materialNum, String location, String isPageQuery, int pageNum, int pageSize, String bizType) {
        return Flowable.just(checkId)
                .flatMap(id -> {
                    ReferenceEntity refData = mCheckServiceDao.getCheckTransferInfo(id, materialNum, location, isPageQuery,
                            pageNum, pageSize, bizType);
                    if (refData.totalCount > 0) {
                        return Flowable.just(refData);
                    }

                    if (refData.totalCount <= 0 && refData.checkList != null && refData.checkList.size() > 0) {
                        return Flowable.just(refData);
                    }
                    return Flowable.error(new Throwable("未获取到盘点缓存数据"));
                });
    }

    @Override
    public Flowable<String> deleteCheckDataSingle(String checkId, String checkLineId, String userId, String bizType) {
        return Flowable.just(userId)
                .flatMap(id -> {
                    if (mCheckServiceDao.deleteCheckDataSingle(checkId, checkLineId, id, bizType)) {
                        return Flowable.just("删除成功");
                    } else {
                        return Flowable.error(new Throwable("删除失败"));
                    }
                });
    }

    @Override
    public Flowable<MaterialEntity> getMaterialInfo(String queryType, String materialNum) {
        return Flowable.just(queryType)
                .flatMap(type -> Flowable.just(mBasicServiceDao.getMaterialInfo(type, materialNum)));
    }

    @Override
    public Flowable<String> transferCheckData(String checkId, String userId, String bizType) {
        return null;
    }

    @Override
    public Flowable<String> getLocationInfo(String queryType, String workId, String invId, String storageNum, String location) {
        return Flowable.just(queryType)
                .flatMap(type -> {
                    if (mBasicServiceDao.getLocationInfo(type, workId, invId, storageNum, location)) {
                        return Flowable.just("仓位存在");
                    } else {
                        return Flowable.error(new Throwable("您输入的仓位不存在"));
                    }
                });
    }

    @Override
    public Flowable<ArrayList<MenuNode>> getMenuInfo(String loginId, int mode) {
        return Flowable.just(loginId)
                .flatMap(id -> {
                    ArrayList<MenuNode> list = mBasicServiceDao.getMenuInfo(id, mode);
                    if (list == null || list.size() == 0) {
                        return Flowable.error(new Throwable("未获取到用户菜单"));
                    }
                    return Flowable.just(list);
                });

    }

    @Override
    public Flowable<ArrayList<String>> readUserInfo(String userName, String password) {
        UserEntity userEntity = new UserEntity();
        userEntity.userName = userName;
        userEntity.password = password;
        return Flowable.just(userEntity)
                .flatMap(data -> Flowable.just(mBasicServiceDao.readUserInfo(data.userName, data.password)));
    }

    @Override
    public void saveUserInfo(UserEntity userEntity) {
        mBasicServiceDao.saveUserInfo(userEntity);
    }

    @Override
    public void saveExtraConfigInfo(List<RowConfig> configs) {
        mBasicServiceDao.saveExtraConfigInfo(configs);
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
                Flowable.just(mBasicServiceDao.readExtraConfigInfo(data.companyId,
                        data.businessType, data.refType, data.configType)));
    }

    @Override
    public Flowable<Map<String, Object>> readExtraDataSourceByDictionary(String propertyCode, String dictionaryCode) {
        return Flowable.just(dictionaryCode).flatMap(code -> {
            Map<String, Object> source = mBasicServiceDao.readExtraDataSourceByDictionary(propertyCode, code);
            Map<String, Object> map = new HashMap<>();
            map.put(UiUtil.MD5(propertyCode), source);
            return Flowable.just(map);
        });
    }

    @Override
    public String getLoadBasicDataTaskDate(@NonNull String queryType) {
        return mBasicServiceDao.getLoadBasicDataTaskDate(queryType);
    }

    @Override
    public void saveLoadBasicDataTaskDate(@NonNull String queryType, @NonNull String queryDate) {
        mBasicServiceDao.saveLoadBasicDataTaskDate(queryType, queryDate);
    }

    @Override
    public Flowable<Integer> saveBasicData(List<Map<String, Object>> maps) {
        return Flowable.just(maps).flatMap(data ->
                Flowable.just(mBasicServiceDao.saveBasicData(data)));
    }

    @Override
    public void updateExtraConfigTable(Map<String, Set<String>> map) {
        mBasicServiceDao.updateExtraConfigTable(map);
    }

    @Override
    public Flowable<ArrayList<InvEntity>> getInvsByWorkId(String workId, int flag) {
        return Flowable.just(workId)
                .flatMap(id -> {
                    ArrayList<InvEntity> invs = mBasicServiceDao.getInvsByWorkId(id, flag);
                    if (invs == null || invs.size() == 0) {
                        return Flowable.error(new Throwable("未获到库存地点"));
                    }
                    return Flowable.just(invs);
                });
    }

    @Override
    public Flowable<ArrayList<WorkEntity>> getWorks(int flag) {
        return Flowable.just(flag).flatMap(type -> Flowable.just(mBasicServiceDao.getWorks(type)));
    }

    @Override
    public Flowable<Boolean> checkWareHouseNum(String sendWorkId, String sendInvCode, String recWorkId,
                                               String recInvCode, int flag) {
        return Flowable.just(sendWorkId).flatMap(id -> {
            if (!mBasicServiceDao.checkWareHouseNum(id, sendInvCode, recWorkId, recInvCode, flag)) {
                return Flowable.error(new Throwable("您选择的发出库位与接收库位不隶属于同一个ERP系统仓库号"));
            }
            return Flowable.just(true);
        });
    }

    @Override
    public Flowable<ArrayList<SimpleEntity>> getSupplierList(String workCode, String keyWord, int defaultItemNum, int flag) {
        return Flowable.just(keyWord)
                .flatMap(code -> {
                    ArrayList<SimpleEntity> list = mBasicServiceDao.getSupplierList(workCode, code, defaultItemNum, flag);
                    if (list == null || list.size() == 0) {
                        return Flowable.error(new Throwable("未获取到供应商,请检查是否您选择的工厂是否正确或者是否在设置界面同步过供应商"));
                    }
                    return Flowable.just(list);
                });
    }

    @Override
    public Flowable<ArrayList<SimpleEntity>> getCostCenterList(String workCode, String keyWord, int defaultItemNum, int flag) {
        return Flowable.just(keyWord).flatMap(key -> {
            ArrayList<SimpleEntity> list = mBasicServiceDao.getCostCenterList(workCode, key, defaultItemNum, flag);
            if (list == null || list.size() == 0) {
                return Flowable.error(new Throwable("未获取到成本中心,请检查是否您选择的工厂是否正确或者是否在设置界面同步过成本中心"));
            }
            return Flowable.just(list);
        });
    }

    @Override
    public Flowable<ArrayList<SimpleEntity>> getProjectNumList(String workCode, String keyWord, int defaultItemNum, int flag) {
        return Flowable.just(keyWord).flatMap(key -> {
            ArrayList<SimpleEntity> list = mBasicServiceDao.getProjectNumList(workCode, key, defaultItemNum, flag);
            if (list == null || list.size() == 0) {
                return Flowable.error(new Throwable("未获取到项目编号,请检查是否您选择的工厂是否正确或者是否在设置界面同步过项目编号"));
            }
            return Flowable.just(list);
        });
    }

    @Override
    public Flowable<Boolean> saveBizFragmentConfig(ArrayList<BizFragmentConfig> bizFragmentConfigs) {
        return Flowable.just(bizFragmentConfigs)
                .flatMap(configs -> Flowable.just(mBasicServiceDao.saveBizFragmentConfig(configs)));
    }

    @Override
    public Flowable<ArrayList<BizFragmentConfig>> readBizFragmentConfig(String bizType, String refType, int fragmentType, int mode) {
        return Flowable.just(bizType)
                .flatMap(type -> {
                    ArrayList<BizFragmentConfig> fragmentConfigs = mBasicServiceDao.readBizFragmentConfig(type, refType, fragmentType, mode);
                    if (fragmentConfigs == null || fragmentConfigs.size() == 0) {
                        return Flowable.error(new Throwable("未获取到配置信息"));
                    }
                    return Flowable.just(fragmentConfigs);
                });
    }

    @Override
    public void deleteInspectionImages(String refNum, String refCodeId, boolean isLocal) {
        mInspectionServiceDao.deleteInspectionImages(refNum, refCodeId, isLocal);
    }

    @Override
    public void deleteInspectionImagesSingle(String refNum, String refLineNum, String refLineId, boolean isLocal) {
        mInspectionServiceDao.deleteInspectionImagesSingle(refNum, refLineNum, refLineId, isLocal);
    }

    @Override
    public Flowable<String> deleteTakedImages(ArrayList<ImageEntity> images, boolean isLocal) {
        return Flowable.just(images).flatMap(imgs -> {
            boolean flag = mInspectionServiceDao.deleteTakedImages(imgs, isLocal);
            if (!flag) {
                return Flowable.error(new Throwable("删除图片失败"));
            }
            return Flowable.just("删除图片成功");
        });
    }

    @Override
    public void saveTakedImages(ArrayList<ImageEntity> images, String refNum, String refLineId,
                                int takePhotoType, String imageDir, boolean isLocal) {
        mInspectionServiceDao.saveTakedImages(images, refNum, refLineId, takePhotoType, imageDir, isLocal);
    }

    @Override
    public ArrayList<ImageEntity> readImagesByRefNum(String refNum, boolean isLocal) {
        return mInspectionServiceDao.readImagesByRefNum(refNum, isLocal);
    }

    @Override
    public Flowable<String> getStorageNum(String workId, String workCode, String invId, String invCode) {
        return Flowable.just(workId)
                .flatMap(id -> {
                    String storageNum = mBasicServiceDao.getStorageNum(id, workCode, invId, invCode);
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
                    final ArrayList<String> list = mBasicServiceDao.getStorageNumList(state);
                    if (list == null || list.size() <= 1) {
                        return Flowable.error(new Throwable("未查询到仓库列表"));
                    }
                    return Flowable.just(list);
                });
    }

    @Override
    public ArrayList<MenuNode> saveMenuInfo(ArrayList<MenuNode> menus, String loginId, int mode) {
        return mBasicServiceDao.saveMenuInfo(menus, loginId, mode);
    }

    @Override
    public Flowable<UserEntity> Login(String userName, String password) {
        return Flowable.just(userName)
                .flatMap(name -> Flowable.just(mBasicServiceDao.login(name, password)));
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
                .flatMap(type -> Flowable.just(getReferenceInfoInner(refNum, refType, type, moveType, refLineId, userId)))
                .flatMap(refData -> processReferenceError(refData, bizType, "未获取到单据数据"));
    }

    private ReferenceEntity getReferenceInfoInner(String refNum, String refType, String bizType,
                                                  String moveType, String refLineId, String userId) {
        ReferenceEntity refData = null;
        switch (refType) {
            case "0":// 采购订单-(20N0||8200)
                refData = mReferenceServiceDao.getPoInfo(refNum, refType, bizType, moveType, refLineId, userId);
                break;
            case "1":// 验收清单(8200)
                refData = mReferenceServiceDao.getInspectionInfo(refNum, refType, bizType, moveType, refLineId, userId);
                break;
            case "4":// 交货单(20N0)
                refData = mReferenceServiceDao.getDeliveryInfo(refNum, refType, bizType, moveType, refLineId, userId);
                break;
            case "10":// 预留单据(8200)
                refData = mReferenceServiceDao.getReservationInfo(refNum, refType, bizType, moveType, refLineId, userId);
                break;
        }
        return refData;
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
        switch (refType) {
            case "0":// 采购订单-(20N0||8200)
                mReferenceServiceDao.savePoInfo(refData, bizType, refType);
                break;
            case "1":// 验收清单(8200)
                mReferenceServiceDao.saveInspectionInfo(refData, bizType, refType);
                break;
            case "4":// 交货单(20N0)
                mReferenceServiceDao.saveDeliveryInfo(refData, bizType, refType);
                break;
            case "10":// 预留单据(8200)
                mReferenceServiceDao.saveReservationInfo(refData, bizType, refType);
                break;
        }
    }

    @Override
    public Flowable<ArrayList<MenuNode>> readMenuInfo(String loginId) {
        return Flowable.just(loginId)
                .map(id -> mBasicServiceDao.readMenuInfo(id));
    }

    @Override
    public Flowable<List<ReferenceEntity>> readTransferedData() {
        return Flowable.just(1)
                .flatMap(a -> {
                    List<ReferenceEntity> datas = mBusinessServiceDao.readTransferedData();
                    if (datas.size() == 0) {
                        return Flowable.error(new Throwable("您还未采集过数据"));
                    }
                    return Flowable.just(datas);
                });
    }

    @Override
    public void deleteOfflineDataAfterUploadSuccess(String transId, String bizType, String refType, String userId) {
        mBusinessServiceDao.deleteOfflineDataAfterUploadSuccess(transId, bizType, refType, userId);
    }

    @Override
    public Flowable<String> setTransFlag(String transId) {
        if (TextUtils.isEmpty(transId)) {
            return Flowable.error(new Throwable("修改失败"));
        }
        return Flowable.just(transId)
                .flatMap(id -> {
                    boolean flag = mBusinessServiceDao.setTransFlag(id);
                    if (flag) {
                        return Flowable.just("修改成功");
                    }
                    return Flowable.error(new Throwable("修改失败"));
                });
    }


    /**
     * 获取单条缓存
     */
    @Override
    public Flowable<ReferenceEntity> getTransferInfo(String recordNum, String refCodeId, String bizType,
                                                     String refType, String userId, String workId,
                                                     String invId, String recWorkId, String recInvId) {
        if (TextUtils.isEmpty(bizType)) {
            return Flowable.error(new Throwable("未获取到缓存"));
        }
        return Flowable.just(bizType)
                .flatMap(type -> Flowable.just(getTransferInfoInner(recordNum, refCodeId, type,
                        refType, userId, workId, invId, recWorkId, recInvId)))
                .flatMap(refData -> processReferenceError(refData, bizType, "未获取到缓存"));
    }

    private ReferenceEntity getTransferInfoInner(String recordNum, String refCodeId, String businessType,
                                                 String refType, String userId, String workId, String invId,
                                                 String recWorkId, String recInvId) {

        ReferenceEntity refData = null;
        switch (businessType) {
            case "11":// 采购入库-101
            case "12":// 采购入库-103
            case "13":// 采购入库-105(非必检)
            case "19":// 委外入库
            case "19_ZJ":// 委外入库-组件
            case "110":// 采购入库-105(必检)
            case "21":// 销售出库
            case "23":// 委外发料
            case "24":// 其他出库-有参考
            case "38":// UB 351
            case "311":// UB 101
            case "45":// UB 352
            case "51":// 采购退货
                refData = mTransferServiceDao.getBusinessTransferInfoRef(recordNum, refCodeId, businessType, refType,
                        userId, workId, invId, recWorkId, recInvId);
                break;
            case "16":// 其他入库-无参考
            case "25":// 其他出库-无参考
            case "26":// 无参考-201
            case "27":// 无参考-221
            case "32":// 301(无参考)
            case "34":// 311(无参考)
            case "44":// 其他退库-无参考
            case "46":// 无参考-202
            case "47":// 无参考-222
            case "71":// 代管料入库
            case "72":// 代管料出库
            case "73":// 代管料退库
            case "74":// 代管料调拨
            case "91":// 代管料入库-HRM
            case "92":// 代管料出库-HRM
            case "93":// 代管料退库-HRM
            case "94":// 代管料调拨-HRM
                refData = mTransferServiceDao.getBusinessTransferInfo(recordNum, refCodeId, businessType, refType,
                        userId, workId, invId, recWorkId, recInvId);
                break;
        }
        return refData;
    }

    @Override
    public Flowable<ReferenceEntity> getTransferInfoSingle(String refCodeId, String refType, String bizType, String refLineId, String workId, String invId, String recWorkId, String recInvId, String materialNum,
                                                           String batchFlag, String location, String refDoc, int refDocItem, String userId) {
        if (TextUtils.isEmpty(bizType)) {
            return Flowable.error(new Throwable("未获取到缓存"));
        }
        return Flowable.just(bizType)
                .flatMap(type -> Flowable.just(getTransferInfoSingleInner(refCodeId, refType,
                        type, refLineId, workId, invId, recWorkId, recInvId, materialNum, "", "", refDoc, refDocItem,
                        userId)))
                .flatMap(refData -> processReferenceError(refData, bizType, "未获取到缓存"));
    }

    private ReferenceEntity getTransferInfoSingleInner(String refCodeId, String refType, String bizType, String refLineId, String workId, String invId, String recWorkId, String recInvId, String materialNum,
                                                       String batchFlag, String location, String refDoc, int refDocItem, String userId) {

        ReferenceEntity refData = null;
        switch (bizType) {
            case "00":
            case "01":
                refData = mTransferServiceDao.getInspectTransferInfoSingle(refCodeId, refType, bizType, refLineId, workId, invId, recWorkId, recInvId,
                        materialNum, batchFlag, location, refDoc, refDocItem, userId);
                break;
            case "11":// 采购入库-101
            case "12":// 采购入库-103
            case "13":// 采购入库-105(非必检)
            case "19":// 委外入库
            case "19_ZJ":// 委外入库-组件
            case "110":// 采购入库-105(青海必检)
            case "21":// 销售出库
            case "23":// 委外发料
            case "24":// 其他出库-有参考
            case "38":// UB 351
            case "311":// UB 101
            case "45":// UB 352
            case "51":// 采购退货-161
                refData = mTransferServiceDao.getBusinessTransferInfoSingleRef(refCodeId, refType, bizType, refLineId, workId, invId, recWorkId, recInvId,
                        materialNum, batchFlag, location, refDoc, refDocItem, userId);
                break;
            case "16":// 其他入库-无参考
            case "25":// 其他出库-无参考
            case "26":// 无参考-201
            case "27":// 无参考-221
            case "32":// 301(无参考)
            case "34":// 311(无参考)
            case "44":// 其他退库-无参考
            case "46":// 无参考-202
            case "47":// 无参考-222
            case "71":// 代管料入库
            case "72":// 代管料出库
            case "73":// 代管料退库
            case "74":// 代管料调拨
            case "91":// 代管料入库-HRM
            case "92":// 代管料出库-HRM
            case "93":// 代管料退库-HRM
            case "94":// 代管料调拨-HRM
                refData = mTransferServiceDao.getBusinessTransferInfoSingle(refCodeId, refType, bizType, refLineId, workId, invId, recWorkId, recInvId,
                        materialNum, batchFlag, location, refDoc, refDocItem, userId);
                break;
        }
        return refData;
    }

    @Override
    public Flowable<String> deleteCollectionDataSingle(String lineDeleteFlag, String transId, String transLineId, String locationId,
                                                       String refType, String bizType, String refLineId, String userId, int position, String companyCode) {

        if (TextUtils.isEmpty(bizType)) {
            return Flowable.error(new Throwable("删除失败"));
        }

        return Flowable.just(bizType)
                .flatMap(businessType -> {
                    boolean isDeleteSuccess = deleteCollectDataSingleInner(lineDeleteFlag, transId, transLineId,
                            locationId, refType, businessType, refLineId, userId, position, companyCode);
                    if (!isDeleteSuccess) {
                        return Flowable.error(new Throwable("删除失败"));
                    } else {
                        return Flowable.just("删除成功");
                    }
                });
    }

    private boolean deleteCollectDataSingleInner(String lineDeleteFlag, String transId, String transLineId, String locationId,
                                                 String refType, String bizType, String refLineId, String userId, int position, String companyCode) {
        boolean isDeleteSuccess;
        switch (bizType) {
            case "00":
            case "01":
                isDeleteSuccess = mInspectionServiceDao.deleteInspectionByLineId(refLineId);
                break;
            default:
                isDeleteSuccess = "Y".equals(lineDeleteFlag) ? mBusinessServiceDao.deleteBusinessDataByLineId(bizType, transId, transLineId)
                        : mBusinessServiceDao.deleteBusinessDataByLocationId(locationId, transId, transLineId);
                break;
        }
        return isDeleteSuccess;
    }

    @Override
    public Flowable<String> deleteCollectionData(String refNum, String transId, String refCodeId,
                                                 String refType, String bizType, String userId,
                                                 String companyCode) {

        return Flowable.just(refNum)
                .flatMap(recordNum -> {
                    boolean isDeleteSuccess = deleteCollectionDataInner(recordNum, transId, refCodeId, refType,
                            bizType, userId, companyCode);
                    if (isDeleteSuccess) {
                        return Flowable.just("删除成功");
                    } else {
                        return Flowable.error(new Throwable("删除失败"));
                    }
                });
    }

    private boolean deleteCollectionDataInner(String refNum, String transId, String refCodeId, String refType,
                                              String bizType, String userId, String companyCode) {

        if (TextUtils.isEmpty(bizType)) {
            return false;
        }
        boolean isDeleteSuccess;
        switch (bizType) {
            case "00":
            case "01":
                isDeleteSuccess = mInspectionServiceDao.deleteInspectionByHeadId(refCodeId);
                break;
            case "11":// 采购入库-101
            case "12":// 采购入库-103
            case "13":// 采购入库-105(非必检)
            case "19":// 委外入库
            case "110":// 采购入库-105(青海必检)
            case "21":// 销售出库
            case "23":// 委外发料
            case "24":// 其他出库-有参考
            case "38":// UB 351
            case "311":// UB 101
            case "45":// UB 352
            case "51":// 采购退货-161
                isDeleteSuccess = mBusinessServiceDao.deleteBusinessData(refNum, transId, refCodeId, refType, bizType, userId, companyCode);
                break;
            case "16":// 其他入库-无参考
            case "25":// 其他出库-无参考
            case "26":// 无参考-201
            case "27":// 无参考-221
            case "32":// 301(无参考)
            case "34":// 311(无参考)
            case "44":// 其他退库-无参考
            case "46":// 无参考-202
            case "47":// 无参考-222
            case "71":// 代管料入库
            case "72":// 代管料出库
            case "73":// 代管料退库
            case "74":// 代管料调拨
            case "91":// 代管料入库-HRM
            case "92":// 代管料出库-HRM
            case "93":// 代管料退库-HRM
            case "94":// 代管料调拨-HRM
                isDeleteSuccess = mBusinessServiceDao.deleteBusinessData(refNum, transId, refCodeId, refType, bizType, userId, companyCode);
                break;
            default:
                isDeleteSuccess = false;
                break;
        }
        return isDeleteSuccess;

    }

    @Override
    public Flowable<String> uploadCollectionDataSingle(ResultEntity result) {
        if (TextUtils.isEmpty(result.businessType)) {
            return Flowable.error(new Throwable("保存出错,未获取到业务类型"));
        }
        return Flowable.just(result)
                .flatMap(res -> Flowable.just(uploadCollectionDataSingleInner(res)))
                .flatMap(flag -> {
                    if (!flag.booleanValue()) {
                        return Flowable.error(new Throwable("保存出错"));
                    }
                    return Flowable.just("保存成功");
                });
    }

    @Override
    public Flowable<String> uploadCheckDataSingle(ResultEntity result) {
        return Flowable.just(result)
                .flatMap(res -> {
                    if (mCheckServiceDao.uploadCheckDataSingle(res)) {
                        return Flowable.just("盘点成功");
                    } else {
                        return Flowable.error(new Throwable("盘点失败"));
                    }
                });
    }

    private boolean uploadCollectionDataSingleInner(ResultEntity param) {
        boolean isSuccess = false;
        // 接收传入参数
        switch (param.businessType) {
            case "00":
            case "01":
                isSuccess = mInspectionServiceDao.uploadInspectionDataSingle(param);
                break;
            case "11":// 采购入库-101
            case "12":// 采购入库-103
            case "13":// 采购入库-105(非必检)
            case "19":// 委外入库
            case "19_ZJ":// 委外入库-组件
            case "110":// 采购入库-105(青海必检)
            case "21":// 销售出库
            case "23":// 委外发料
            case "24":// 其他出库-有参考
            case "38":// UB 351
            case "311":// UB 101
            case "45":// UB 352
            case "51":// 采购退货-161
                isSuccess = mBusinessServiceDao.uploadBusinessDataSingle(param);
                break;
            case "16":// 其他入库-无参考
            case "25":// 其他出库-无参考
            case "26":// 无参考-201
            case "27":// 无参考-221
            case "32":// 301(无参考)
            case "34":// 311(无参考)
            case "44":// 其他退库-无参考
            case "46":// 无参考-202
            case "47":// 无参考-222
            case "71":// 代管料入库
            case "72":// 代管料出库
            case "73":// 代管料退库
            case "74":// 代管料调拨
            case "91":// 代管料入库-HRM
            case "92":// 代管料出库-HRM
            case "93":// 代管料退库-HRM
            case "94":// 代管料调拨-HRM
                isSuccess = mBusinessServiceDao.uploadBusinessDataSingle(param);
                break;
        }
        return isSuccess;
    }

    /**
     * 统一处理读取单据数据可能发生的错误
     *
     * @param refData
     * @return
     */
    protected Flowable<ReferenceEntity> processReferenceError(ReferenceEntity refData, final String bizType,
                                                              final String errorMsg) {
        switch (bizType) {
            //针对验收处理
            case "01":
                if (refData == null || refData.billDetailList == null) {
                    return Flowable.error(new Throwable(errorMsg));
                }
                return Flowable.just(refData);

            default:
                if (refData == null || refData.billDetailList == null || refData.billDetailList.size() == 0) {
                    return Flowable.error(new Throwable(errorMsg));
                }
                return Flowable.just(refData);
        }
    }
}
