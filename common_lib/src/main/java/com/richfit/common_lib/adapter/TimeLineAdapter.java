package com.richfit.common_lib.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.richfit.common_lib.R;
import com.richfit.common_lib.utils.AppCompat;
import com.richfit.domain.bean.UploadMsgEntity;

import java.util.List;

public class TimeLineAdapter extends RecyclerView.Adapter<TimeLineViewHolder> {
    private List<UploadMsgEntity> mDataSet;
    private StringBuffer mMessage;
    private Context mContext;
    private ItemClickListener mOnItemClickListener;

    public TimeLineAdapter(Context context, List<UploadMsgEntity> models) {
        mDataSet = models;
        mContext = context;
        mMessage = new StringBuffer();
    }


    @Override
    public TimeLineViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_upload, viewGroup, false);
        final TimeLineViewHolder viewHolder = new TimeLineViewHolder(itemView);


        itemView.setOnClickListener(v -> {
            final int position = viewHolder.getAdapterPosition();
            //注意第一条数据不能点击
            if (mOnItemClickListener != null && (mDataSet.size() > 0 && position > 0 && position < mDataSet.size())) {
                mOnItemClickListener.onItemLongClick(position, mDataSet.get(position));
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TimeLineViewHolder viewHolder, int position) {
        UploadMsgEntity item = mDataSet.get(position);
        mMessage.setLength(0);
        if (position == 0) {
            //提示信息
            mMessage.append("您一共有" + item.totalTaskNum + "单数据需要上传");
        } else {
            mMessage.append(String.valueOf(item.taskId + 1) + "/" + String.valueOf(item.totalTaskNum))
                    .append(":");
            if (TextUtils.isEmpty(item.refType)) {
                mMessage.append(item.bizTypeDesc);
            } else {
                mMessage.append(item.bizTypeDesc).append("_").append(item.refTypeDesc);
            }
            mMessage.append("\n");
            if (item.isEror) {
                mMessage.append(item.errorMsg);
            } else {
                mMessage.append(item.materialDoc);
                if (!TextUtils.isEmpty(item.transNum)) {
                    mMessage.append("\n").append(item.transNum);
                }
            }
        }
        viewHolder.tvShow.setText(mMessage.toString());
        viewHolder.tvShow.setTextColor(item.isEror ? AppCompat.getColor(R.color.red_400, mContext) :
                AppCompat.getColor(R.color.black, mContext));
    }


    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void setOnItemClickListener(ItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface ItemClickListener {
        void onItemLongClick(int position, UploadMsgEntity info);
    }
}