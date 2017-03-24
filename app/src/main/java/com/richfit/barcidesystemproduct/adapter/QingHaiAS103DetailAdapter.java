package com.richfit.barcidesystemproduct.adapter;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.R;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.basetreerv.CommonTreeAdapter;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.RowConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QingHaiAS103DetailAdapter extends CommonTreeAdapter<RefDetailEntity> {


    public QingHaiAS103DetailAdapter(Context context, int layoutId, List<RefDetailEntity> allNodes,
                                     List<RowConfig> parentNodeConfigs, List<RowConfig> childNodeConfigs) {
        super(context, layoutId, allNodes, parentNodeConfigs, childNodeConfigs);
    }

    @Override
    protected void convert(ViewHolder holder, RefDetailEntity data, int position) {
        holder.setText(R.id.rowNum, (position + 1) + "")
                //单据行号
                .setText(R.id.lineNum, data.lineNum)
                //检验批
                .setText(R.id.insLot, data.insLot)
                //参考单据
                .setText(R.id.refLineNum, data.lineNum105)
                //物料凭证
                .setText(R.id.refDoc, data.refDoc)
                //物料凭证行号
                .setText(R.id.refDocItem, String.valueOf(data.refDocItem))
                //物料号
                .setText(R.id.materialNum, data.materialNum)
                //物资描述
                .setText(R.id.materialDesc, data.materialDesc)
                //物料组
                .setText(R.id.materialGroup, data.materialGroup)
                //计量单位
                .setText(R.id.materialUnit, data.unit)
                //特殊库存标识
                .setText(R.id.specailInventoryFlag, data.specialInvFlag)
                //应收数量
                .setText(R.id.actQuantity, data.actQuantity)
                //累计实收数量
                .setText(R.id.totalQuantity, data.totalQuantity)
                //工厂
                .setText(R.id.work, data.workCode)
                //库存地点
                .setText(R.id.inv, data.invCode);
    }

    @Override
    public void notifyParentNodeChanged(int childNodePosition, int parentNodePosition) {
//        RefDetailEntity childNode = mVisibleNodes.get(childNodePosition);
//        RefDetailEntity parentNode = mVisibleNodes.get(parentNodePosition);
//        final float parentTotalQuantityV = UiUtil.convertToFloat(parentNode.totalQuantity, 0.0f);
//        final float childTotalQuantityV = UiUtil.convertToFloat(childNode.quantity, 0.0f);
//        final String newTotalQuantity = String.valueOf(parentTotalQuantityV - childTotalQuantityV);
//        parentNode.totalQuantity = newTotalQuantity;
//        notifyItemChanged(parentNodePosition);
    }

    /**
     * 由于103不具有父子节点的结构所以仅仅刷新本节点即可
     * @param position
     */
    @Override
    public void notifyNodeChanged(int position) {
        if (position >= 0 && position < mVisibleNodes.size()) {
            RefDetailEntity node = mVisibleNodes.get(position);
            node.invCode = "";
            node.invId = "";
            node.invName = "";
            node.totalQuantity = "";
            notifyItemChanged(position);
        }
    }

    @Override
    protected Map<String, Object> provideExtraData(int position) {
        return null;
    }

    /**
     * 给出同一级别的其他节点中所有的仓位信息
     * @param position
     * @param flag : 0:表示仓位/发出仓位 其他表示接受仓位
     * @return
     */
    public ArrayList<String> getLocations(int position, int flag) {
        ArrayList<String> locations = new ArrayList<>();
        for (int i = 0; i < mVisibleNodes.size(); i++) {
            RefDetailEntity node = mVisibleNodes.get(i);
            if (i == position || TextUtils.isEmpty(node.location) || TextUtils.isEmpty(node.recLocation))
                continue;
            locations.add(flag == 0 ? node.location : node.recLocation);
        }
        return locations;
    }

}