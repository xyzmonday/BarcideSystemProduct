package com.richfit.barcidesystemproduct.adapter;

import android.content.Context;

import com.richfit.common_lib.baseadapterlv.CommonAdapter;
import com.richfit.common_lib.baseadapterlv.ViewHolder;
import com.richfit.domain.bean.InventoryEntity;

import java.util.List;

/**
 * Created by monday on 2016/11/19.
 */

public class LocationAdapter extends CommonAdapter<InventoryEntity> {


    public LocationAdapter(Context context, int layoutId, List<InventoryEntity> datas) {
        super(context, layoutId, datas);
    }

    @Override
    protected void convert(ViewHolder viewHolder, InventoryEntity item, int position) {
        viewHolder.setText(android.R.id.text1, "barcode".equalsIgnoreCase(item.location)? "" : item.locationCombine);
    }
}
