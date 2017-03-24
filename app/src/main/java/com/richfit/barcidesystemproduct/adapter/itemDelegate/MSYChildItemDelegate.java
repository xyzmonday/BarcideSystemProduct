package com.richfit.barcidesystemproduct.adapter.itemDelegate;

import com.richfit.barcodesystemproduct.R;
import com.richfit.common_lib.baseadapterrv.base.ItemViewDelegate;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.RefDetailEntity;

/**
 * Created by monday on 2017/2/10.
 */

public class MSYChildItemDelegate implements ItemViewDelegate<RefDetailEntity> {
    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_msy_detail_child_item;
    }

    @Override
    public boolean isForViewType(RefDetailEntity item, int position) {
        return item.getViewType() == Global.CHILD_NODE_ITEM_TYPE;
    }

    @Override
    public void convert(ViewHolder holder, RefDetailEntity item, int position) {
        //发出仓位
        holder.setText(R.id.sendLocation, item.location);
        //发出批次
        holder.setText(R.id.sendBatchFlag, item.batchFlag);
        holder.setText(R.id.quantity,item.quantity);
        //接搜仓位
        holder.setText(R.id.recLocation,item.recLocation);
        //接收批次
        holder.setText(R.id.recBatchFlag,item.recBatchFlag);
        //特殊库存
        holder.setText(R.id.specialInvFlag,item.specialInvFlag);
        holder.setText(R.id.specialInvNum,item.specialInvNum);
    }
}
