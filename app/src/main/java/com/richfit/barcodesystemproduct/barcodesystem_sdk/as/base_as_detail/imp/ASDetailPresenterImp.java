package com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_detail.imp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_detail.IASDetailPresenter;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_detail.IASDetailView;
import com.richfit.barcodesystemproduct.base.base_detail.BaseDetailPresenterImp;
import com.richfit.barcodesystemproduct.module.edit.EditActivity;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.domain.bean.LocationInfoEntity;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.TreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.subscribers.ResourceSubscriber;

/**
 * 注意这里给出的数据上传是标准的，也就是说uploadCollectionData和上下架是分开的
 * Created by monday on 2016/11/15.
 */

public class ASDetailPresenterImp extends BaseDetailPresenterImp<IASDetailView>
        implements IASDetailPresenter {

    @Inject
    public ASDetailPresenterImp(@ContextLife("Activity") Context context) {
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
                                    mView.setRefreshing(true, "获取明细缓存成功");
                                }
                            }
                        });
        addSubscriber(subscriber);
    }

    @Override
    public void deleteNode(String lineDeleteFlag, String transId, String transLineId, String locationId,
                           String refType, String bizType, final int position, String companyCode) {
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
    public void editNode(ArrayList<String> sendLocations, ArrayList<String> recLocations,
                         ReferenceEntity refData, RefDetailEntity node, String companyCode,
                         String bizType, String refType, String subFunName, int position) {
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
                    //获取该行下所有已经上架的仓位
                    final ArrayList<String> locationsOfParentNode = new ArrayList<>();
                    if (parentNode != null) {
                        List<TreeNode> childTreeNodes = parentNode.getChildren();
                        for (TreeNode childTreeNode : childTreeNodes) {
                            if (childTreeNode != null && RefDetailEntity.class.isInstance(childTreeNode)) {
                                final RefDetailEntity childNode = (RefDetailEntity) childTreeNode;
                                if (childNode.getViewType() == Global.CHILD_NODE_HEADER_TYPE || node == childNode)
                                    //排除自己
                                    continue;
                                locationsOfParentNode.add(childNode.location);
                            }
                        }
                    }
                    //该子节点的id
                    bundle.putString(Global.EXTRA_REF_LINE_ID_KEY, parentNode.refLineId);
                    bundle.putString(Global.EXTRA_LOCATION_ID_KEY, node.locationId);

                    //入库子菜单类型
                    bundle.putString(Global.EXTRA_BIZ_TYPE_KEY, bizType);
                    bundle.putString(Global.EXTRA_REF_TYPE_KEY, refType);

                    //父节点的位置
                    bundle.putInt(Global.EXTRA_POSITION_KEY, indexOf);

                    //父节点的退货交货数量
                    bundle.putString(Global.EXTRA_RETURN_QUANTITY_KEY, parentNode.returnQuantity);

                    //父节点的项目文本
                    bundle.putString(Global.EXTRA_PROJECT_TEXT_KEY, parentNode.projectText);

                    //移动原因说明
                    bundle.putString(Global.EXTRA_MOVE_CAUSE_DESC_KEY, parentNode.moveCauseDesc);

                    //移动原因
                    bundle.putString(Global.EXTRA_MOVE_CAUSE_KEY, parentNode.moveCause);

                    //决策代码
                    bundle.putString(Global.EXTRA_DECISION_CAUSE_KEY, parentNode.decisionCode);

                    //入库的子菜单的名称
                    bundle.putString(Global.EXTRA_TITLE_KEY, subFunName + "-明细修改");

                    //该父节点所有的已经入库的仓位
                    bundle.putStringArrayList(Global.EXTRA_LOCATION_LIST_KEY, locationsOfParentNode);

                    //库存地点
                    bundle.putString(Global.EXTRA_INV_ID_KEY, node.invId);
                    bundle.putString(Global.EXTRA_INV_CODE_KEY, node.invCode);

                    //累计数量
                    bundle.putString(Global.EXTRA_TOTAL_QUANTITY_KEY, parentNode.totalQuantity);

                    //批次
                    bundle.putString(Global.EXTRA_BATCH_FLAG_KEY, node.batchFlag);

                    //上架仓位
                    bundle.putString(Global.EXTRA_LOCATION_KEY, node.location);

                    //实收数量
                    bundle.putString(Global.EXTRA_QUANTITY_KEY, node.quantity);


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
        RxSubscriber<String> subscriber = mRepository.transferCollectionData(transId, bizType, refType,
                userId, voucherDate, transToSapFlag, extraHeaderMap)
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
    public void sapUpAndDownLocation(String transId, String bizType, String refType, String userId, String voucherDate, String transToSapFlag, Map<String, Object> extraHeaderMap, int submitFlag) {

    }

    /**
     * 通过抬头获取的单据数据和缓存数据生成新的单据数据。
     * 注意我们的目的是将这两部分数据完全分离，这样有利于处理。
     * 注意扩展字段生成的规则：
     * 父节点：原始单据的扩展字段+对应的该行的缓存扩展字段
     * 子节点：该行仓位级别的缓存扩展子弹
     *
     * @param refData：塔头获取的原始单据数据
     * @param cache：缓存单据数据
     * @return
     */
    protected ArrayList<RefDetailEntity> createNodesByCache(ReferenceEntity refData, ReferenceEntity cache) {
        ArrayList<RefDetailEntity> nodes = new ArrayList<>();
        List<RefDetailEntity> list = refData.billDetailList;
        //1.形成父节点数据集合
        for (RefDetailEntity data : list) {
            RefDetailEntity cachedData = getLineDataByRefLineId(data, cache);
            if (cachedData == null) {
                //说明该还没有缓存
                nodes.add(data);
            } else {
                //如果有缓存，那么将缓存作为父节点，注意注意此时应当将原始单据的部分字段信息赋值给缓存。
                //这里我们不适用原始单据信息作为缓存这是因为单据信息是全局的,另外就是修改等针对该节点的操作需要缓存数据
                //将原始单据的物料信息赋值给缓存
                cachedData.lineNum = data.lineNum;
                cachedData.materialNum = data.materialNum;
                cachedData.materialId = data.materialId;
                cachedData.materialDesc = data.materialDesc;
                cachedData.materialGroup = data.materialGroup;
                cachedData.unit = data.unit;
                cachedData.actQuantity = data.actQuantity;
                cachedData.refDoc = data.refDoc;
                cachedData.refDocItem = data.refDocItem;
                //注意单据中有lineNum105
                cachedData.lineNum105 = data.lineNum105;
                cachedData.insLot = data.insLot;
                nodes.add(cachedData);
            }
        }
        //2.形成父节点结构
        addTreeInfo(nodes);
        ArrayList<RefDetailEntity> result = new ArrayList<>();
        result.addAll(nodes);
        //3.生成子节点
        for (RefDetailEntity parentNode : nodes) {
            List<LocationInfoEntity> locationList = parentNode.locationList;
            if (locationList == null || locationList.size() == 0) {
                //说明是原始单据的父节点
                continue;
            }
            //首先去除之前所有父节点的子节点
            parentNode.getChildren().clear();
            parentNode.setHasChild(false);

            for (LocationInfoEntity location : locationList) {
                RefDetailEntity childNode = new RefDetailEntity();
                childNode.invId = parentNode.invId;
                childNode.invCode = parentNode.invCode;
                childNode.totalQuantity = parentNode.totalQuantity;
                childNode.location = location.location;
                childNode.batchFlag = location.batchFlag;
                childNode.quantity = location.quantity;
                childNode.transId = location.transId;
                childNode.transLineId = location.transLineId;
                childNode.locationId = location.id;
                addTreeInfo(parentNode, childNode, result);
            }
        }
        return result;
    }
}
