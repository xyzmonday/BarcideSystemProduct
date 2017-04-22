package com.richfit.barcodesystemproduct.module_acceptstore.qinghai_105n.imp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_detail.imp.ASDetailPresenterImp;
import com.richfit.barcodesystemproduct.module.edit.EditActivity;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.TreeNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class QingHaiAS105NDetailPresenterImp extends ASDetailPresenterImp {

    @Inject
    public QingHaiAS105NDetailPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void editNode(ArrayList<String> sendLocations, ArrayList<String> recLocations,
                         ReferenceEntity refData, RefDetailEntity node, String companyCode,
                         String bizType, String refType, String subFunName, int position) {
        if (refData != null) {
            TreeNode treeNode = node.getParent();
            if (treeNode != null && RefDetailEntity.class.isInstance(treeNode)) {

                final RefDetailEntity parentNode = (RefDetailEntity) treeNode;

                int indexOf = getParentNodePosition(refData, parentNode.lineNum105);

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

                    //父节点的扩展字段
                    bundle.putSerializable(Global.COLLECT_EXTRA_MAP_KEY, (Serializable) parentNode.mapExt);

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

                    //子节点的额外字段的数据
                    bundle.putSerializable(Global.LOCATION_EXTRA_MAP_KEY, (Serializable) node.mapExt);

                    intent.putExtras(bundle);
                    Activity activity = (Activity) mContext;
                    activity.startActivity(intent);
                }
            }
        }
    }
    /**
     * 通过lineNum105将缓存和原始单据行关联起来
     */
    @Override
    protected RefDetailEntity getLineDataByRefLineId(RefDetailEntity refLineData, ReferenceEntity cachedRefData) {
        if (refLineData == null) {
            return null;
        }

        final String lineNum105 = refLineData.lineNum105;
        if (TextUtils.isEmpty(lineNum105))
            return null;
        //通过refLineId匹配出缓存中的明细行
        List<RefDetailEntity> detail = cachedRefData.billDetailList;
        for (RefDetailEntity entity : detail) {
            if (lineNum105.equals(entity.lineNum105)) {
                return entity;
            }
        }
        return null;
    }

    @Override
    /**
     * 获取父节点的位置
     *
     * @param refData:明细数据列表
     * @param refLineId:需要查询的父节点的id
     */
    protected int getParentNodePosition(ReferenceEntity refData, String key) {
        int pos = -1;
        if (TextUtils.isEmpty(key)) {
            return pos;
        }
        List<RefDetailEntity> list = refData.billDetailList;
        if (list != null && list.size() > 0) {
            for (RefDetailEntity entity : list) {
                pos++;
                if (key.equals(entity.lineNum105))
                    return pos;
            }
        }
        return pos;
    }


}
