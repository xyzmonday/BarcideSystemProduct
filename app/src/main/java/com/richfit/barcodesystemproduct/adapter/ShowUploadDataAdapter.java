package com.richfit.barcodesystemproduct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
//        holder.actQuantity.setText(item.actQuantity);
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

        //这我们给出的是业务类型的共享同一个stickyHeader
        String businessType = mDatas.get(position).businessType;
        String workId = mDatas.get(position).workId;
        String invId = mDatas.get(position).invId;
        String storageNum = mDatas.get(position).storageNum;
        String recInvId = mDatas.get(position).recInvId;
        String refNum = mDatas.get(position).refCode;
        String checkLevel = mDatas.get(position).checkLevel;
        if (TextUtils.isEmpty(businessType)) {
            return -1;
        }
        char[] chars = null;
        switch (businessType) {
            //特别处理盘点
            case "C01":
            case "C02":
                //如果选择的库存级，那么直接使用业务类型+工厂+库存地点(注意这里是无仓考)
                chars = !TextUtils.isEmpty(storageNum) ?
                        (businessType + storageNum).toCharArray() : (businessType + workId + invId).toCharArray();
                break;
            case "16":// 其他入库-无参考
            case "25":// 其他出库-无参考
            case "26":// 无参考-201
            case "27":// 无参考-221
            case "32":// 301(无参考)
            case "34":// 311(无参考)
            case "44":// 其他退库-无参考
            case "46":// 无参考-202
            case "47":// 无参考-222
            case "71":// 代管料入库
            case "72":// 代管料出库
            case "73":// 代管料退库
            case "74":// 代管料调拨
            case "91":// 代管料入库-HRM
            case "92":// 代管料出库-HRM
            case "93":// 代管料退库-HRM
            case "94":// 代管料调拨-HRM
                chars = TextUtils.isEmpty(recInvId) ? (businessType + workId + invId).toCharArray() :
                        (businessType + workId + invId + recInvId).toCharArray();
                break;
            default:
                //对于有参考使用单据+业务类型
                chars = (refNum + businessType).toCharArray();
        }
        if (chars == null)
            return -1;
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
        if (TextUtils.isEmpty(item.businessType))
            return;
        switch (item.businessType) {
            case "C01":
            case "C02":
                //只显示工厂，库位，仓库
                holder.recordNum.setVisibility(View.GONE);
                holder.refType.setVisibility(View.GONE);
                holder.bizType.setText(item.businessTypeDesc);
                holder.work.setText(item.workCode);
                holder.inv.setText(item.invCode);
                holder.storageNum.setText(item.storageNum);
                break;
            case "11":// 采购入库-101
            case "12":// 采购入库-103
            case "13":// 采购入库-105(非必检)
            case "19":// 委外入库
            case "19_ZJ":// 委外入库-组件
            case "110":// 采购入库-105(必检)
            case "21":// 销售出库
            case "23":// 委外发料
            case "24":// 其他出库-有参考
            case "38":// UB 351
            case "311":// UB 101
            case "45":// UB 352
            case "51":// 采购退货
            case "00"://验收结果录入
            case "01"://SAP验收结果录入
                holder.recordNum.setText(item.refCode);
                holder.refType.setText(item.refTypeDesc);
                holder.bizType.setText(item.businessTypeDesc);
                break;
            case "16":// 其他入库-无参考
            case "25":// 其他出库-无参考
            case "26":// 无参考-201
            case "27":// 无参考-221
            case "32":// 301(无参考)
            case "34":// 311(无参考)
            case "44":// 其他退库-无参考
            case "46":// 无参考-202
            case "47":// 无参考-222
            case "71":// 代管料入库
            case "72":// 代管料出库
            case "73":// 代管料退库
            case "74":// 代管料调拨
            case "91":// 代管料入库-HRM
            case "92":// 代管料出库-HRM
            case "93":// 代管料退库-HRM
            case "94":// 代管料调拨-HRM
                holder.recordNum.setText(item.refCode);
                holder.refType.setText(item.refTypeDesc);
                holder.bizType.setText(item.businessTypeDesc);
                holder.work.setText(item.workCode);
                holder.inv.setText(item.invCode);
                break;
        }

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
        TextView work;
        TextView inv;
        TextView storageNum;

        public UploadHeaderViewHolder(View itemView) {
            super(itemView);
            recordNum = (TextView) itemView.findViewById(R.id.recordNum);
            bizType = (TextView) itemView.findViewById(R.id.bizType);
            refType = (TextView) itemView.findViewById(R.id.refType);
            work = (TextView) itemView.findViewById(R.id.work);
            inv = (TextView) itemView.findViewById(R.id.inv);
            storageNum = (TextView) itemView.findViewById(R.id.storageNum);
        }
    }
}
