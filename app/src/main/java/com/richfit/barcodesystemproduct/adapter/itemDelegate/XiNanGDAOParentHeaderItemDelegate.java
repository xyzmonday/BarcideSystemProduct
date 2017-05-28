package com.richfit.barcodesystemproduct.adapter.itemDelegate;

import com.richfit.barcodesystemproduct.R;
import com.richfit.common_lib.baseadapterrv.base.ItemViewDelegate;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.RefDetailEntity;

/**
 * Created by monday on 2017/5/26.
 */

public class XiNanGDAOParentHeaderItemDelegate implements ItemViewDelegate<RefDetailEntity> {
    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_xinangdao_detail_parent_header;
    }

    @Override
    public boolean isForViewType(RefDetailEntity item, int position) {
        return item.getViewType() == Global.PARENT_NODE_HEADER_TYPE;
    }

    @Override
    public void convert(ViewHolder holder, RefDetailEntity data, int position) {

    }
}
