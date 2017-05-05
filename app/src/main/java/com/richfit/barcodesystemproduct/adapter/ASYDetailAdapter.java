package com.richfit.barcodesystemproduct.adapter;

import android.content.Context;

import com.richfit.barcodesystemproduct.adapter.itemDelegate.ASYChildHeaderItemDelegate;
import com.richfit.barcodesystemproduct.adapter.itemDelegate.ASYChildItemDelegate;
import com.richfit.barcodesystemproduct.adapter.itemDelegate.ASYParentHeaderItemDelegate;
import com.richfit.common_lib.basetreerv.MultiItemTypeTreeAdapter;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.RefDetailEntity;

import java.util.List;

/**
 * 标准物资入库有参考明细界面适配器
 * Created by monday on 2017/3/17.
 */

public class ASYDetailAdapter extends MultiItemTypeTreeAdapter<RefDetailEntity> {


    public ASYDetailAdapter(Context context, List<RefDetailEntity> allNodes) {
        super(context, allNodes);
        addItemViewDelegate(Global.PARENT_NODE_HEADER_TYPE,new ASYParentHeaderItemDelegate());
        addItemViewDelegate(Global.CHILD_NODE_HEADER_TYPE,new ASYChildHeaderItemDelegate());
        addItemViewDelegate(Global.CHILD_NODE_ITEM_TYPE,new ASYChildItemDelegate());
    }

    @Override
    public void notifyParentNodeChanged(int childNodePosition, int parentNodePosition) {
        RefDetailEntity childNode = mVisibleNodes.get(childNodePosition);
        RefDetailEntity parentNode = mVisibleNodes.get(parentNodePosition);
        final float parentTotalQuantityV = UiUtil.convertToFloat(parentNode.totalQuantity, 0.0f);
        final float childTotalQuantityV = UiUtil.convertToFloat(childNode.quantity, 0.0f);
        final String newTotalQuantity = String.valueOf(parentTotalQuantityV - childTotalQuantityV);
        parentNode.totalQuantity = newTotalQuantity;
        notifyItemChanged(parentNodePosition);
    }

    @Override
    public void notifyNodeChanged(int position) {

    }

}
