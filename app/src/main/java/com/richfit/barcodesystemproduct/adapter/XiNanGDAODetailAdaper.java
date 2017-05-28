package com.richfit.barcodesystemproduct.adapter;

import android.content.Context;

import com.richfit.barcodesystemproduct.adapter.itemDelegate.XiNanGDAOChildHeaderItemDelegate;
import com.richfit.barcodesystemproduct.adapter.itemDelegate.XiNanGDAOChildItemDelegate;
import com.richfit.barcodesystemproduct.adapter.itemDelegate.XiNanGDAOParentHeaderItemDelegate;
import com.richfit.common_lib.basetreerv.MultiItemTypeTreeAdapter;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.RefDetailEntity;

import java.util.List;

/**
 * Created by monday on 2017/5/26.
 */

public class XiNanGDAODetailAdaper extends MultiItemTypeTreeAdapter<RefDetailEntity> {

    public XiNanGDAODetailAdaper(Context context, List<RefDetailEntity> allNodes) {
        super(context, allNodes);
        addItemViewDelegate(Global.PARENT_NODE_HEADER_TYPE,new XiNanGDAOParentHeaderItemDelegate());
        addItemViewDelegate(Global.CHILD_NODE_HEADER_TYPE,new XiNanGDAOChildHeaderItemDelegate());
        addItemViewDelegate(Global.CHILD_NODE_ITEM_TYPE,new XiNanGDAOChildItemDelegate());
    }
}
