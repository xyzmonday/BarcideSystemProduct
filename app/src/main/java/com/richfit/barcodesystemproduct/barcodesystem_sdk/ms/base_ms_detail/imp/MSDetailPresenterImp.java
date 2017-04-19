package com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_ms_detail.imp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_ms_detail.IMSDetailPresenter;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_ms_detail.IMSDetailView;
import com.richfit.barcodesystemproduct.base.base_detail.BaseDetailPresenterImp;
import com.richfit.barcodesystemproduct.module.edit.EditActivity;
import com.richfit.common_lib.rxutils.RetryWhenNetworkException;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.LocationInfoEntity;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.TreeNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by monday on 2017/2/10.
 */

public class MSDetailPresenterImp extends BaseDetailPresenterImp<IMSDetailView>
        implements IMSDetailPresenter {


    @Inject
    public MSDetailPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void getTransferInfo(final ReferenceEntity refData, String refCodeId, String bizType, String refType,
                                String userId, String workId, String invId, String recWorkId, String recInvId) {
        mView = getView();
        ResourceSubscriber<ArrayList<RefDetailEntity>> subscriber =
                mRepository.getTransferInfo("", refCodeId, bizType, refType,
                        "", "", "", "", "")
                        .zipWith(Flowable.just(refData), (cache, data) -> createNodesByCache(data, cache))
                        .flatMap(nodes -> sortNodes(nodes))
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new ResourceSubscriber<ArrayList<RefDetailEntity>>() {
                            @Override
                            public void onNext(ArrayList<RefDetailEntity> nodes) {
                                if (mView != null) {
                                    mView.showNodes(nodes);
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                if (mView != null) {
                                    mView.setRefreshing(true, t.getMessage());
                                    //展示抬头获取的数据，没有缓存
                                    mView.showNodes(refData.billDetailList);
                                }
                            }

                            @Override
                            public void onComplete() {
                                if (mView != null) {
                                   mView.refreshComplete();
                                }
                            }
                        });
        addSubscriber(subscriber);
    }

    @Override
    public void deleteNode(String lineDeleteFlag, String transId, String transLineId, String locationId,
                           String refType, String bizType, int position, String companyCode) {
        RxSubscriber<String> subscriber = mRepository.deleteCollectionDataSingle(lineDeleteFlag, transId, transLineId,
                locationId, refType, bizType, "", "", position, companyCode)
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
    public void editNode(ArrayList<String> sendLocations,ArrayList<String> recLocations,
                         ReferenceEntity refData, RefDetailEntity node,
                         String companyCode, String bizType, String refType, String subFunName,int position) {

        if (refData != null) {
            TreeNode treeNode = node.getParent();
            if (treeNode != null && RefDetailEntity.class.isInstance(treeNode)) {
                final RefDetailEntity parentNode = (RefDetailEntity) treeNode;
                int indexOf = getParentNodePosition(refData, parentNode.refLineId);
                if (indexOf < 0) {
                    return;
                }
                if (indexOf >= 0 && indexOf < refData.billDetailList.size()) {
                    Intent intent = new Intent(mContext, EditActivity.class);
                    Bundle bundle = new Bundle();
                    //获取该行下所有已经移库的仓位
                    final ArrayList<String> locations = new ArrayList<>();
                    if (parentNode != null) {
                        List<TreeNode> childTreeNodes = parentNode.getChildren();
                        for (TreeNode childTreeNode : childTreeNodes) {
                            if (childTreeNode != null && RefDetailEntity.class.isInstance(childTreeNode)) {
                                final RefDetailEntity childNode = (RefDetailEntity) childTreeNode;
                                if (childNode.getViewType() == Global.CHILD_NODE_HEADER_TYPE || node == childNode)
                                    //排除自己
                                    continue;
                                locations.add(childNode.location);
                            }
                        }
                    }
                    //该子节点的id
                    bundle.putString(Global.EXTRA_REF_LINE_ID_KEY, node.refLineId);
                    //该子节点的LocationId
                    bundle.putString(Global.EXTRA_LOCATION_ID_KEY, node.locationId);


                    //入库子菜单类型
                    bundle.putString(Global.EXTRA_BIZ_TYPE_KEY, bizType);
                    bundle.putString(Global.EXTRA_REF_TYPE_KEY, refType);
                    //父节点的位置
                    bundle.putInt(Global.EXTRA_POSITION_KEY, indexOf);
                    //入库的子菜单的名称
                    bundle.putString(Global.EXTRA_TITLE_KEY, subFunName + "-明细修改");

                    //该父节点所有的已经入库的仓位
                    bundle.putStringArrayList(Global.EXTRA_LOCATION_LIST_KEY, locations);

                    //库存地点
                    bundle.putString(Global.EXTRA_INV_CODE_KEY, node.invCode);
                    bundle.putString(Global.EXTRA_INV_ID_KEY, node.invId);

                    //累计数量
                    bundle.putString(Global.EXTRA_TOTAL_QUANTITY_KEY, parentNode.totalQuantity);

                    //批次
                    bundle.putString(Global.EXTRA_BATCH_FLAG_KEY, node.batchFlag);

                    //上架仓位
                    bundle.putString(Global.EXTRA_LOCATION_KEY, node.location);
                    bundle.putString(Global.EXTRA_SPECIAL_INV_FLAG_KEY,node.specialInvFlag);
                    bundle.putString(Global.EXTRA_SPECIAL_INV_NUM_KEY,node.specialInvNum);
                    //实收数量
                    bundle.putString(Global.EXTRA_QUANTITY_KEY, node.quantity);

                    //子节点的额外字段的数据
                    bundle.putSerializable(Global.LOCATION_EXTRA_MAP_KEY, (Serializable) node.mapExt);

                    intent.putExtras(bundle);

                    Activity activity = (Activity) mContext;
                    activity.startActivity(intent);
                }
            }
        }
    }

    @Override
    public void submitData2BarcodeSystem(String transId, String bizType, String refType, String userId, String voucherDate,
                                         String transToSapFlag, Map<String, Object> extraHeaderMap) {
        mView = getView();
        RxSubscriber<String> subscriber = Flowable.concat(mRepository.uploadCollectionData("", transId, bizType, refType, -1, voucherDate, "", userId),
                mRepository.transferCollectionData(transId, bizType, refType, userId, voucherDate, transToSapFlag, extraHeaderMap))
                .doOnError(str -> SPrefUtil.saveData(bizType + refType, "0"))
                .doOnComplete(() -> SPrefUtil.saveData(bizType + refType, "1"))
                .compose(TransformerHelper.io2main())
                .subscribeWith(new RxSubscriber<String>(mContext, "正在过账...") {
                    @Override
                    public void _onNext(String message) {
                        if (mView != null) {
                            mView.showTransferedVisa(message);
                        }
                    }

                    @Override
                    public void _onNetWorkConnectError(String message) {
                        if (mView != null) {
                            mView.networkConnectError(Global.RETRY_TRANSFER_DATA_ACTION);
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
                });
        addSubscriber(subscriber);
    }

    @Override
    public void submitData2SAP(String transId, String bizType, String refType, String userId,
                               String voucherDate, String transToSapFlag, Map<String, Object> extraHeaderMap) {
        mView = getView();
        RxSubscriber<String> subscriber = mRepository.transferCollectionData(transId, bizType, refType, userId,
                voucherDate, transToSapFlag, extraHeaderMap)
                .retryWhen(new RetryWhenNetworkException(3, 3000))
                .doOnComplete(() -> SPrefUtil.saveData(bizType + refType, "0"))
                .compose(TransformerHelper.io2main())
                .subscribeWith(new RxSubscriber<String>(mContext, "正在上传数据...") {
                    @Override
                    public void _onNext(String message) {
                        if (mView != null) {
                            mView.showInspectionNum(message);
                        }
                    }

                    @Override
                    public void _onNetWorkConnectError(String message) {
                        if (mView != null) {
                            mView.networkConnectError(Global.RETRY_UPLOAD_DATA_ACTION);
                        }
                    }

                    @Override
                    public void _onCommonError(String message) {
                        if (mView != null && !TextUtils.isEmpty(message)) {
                            mView.submitSAPFail(message.split("_"));
                        }
                    }

                    @Override
                    public void _onServerError(String code, String message) {
                        if (mView != null) {
                            mView.submitSAPFail(new String[]{message});
                        }
                    }

                    @Override
                    public void _onComplete() {
                        if (mView != null) {
                            mView.submitSAPSuccess();
                        }
                    }
                });
        addSubscriber(subscriber);
    }

    @Override
    public void turnOwnSupplies(String transId, String bizType, String refType, String userId,
                                String voucherDate, String transToSapFlag,
                                Map<String, Object> extraHeaderMap, int submitFlag) {
        mView = getView();
        RxSubscriber<String> subscriber =
                Flowable.concat(mRepository.transferCollectionData(transId, bizType, refType,
                        userId, voucherDate, transToSapFlag, extraHeaderMap),
                        mRepository.transferCollectionData(transId, bizType, refType,
                              userId, voucherDate, "08", extraHeaderMap))
                .compose(TransformerHelper.io2main())
                .subscribeWith(new RxSubscriber<String>(mContext, "正在寄售转自有数据...") {
                    @Override
                    public void _onNext(String s) {

                    }

                    @Override
                    public void _onNetWorkConnectError(String message) {
                        if (mView != null) {
                            mView.turnOwnSuppliesFail(message);
                        }
                    }

                    @Override
                    public void _onCommonError(String message) {
                        if (mView != null) {
                            mView.turnOwnSuppliesFail(message);
                        }
                    }

                    @Override
                    public void _onServerError(String code, String message) {
                        if (mView != null) {
                            mView.turnOwnSuppliesFail(message);
                        }
                    }

                    @Override
                    public void _onComplete() {
                        if (mView != null) {
                            mView.turnOwnSuppliesSuccess();
                        }
                    }
                });
        addSubscriber(subscriber);
    }

    /**
     * 通过抬头获取的单据数据和缓存数据生成新的单据数据。
     * 注意我们的目的是将这两部分数据完全分离，这样有利于处理。
     *
     * @param refData：塔头获取的原始单据数据
     * @param cache：缓存单据数据
     * @return
     */
    private ArrayList<RefDetailEntity> createNodesByCache(ReferenceEntity refData, ReferenceEntity cache) {
        ArrayList<RefDetailEntity> nodes = new ArrayList<>();
        //第一步，将原始单据中的行明细赋值新的父节点中
        List<RefDetailEntity> list = refData.billDetailList;
        for (RefDetailEntity node : list) {
            //获取缓存中的明细，如果该行明细没有缓存，那么该行明细仅仅赋值原始单据信息
            RefDetailEntity cachedEntity = getLineDataByRefLineId(node, cache);
            if (cachedEntity == null)
                cachedEntity = new RefDetailEntity();

            cachedEntity.lineNum = node.lineNum;
            cachedEntity.materialNum = node.materialNum;
            cachedEntity.materialId = node.materialId;
            cachedEntity.materialDesc = node.materialDesc;
            cachedEntity.materialGroup = node.materialGroup;
            cachedEntity.actQuantity = node.actQuantity;
            cachedEntity.workCode = node.workCode;
            //处理父节点的缓存
            cachedEntity.mapExt = UiUtil.copyMap(node.mapExt, cachedEntity.mapExt);
            nodes.add(cachedEntity);
        }

        //生成父节点
        addTreeInfo(nodes);

        //第二步，利用缓存生成新的子节点
        //生成父子节点
        List<RefDetailEntity> details = cache.billDetailList;
        for (RefDetailEntity parentNode : details) {
            //首先去除之前所有父节点的子节点
            parentNode.getChildren().clear();
            parentNode.setHasChild(false);

            //生成子结点
            List<LocationInfoEntity> locations = parentNode.locationList;
            if (locations != null && locations.size() > 0) {
                for (LocationInfoEntity location : locations) {
                    RefDetailEntity childNode = new RefDetailEntity();
                    childNode.refLineId = parentNode.refLineId;
                    childNode.invId = parentNode.invId;
                    childNode.invCode = parentNode.invCode;
                    childNode.totalQuantity = parentNode.totalQuantity;
                    //赋值子节点的缓存数据
                    childNode.location = location.location;
                    childNode.batchFlag = location.batchFlag;
                    childNode.quantity = location.quantity;
                    childNode.transId = location.transId;
                    childNode.transLineId = location.transLineId;
                    //LocationId
                    childNode.locationId = location.id;
                    childNode.specialInvFlag = location.specialInvFlag;
                    childNode.specialInvNum = location.specialInvNum;
                    childNode.specialConvert = location.specialConvert;
                    //处理子节点的缓存
                    childNode.mapExt = location.mapExt;
                    addTreeInfo(parentNode, childNode, nodes);
                }
            }
        }
        return nodes;
    }

}
