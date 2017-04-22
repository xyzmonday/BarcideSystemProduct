package com.richfit.common_lib.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.richfit.common_lib.R;

import java.util.List;

/**
 * 展示Errors列表的适配器，支持错误的展开和收缩
 * Created by monday
 * on 15/8/23.
 */
public class ErrorListAdapter extends RecyclerView.Adapter<ErrorListViewHolder> {
    private List<String> mDataSet;
    private final SparseBooleanArray mCollapsedStatus;

    public ErrorListAdapter(List<String> models) {
        mDataSet = models;
        mCollapsedStatus = new SparseBooleanArray();
    }


    @Override
    public ErrorListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_show_message, viewGroup, false);
        return new ErrorListViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(ErrorListViewHolder viewHolder, int position) {
        viewHolder.setData(mDataSet.get(position), mCollapsedStatus, position);

    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
