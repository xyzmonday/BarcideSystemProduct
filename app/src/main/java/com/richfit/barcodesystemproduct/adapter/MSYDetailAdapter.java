package com.richfit.barcodesystemproduct.adapter;

import android.content.Context;

import com.richfit.barcodesystemproduct.adapter.itemDelegate.MSYChildHeaderItemDelegate;
import com.richfit.barcodesystemproduct.adapter.itemDelegate.MSYChildItemDelegate;
import com.richfit.barcodesystemproduct.adapter.itemDelegate.MSYParentHeaderItemDelegate;
import com.richfit.common_lib.basetreerv.MultiItemTypeTreeAdapter;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.RowConfig;

import java.util.List;
import java.util.Map;

/**
 * Created by monday on 2017/3/19.
 */

public class MSYDetailAdapter extends MultiItemTypeTreeAdapter<RefDetailEntity> {


    public MSYDetailAdapter(Context context, List<RefDetailEntity> allNodes) {
        super(context, allNodes);
        addItemViewDelegate(Global.PARENT_NODE_HEADER_TYPE, new MSYParentHeaderItemDelegate());
        addItemViewDelegate(Global.CHILD_NODE_HEADER_TYPE, new MSYChildHeaderItemDelegate());
        addItemViewDelegate(Global.CHILD_NODE_ITEM_TYPE, new MSYChildItemDelegate());
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
