package com.richfit.barcidesystemproduct.adapter;

import android.content.Context;

import com.richfit.barcodesystemproduct.adapter.itemDelegate.ASYChildHeaderItemDelegate;
import com.richfit.barcodesystemproduct.adapter.itemDelegate.ASYChildItemDelegate;
import com.richfit.barcodesystemproduct.adapter.itemDelegate.QingHaiAS105ParentHeaderItemDelegate;
import com.richfit.common_lib.basetreerv.MultiItemTypeTreeAdapter;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.RowConfig;

import java.util.List;
import java.util.Map;

/**
 * Created by monday on 2017/3/7.
 */

public class QingHaiAS105DetailAdapter extends MultiItemTypeTreeAdapter<RefDetailEntity> {

    public QingHaiAS105DetailAdapter(Context context, List<RefDetailEntity> allNodes,
                                     List<RowConfig> parentNodeConfigs,
                                     List<RowConfig> childNodeConfigs) {
        super(context, allNodes, parentNodeConfigs, childNodeConfigs);
        addItemViewDelegate(Global.PARENT_NODE_HEADER_TYPE,new QingHaiAS105ParentHeaderItemDelegate());
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

    @Override
    protected Map<String, Object> provideExtraData(int position) {
        return mVisibleNodes.get(position).mapExt;
    }
}
