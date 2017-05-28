package com.richfit.barcodesystemproduct.adapter.itemDelegate;

import com.richfit.barcodesystemproduct.R;
import com.richfit.common_lib.baseadapterrv.base.ItemViewDelegate;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.RefDetailEntity;

/**
 * Created by monday on 2017/5/26.
 */

public class XiNanGDAOChildItemDelegate implements ItemViewDelegate<RefDetailEntity> {
    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_xinangdao_detail_child_item;
    }

    @Override
    public boolean isForViewType(RefDetailEntity item, int position) {
        return item.getViewType() == Global.CHILD_NODE_ITEM_TYPE;
    }

    @Override
    public void convert(ViewHolder holder, RefDetailEntity item, int position) {
        holder.setText(R.id.Location, item.location)
                .setText(R.id.batchFlag, item.batchFlag)
                .setText(R.id.quantity, item.quantity);
    }
}