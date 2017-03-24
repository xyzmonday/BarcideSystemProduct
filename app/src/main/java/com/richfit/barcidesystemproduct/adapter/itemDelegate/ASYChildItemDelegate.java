package com.richfit.barcidesystemproduct.adapter.itemDelegate;

import com.richfit.barcodesystemproduct.R;
import com.richfit.common_lib.baseadapterrv.base.ItemViewDelegate;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.RefDetailEntity;

/**
 * 标准物资入库子节点的明细Item代理
 * Created by monday on 2017/3/17.
 */

public class ASYChildItemDelegate implements ItemViewDelegate<RefDetailEntity> {
    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_asy_detail_child_item;
    }

    @Override
    public boolean isForViewType(RefDetailEntity item, int position) {
        return item.getViewType() == Global.CHILD_NODE_ITEM_TYPE;
    }

    @Override
    public void convert(ViewHolder holder, RefDetailEntity item, int position) {

        holder.setText(R.id.location, "barcode".equalsIgnoreCase(item.location) ? "" : item.location);
        holder.setText(R.id.batchFlag, item.batchFlag);
        holder.setText(R.id.quantity, item.quantity);
    }
}
