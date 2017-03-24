package com.richfit.barcidesystemproduct.adapter.itemDelegate;

import com.richfit.barcodesystemproduct.R;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.domain.bean.RefDetailEntity;

public class QingHaiAS105ParentHeaderItemDelegate extends ASYParentHeaderItemDelegate {

    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_qinghai_as105_parent_header;
    }


    @Override
    public void convert(ViewHolder holder, RefDetailEntity data, int position) {
        //绑定标志入库的数据
        super.convert(holder, data, position);
        //退货交货数量
        holder.setText(R.id.returnDeliveryQuantity, data.returnQuantity)
                //项目文本
                .setText(R.id.projectText, data.projectText)
                //移动原因说明
                .setText(R.id.moveCauseDesc, data.moveCauseDesc)
                //移动原因
                .setText(R.id.moveCause, data.moveCause)
                //决策代码
                .setText(R.id.strategyCode, data.decisionCode);
    }
}
