package com.richfit.barcodesystemproduct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.richfit.barcodesystemproduct.R;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.ResultEntity;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.List;

/**
 * Created by monday on 2017/4/18.
 */

public class ShowUploadDataAdapter extends RecyclerView.Adapter<ShowUploadDataAdapter.UploadViewHolder>
        implements StickyRecyclerHeadersAdapter<ShowUploadDataAdapter.UploadHeaderViewHolder> {

    private int mLayoutId;
    private List<ResultEntity> mDatas;
    private Context mContext;

    public ShowUploadDataAdapter(Context context, int layoutId, List<ResultEntity> datas) {
        this.mContext = context;
        this.mDatas = datas;
        this.mLayoutId = layoutId;
    }

    /**
     * 创建Item的ViewHolder
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public UploadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(mLayoutId, parent, false);
        UploadViewHolder holder = new UploadViewHolder(itemView);
        return holder;
    }

    /**
     * 为ViewHolder绑定数据
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(UploadViewHolder holder, int position) {
        final ResultEntity item = mDatas.get(position);
        holder.rowNum.setText(String.valueOf((position + 1)));
        holder.lineNum.setText(item.refLineNum);
        holder.materialNum.setText(item.materialNum);
        holder.materialDesc.setText(item.materialDesc);
        holder.materialGroup.setText(item.materialGroup);
        holder.batchFlag.setText(item.batchFlag);
        holder.totalQuantity.setText(item.quantity);
        holder.actQuantity.setText(item.actQuantity);
        holder.work.setText(item.workCode);
        holder.location.setText("barcode".equalsIgnoreCase(item.location) ? "" : item.location);
        holder.inv.setText(item.invCode);
        holder.batchFlag.setText(item.batchFlag);
    }

    /**
     * 给出stickyHeader的Id,每一个Item的将根据Id与上一个Item的Id是否一致来判断是否需要
     * 生成一个新的stickyHeader
     *
     * @param position
     * @return
     */
    @Override
    public long getHeaderId(int position) {
        //这我们给出的是相同单据号的共享同一个stickyHeader
        char[] chars = mDatas.get(position).refCode.toCharArray();
        long id = 0L;
        for (char c : chars) {
            id += c;
        }
        return id;
    }

    /**
     * 为stickyHeader生成一个ViewHolder
     *
     * @param parent
     * @return
     */
    @Override
    public UploadHeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_local_data_sticky, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        //注意这里我们需要直接给出stickyHeader宽度，因为一般而言RecyclerView的Item不进行左右滑动。
        //由于需要左右滑动也就是Item的宽度已经超过了屏幕宽度，stickyHeader不能测量出准确的宽度，所以直接给出
        //精确的宽度，这个宽度是通过布局文件手动计算得到的。
        layoutParams.width = UiUtil.dip2px(mContext, 1840);
        return new UploadHeaderViewHolder(view);
    }

    /**
     * 为stickyHeader的ViewHolder绑定数据
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindHeaderViewHolder(UploadHeaderViewHolder holder, int position) {
        ResultEntity item = mDatas.get(position);
        holder.recordNum.setText(item.refCode);
        holder.bizType.setText(item.businessTypeDesc);
        holder.refType.setText(item.refTypeDesc);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public static class UploadViewHolder extends RecyclerView.ViewHolder {

        TextView rowNum;
        TextView lineNum;
        TextView materialNum;
        TextView materialDesc;
        TextView materialGroup;
        TextView batchFlag;
        TextView actQuantity;
        TextView totalQuantity;
        TextView location;
        TextView work;
        TextView inv;

        public UploadViewHolder(View itemView) {
            super(itemView);
            rowNum = (TextView) itemView.findViewById(R.id.rowNum);
            lineNum = (TextView) itemView.findViewById(R.id.lineNum);
            materialNum = (TextView) itemView.findViewById(R.id.materialNum);
            materialDesc = (TextView) itemView.findViewById(R.id.materialDesc);
            materialGroup = (TextView) itemView.findViewById(R.id.materialGroup);
            batchFlag = (TextView) itemView.findViewById(R.id.batchFlag);
            actQuantity = (TextView) itemView.findViewById(R.id.actQuantity);
            totalQuantity = (TextView) itemView.findViewById(R.id.totalQuantity);
            location = (TextView) itemView.findViewById(R.id.location);
            work = (TextView) itemView.findViewById(R.id.work);
            inv = (TextView) itemView.findViewById(R.id.inv);
        }
    }

    public static class UploadHeaderViewHolder extends RecyclerView.ViewHolder {

        TextView recordNum;
        TextView bizType;
        TextView refType;

        public UploadHeaderViewHolder(View itemView) {
            super(itemView);
            recordNum = (TextView) itemView.findViewById(R.id.recordNum);
            bizType = (TextView) itemView.findViewById(R.id.bizType);
            refType = (TextView) itemView.findViewById(R.id.refType);
        }
    }
}
