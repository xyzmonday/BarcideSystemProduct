package com.richfit.barcidesystemproduct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.richfit.barcodesystemproduct.R;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.RefDetailEntity;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 离线单据数据下载界面中数据明细适配器
 * Created by monday on 2017/3/21.
 */

public class LocalRefDataAdapter extends RecyclerView.Adapter<LocalRefDataAdapter.LocalDataViewHolder>
        implements StickyRecyclerHeadersAdapter<LocalRefDataAdapter.LocalHeaderViewHolder> {

    private int mLayoutId;
    private List<RefDetailEntity> mDatas;
    private Context mContext;

    public LocalRefDataAdapter(Context context, int layoutId, List<RefDetailEntity> datas) {
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
    public LocalDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(mLayoutId, parent,
                false);
        LocalDataViewHolder holder = new LocalDataViewHolder(itemView);
        return holder;
    }

    /**
     * 为ViewHolder绑定数据
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(LocalDataViewHolder holder, int position) {
        final RefDetailEntity item = mDatas.get(position);
        holder.rowNum.setText(String.valueOf((position + 1)));
        holder.lineNum.setText(item.lineNum);
        holder.materialNum.setText(item.materialNum);
        holder.materialDesc.setText(item.materialDesc);
        holder.materialGroup.setText(item.materialGroup);
        holder.batchFlag.setText(item.batchFlag);
        holder.totalQuantity.setText(item.totalQuantity);
        holder.actQuantity.setText(item.actQuantity);
        holder.work.setText(item.workCode);
        holder.inv.setText(item.invCode);
    }

    /**
     * 给出stickyHeader的Id,每一个Item的将根据Id与上一个Item的Id是否一致来判断是否需要
     * 生成一个新的stickyHeader
     * @param position
     * @return
     */
    @Override
    public long getHeaderId(int position) {
        //这我们给出的是相同单据号的共享同一个stickyHeader
        char[] chars = mDatas.get(position).recordNum.toCharArray();
        long id = 0L;
        for (char c : chars) {
            id += c;
        }
        return id;
    }

    /**
     * 为stickyHeader生成一个ViewHolder
     * @param parent
     * @return
     */
    @Override
    public LocalHeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_local_data_sticky, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        //注意这里我们需要直接给出stickyHeader宽度，因为一般而言RecyclerView的Item不进行左右滑动。
        //由于需要左右滑动也就是Item的宽度已经超过了屏幕宽度，stickyHeader不能测量出准确的宽度，所以直接给出
        //精确的宽度，这个宽度是通过布局文件手动计算得到的。
        layoutParams.width = UiUtil.dip2px(mContext, 1840);
        return new LocalHeaderViewHolder(view);
    }

    /**
     * 为stickyHeader的ViewHolder绑定数据
     * @param holder
     * @param position
     */
    @Override
    public void onBindHeaderViewHolder(LocalHeaderViewHolder holder, int position) {
        holder.mTvHeader.setText("单据号:" + mDatas.get(position).recordNum);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public static class LocalDataViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.rowNum)
        TextView rowNum;
        @BindView(R.id.lineNum)
        TextView lineNum;
        @BindView(R.id.materialNum)
        TextView materialNum;
        @BindView(R.id.materialDesc)
        TextView materialDesc;
        @BindView(R.id.materialGroup)
        TextView materialGroup;
        @BindView(R.id.batchFlag)
        TextView batchFlag;
        @BindView(R.id.tv_act_quantity)
        TextView actQuantity;
        @BindView(R.id.totalQuantity)
        TextView totalQuantity;
        @BindView(R.id.work)
        TextView work;
        @BindView(R.id.inv)
        TextView inv;

        public LocalDataViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(itemView);
        }
    }

    public static class LocalHeaderViewHolder extends RecyclerView.ViewHolder {

        TextView mTvHeader;

        public LocalHeaderViewHolder(View itemView) {
            super(itemView);
            mTvHeader = (TextView) itemView.findViewById(R.id.tv_sticky_header);
        }
    }

}
