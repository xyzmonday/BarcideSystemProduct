package com.richfit.barcodesystemproduct.adapter;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.common_lib.baseadapterlv.CommonAdapter;
import com.richfit.common_lib.baseadapterlv.ViewHolder;
import com.richfit.domain.bean.InventoryEntity;

import java.util.List;

/**
 * Created by monday on 2017/5/25.
 */

public class SpecialInvAdapter extends CommonAdapter<InventoryEntity> {


    public SpecialInvAdapter(Context context, int layoutId, List<InventoryEntity> datas) {
        super(context, layoutId, datas);
    }

    @Override
    protected void convert(ViewHolder viewHolder, InventoryEntity item, int position) {
        if (position == 0) {
            viewHolder.setText(android.R.id.text1, item.location);
            return;
        }
        if (TextUtils.isEmpty(item.specialInvFlag) || TextUtils.isEmpty(item.specialInvNum)) {
            viewHolder.setText(android.R.id.text1, "自有");
        } else {

            viewHolder.setText(android.R.id.text1, item.specialInvFlag + "_" + item.specialInvNum);
        }
    }
}