package com.richfit.barcodesystemproduct.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.richfit.barcodesystemproduct.R;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.basetreerv.CommonTreeAdapter;
import com.richfit.common_lib.utils.AppCompat;
import com.richfit.domain.bean.InventoryEntity;

import java.util.List;

/**
 * Created by monday on 2016/12/6.
 */

public class CNDetailAdapter extends CommonTreeAdapter<InventoryEntity> {


    public CNDetailAdapter(Context context, int layoutId, List<InventoryEntity> allNodes) {
        super(context, layoutId, allNodes);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void convert(ViewHolder holder, InventoryEntity item, int position) {
        holder.setText(R.id.rowNum, String.valueOf(position + 1))
                .setText(R.id.lineNum, item.lineNum)
                .setText(R.id.materialNum, item.materialNum)
                .setText(R.id.materialDesc, item.materialDesc)
                .setText(R.id.materialGroup, item.materialGroup)
                .setText(R.id.checkLocation, item.location)
                .setText(R.id.invQuantity, item.invQuantity)
                .setText(R.id.checkQuantity, item.totalQuantity)
                .setText(R.id.specialInvFlag, item.specialInvFlag)
                .setText(R.id.specialInvNum, item.specialInvNum)
                .setText(R.id.checkState, item.isChecked ? "已盘点" : "未盘点");
        if (item.isChecked) {
            holder.setBackgroundRes(R.id.root_id, R.color.green_color_emerald);
        } else {
            holder.setBackgroundDrawable(R.id.root_id, AppCompat.getDrawable(mContext, R.drawable.parent_node_bg));
        }
    }

}