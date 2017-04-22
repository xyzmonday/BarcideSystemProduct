package com.richfit.common_lib.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.View;

import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.richfit.common_lib.R;

public class ErrorListViewHolder extends RecyclerView.ViewHolder {

    private ExpandableTextView mErrorMessage;

    public ErrorListViewHolder(View itemView, int type) {
        super(itemView);
        mErrorMessage = (ExpandableTextView) itemView.findViewById(R.id.tv_error_message);
    }

    public void setData(String data, SparseBooleanArray collapsedStatus,int position) {
        mErrorMessage.setText(data, collapsedStatus, position);
    }
}