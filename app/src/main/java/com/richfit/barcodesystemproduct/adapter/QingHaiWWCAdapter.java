package com.richfit.barcodesystemproduct.adapter;

import android.content.Context;

import com.richfit.barcodesystemproduct.R;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.basetreerv.CommonTreeAdapter;
import com.richfit.domain.bean.RefDetailEntity;

import java.util.List;

/**
 * 青海委外组件适配器
 * Created by monday on 2017/3/10.
 */

public class QingHaiWWCAdapter extends CommonTreeAdapter<RefDetailEntity> {

    public QingHaiWWCAdapter(Context context, int layoutId,
                             List<RefDetailEntity> allNodes) {
        super(context, layoutId, allNodes);
    }

    @Override
    protected void convert(ViewHolder holder, RefDetailEntity item, int position) {
        holder.setText(R.id.refLineNum, String.valueOf(item.refDocItem))
                .setText(R.id.materialNum, item.materialNum)
                .setText(R.id.materialDesc, item.materialDesc)
                .setText(R.id.batchFlag, item.batchFlag)
                .setText(R.id.totalUsedQuantity, item.totalQuantity);
    }

//    @Override
//    public void notifyParentNodeChanged(int childNodePosition, int parentNodePosition) {
//
//    }
//
//    /**
//     * 明细删除成功后清空累计消耗数量
//     *
//     * @param position
//     */
//    @Override
//    public void notifyNodeChanged(int position) {
//        RefDetailEntity deleteNode = mVisibleNodes.get(position);
//        deleteNode.totalQuantity = "";
//        notifyItemChanged(position);
//    }

}
