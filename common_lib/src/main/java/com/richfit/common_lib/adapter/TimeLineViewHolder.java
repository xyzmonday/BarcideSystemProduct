package com.richfit.common_lib.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.richfit.common_lib.R;

public class TimeLineViewHolder extends RecyclerView.ViewHolder {
    private TextView tvShow;

    public TimeLineViewHolder(View itemView) {
        super(itemView);
        tvShow = (TextView) itemView.findViewById(R.id.item_time_line_txt);
    }

    public void setData(String data) {
        tvShow.setText(data);
    }
}