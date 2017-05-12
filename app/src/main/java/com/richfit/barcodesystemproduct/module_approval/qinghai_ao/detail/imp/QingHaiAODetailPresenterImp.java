package com.richfit.barcodesystemproduct.module_approval.qinghai_ao.detail.imp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.richfit.barcodesystemproduct.base.base_detail.BaseDetailPresenterImp;
import com.richfit.barcodesystemproduct.module.edit.EditActivity;
import com.richfit.barcodesystemproduct.module_approval.qinghai_ao.detail.IQingHaiAODetailPresenter;
import com.richfit.barcodesystemproduct.module_approval.qinghai_ao.detail.IQingHaiAODetailView;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.FileUtil;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.ImageEntity;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.ResultEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by monday on 2017/2/28.
 */

public class QingHaiAODetailPresenterImp extends BaseDetailPresenterImp<IQingHaiAODetailView>
        implements IQingHaiAODetailPresenter {

    @Inject
    public QingHaiAODetailPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void getReference(ReferenceEntity data, String refNum, String refType, String bizType, String moveType, String refLineId, String userId) {

        mView = getView();

        ResourceSubscriber<ReferenceEntity> subscriber =
                mRepository.getReference(refNum, refType, bizType, moveType, refLineId, userId)
                        .filter(refData -> refData != null && refData.billDetailList != null && refData.billDetailList.size() > 0)
                        .map(refData -> addTreeInfo(refData))
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new ResourceSubscriber<ReferenceEntity>() {
                            @Override
                            public void onNext(ReferenceEntity refData) {
                                if (mView != null) {
                                    mView.showNodes(refData.billDetailList, refData.transId);
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                if (mView != null) {
                                    mView.setRefreshing(false, "获取明细失败" + t.getMessage());
                                    //展示抬头获取的数据，没有缓存
                                    mView.showNodes(data.billDetailList, data.transId);
                                }
                            }

                            @Override
                            public void onComplete() {
                                if (mView != null) {
                                    mView.setRefreshing(true, "获取明细成功");
                                }
                            }
                        });
        addSubscriber(subscriber);
    }

    @Override
    public void deleteNode(String lineDeleteFlag, String refNum, String refLineNum, String refLineId,
                           String refType, String bizType, String userId, int position, String companyCode,boolean isLocal) {
        mView = getView();

        RxSubscriber<String> subscriber =
                mRepository.deleteCollectionDataSingle("", "", "", "", refType, bizType, refLineId,
                        userId, position, companyCode)
                        .doOnNext(str -> mRepository.deleteInspectionImagesSingle(refNum, refLineNum, refLineId, isLocal))
                        .doOnNext(str -> FileUtil.deleteDir(FileUtil.getImageCacheDir(mContext.getApplicationContext(), refNum, refLineNum, isLocal)))
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new RxSubscriber<String>(mContext) {
                            @Override
                            public void _onNext(String s) {

                            }

                            @Override
                            public void _onNetWorkConnectError(String message) {

                            }

                            @Override
                            public void _onCommonError(String message) {
                                if (mView != null) {
                                    mView.deleteNodeFail(message);
                                }
                            }

                            @Override
                            public void _onServerError(String code, String message) {
                                if (mView != null) {
                                    mView.deleteNodeFail(message);
                                }
                            }

                            @Override
                            public void _onComplete() {
                                if (mView != null) {
                                    mView.deleteNodeSuccess(position);
                                }
                            }
                        });
        addSubscriber(subscriber);
    }

    @Override
    public void editNode(ArrayList<String> sendLocations, ArrayList<String> recLocations,
                         ReferenceEntity refData,RefDetailEntity node, String companyCode, String bizType, String refType,
                         String subFunName, int position) {
        Intent intent = new Intent(mContext, EditActivity.class);
        Bundle bundle = new Bundle();

        bundle.putInt(Global.EXTRA_POSITION_KEY, position);

        //入库子菜单类型
        bundle.putString(Global.EXTRA_BIZ_TYPE_KEY, bizType);
        bundle.putString(Global.EXTRA_REF_TYPE_KEY, refType);

        //入库的子菜单的名称
        bundle.putString(Global.EXTRA_TITLE_KEY, subFunName + "-明细修改");
        //库存地点
        bundle.putString(Global.EXTRA_INV_CODE_KEY, node.invCode);
        bundle.putString(Global.EXTRA_INV_ID_KEY, node.invId);

        //制造商
        bundle.putString(Global.EXTRA_MANUFUCTURER_KEY, node.manufacturer);
        //实收数量(注意这里没有仓位级，所以实收数量为totalQuantity)
        bundle.putString(Global.EXTRA_QUANTITY_KEY, node.totalQuantity);
        //抽检数量
        bundle.putString(Global.EXTRA_SAMPLE_QUANTITY_KEY, node.randomQuantity);
        //完好数量
        bundle.putString(Global.EXTRA_QUALIFIED_QUANTITY_KEY, node.qualifiedQuantity);
        //损坏数量
        bundle.putString(Global.EXTRA_DAMAGED_QUANTITY_KEY, node.damagedQuantity);
        //送检数量
        bundle.putString(Global.EXTRA_INSPECTION_QUANTITY_KEY, node.inspectionQuantity);
        //锈蚀数量
        bundle.putString(Global.EXTRA_RUST_QUANTITY_KEY, node.rustQuantity);
        //变质
        bundle.putString(Global.EXTRA_BAD_QUANTITY_KEY, node.badQuantity);
        //其他数量
        bundle.putString(Global.EXTRA_OTHER_QUANTITY_KEY, node.otherQuantity);
        //包装情况
        bundle.putString(Global.EXTRA_PACKAGE_KEY, node.sapPackage);
        //质检单号
        bundle.putString(Global.EXTRA_QM_NUM_KEY, node.qmNum);
        //索赔单号
        bundle.putString(Global.EXTRA_CLAIM_NUM_KEY, node.claimNum);
        //合格证
        bundle.putString(Global.EXTRA_CERTIFICATE_KEY, node.certificate);
        //说明书
        bundle.putString(Global.EXTRA_INSTRUCTIONS_KEY, node.instructions);
        //质检证书
        bundle.putString(Global.EXTRA_QM_CERTIFICATE_KEY, node.qmCertificate);
        //检验结果
        bundle.putString(Global.EXTRA_INSPECTION_RESULT_KEY, node.inspectionResult);
        intent.putExtras(bundle);
        Activity activity = (Activity) mContext;
        activity.startActivity(intent);
    }

    /**
     * 过账验收数据，逻辑是只有是所有步骤都成功了才能够删除图片，任何一步失败了都得从新开始。
     *
     * @param refNum
     * @param refCodeId
     * @param transId
     * @param bizType
     * @param refType
     * @param inspectionType
     * @param userId
     * @param isLocal
     * @param voucherDate
     * @param transToSapFlag
     * @param extraHeaderMap
     */
    @Override
    public void transferCollectionData(String refNum, String refCodeId, String transId,
                                       String bizType, String refType, int inspectionType, String userId,
                                       boolean isLocal, String voucherDate,
                                       String transToSapFlag, Map<String, Object> extraHeaderMap) {
        mView = getView();

        addSubscriber(Flowable.concat(uploadInspectedImages(refNum, refCodeId, transId, userId, "01", isLocal),
                uploadInspectedImages(refNum, refCodeId, transId, userId, "02", isLocal),
                mRepository.transferCollectionData(transId, bizType, refType, userId, voucherDate, transToSapFlag, extraHeaderMap))
                .doOnComplete(() -> mRepository.deleteInspectionImages(refNum, refCodeId, isLocal))
                .doOnComplete(() -> FileUtil.deleteDir(FileUtil.getImageCacheDir(mContext.getApplicationContext(), refNum, false)))
                .compose(TransformerHelper.io2main())
                .subscribeWith(new RxSubscriber<String>(mContext, "正在过账...") {

                    @Override
                    public void _onNext(String transNum) {
                        if (mView != null) {
                            mView.showTransferedVisa(transNum);
                        }
                    }

                    @Override
                    public void _onNetWorkConnectError(String message) {
                        if (mView != null) {
                            mView.submitBarcodeSystemFail(message);
                        }
                    }

                    @Override
                    public void _onCommonError(String message) {
                        if (mView != null) {
                            mView.submitBarcodeSystemFail(message);
                        }
                    }

                    @Override
                    public void _onServerError(String code, String message) {
                        if (mView != null) {
                            mView.submitBarcodeSystemFail(message);
                        }
                    }

                    @Override
                    public void _onComplete() {
                        if (mView != null) {
                            mView.submitBarcodeSystemSuccess();
                        }
                    }
                }));
    }

    /**
     * 上传图片。如果没有图片直接过滤掉。
     */
    private Flowable<String> uploadInspectedImages(String refNum, String refCodeId, String transId,
                                                   String userId, String transFileToServer, boolean isLocal) {
       
    
        return Flowable.just(refNum)
                .map(num -> mRepository.readImagesByRefNum(refNum, isLocal))
                .filter(images -> images != null && images.size() > 0)
                .flatMap(images -> Flowable.fromIterable(wrapperImage(images, refCodeId, transId, userId, transFileToServer)))
                .buffer(3)
                .flatMap(results -> mRepository.uploadMultiFiles(results));
    }

    /**
     * 图片上传。将imageEntity装换成上传ResultEntity实体类
     *
     * @return
     */
    private ResultEntity wrapperImageInternal(ImageEntity image, String refCodeId, String transId, String userId,
                                              String transFileToServer) {
        ResultEntity result = new ResultEntity();
        result.suffix = Global.IMAGE_DEFAULT_FORMAT;
        result.bizHeadId = transId;
        result.bizLineId = image.refLineId;
        result.imageName = image.imageName;
        result.refType = image.refType;
        result.businessType = image.bizType;
        result.bizPart = "1";
        result.imagePath = image.imageDir + File.separator + result.imageName;
        result.createdBy = image.createBy;
        result.imageDate = image.createDate;
        result.userId = userId;
        result.transFileToServer = transFileToServer;
        result.fileType = image.takePhotoType;
        return result;
    }

    private ArrayList<ResultEntity> wrapperImage(List<ImageEntity> images, String refCodeId, String transId, String userId,
                                                 String transFileToServer) {
        ArrayList<ResultEntity> results = new ArrayList<>();
        int pos = -1;
        for (ImageEntity image : images) {
            ResultEntity result = wrapperImageInternal(image, refCodeId, transId, userId, transFileToServer);
            result.taskId = ++pos;
            results.add(result);
        }
        return results;
    }


}
