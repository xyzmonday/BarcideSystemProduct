package com.richfit.barcodesystemproduct.adapter;

import android.content.Context;

import com.richfit.barcodesystemproduct.adapter.itemDelegate.LocQTChildHeaderItemDelegate;
import com.richfit.barcodesystemproduct.adapter.itemDelegate.LocQTChildItemDelegate;
import com.richfit.barcodesystemproduct.adapter.itemDelegate.LocQTParentHeaderItemDelegate;
import com.richfit.common_lib.basetreerv.MultiItemTypeTreeAdapter;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.RefDetailEntity;

import java.util.List;

/**
 * Created by monday on 2017/5/25.
 */

public class LocQTDetailAdapter  extends MultiItemTypeTreeAdapter<RefDetailEntity> {

    public LocQTDetailAdapter(Context context, List<RefDetailEntity> allNodes) {
        super(context, allNodes);
        addItemViewDelegate(Global.PARENT_NODE_HEADER_TYPE,new LocQTParentHeaderItemDelegate());
        addItemViewDelegate(Global.CHILD_NODE_HEADER_TYPE,new LocQTChildHeaderItemDelegate());
        addItemViewDelegate(Global.CHILD_NODE_ITEM_TYPE,new LocQTChildItemDelegate());
    }
}
