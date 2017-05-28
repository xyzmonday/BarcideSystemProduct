package com.richfit.barcodesystemproduct.adapter.itemDelegate;

import com.richfit.barcodesystemproduct.R;
import com.richfit.common_lib.baseadapterrv.base.ItemViewDelegate;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.RefDetailEntity;

/**
 * Created by monday on 2017/5/25.
 */

public class LocQTChildHeaderItemDelegate  implements ItemViewDelegate<RefDetailEntity> {

    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_locqt_detail_child_header;
    }

    @Override
    public boolean isForViewType(RefDetailEntity item, int position) {
        return item.getViewType() == Global.CHILD_NODE_HEADER_TYPE;
    }

    @Override
    public void convert(ViewHolder holder, RefDetailEntity item, int position) {
    }
}
