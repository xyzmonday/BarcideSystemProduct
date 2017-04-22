package com.richfit.common_lib.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.richfit.common_lib.R;
import com.richfit.common_lib.basetreerv.MultiItemTypeTreeAdapter;

import java.util.List;

public class TimeLineAdapter extends RecyclerView.Adapter<TimeLineViewHolder> {
    private List<String> mDataSet;
    private MultiItemTypeTreeAdapter.OnItemClickListener mOnItemClickListener;

    public TimeLineAdapter(List<String> models) {
        mDataSet = models;
    }


    @Override
    public TimeLineViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_upload, viewGroup, false);
        final TimeLineViewHolder viewHolder = new TimeLineViewHolder(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onItemLongClick(itemView,viewHolder,viewHolder.getAdapterPosition());
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TimeLineViewHolder timeLineViewHolder, int i) {
        timeLineViewHolder.setData(mDataSet.get(i));
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void setOnItemClickListener(MultiItemTypeTreeAdapter.OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }
}