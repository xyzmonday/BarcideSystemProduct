package com.richfit.barcodesystemproduct.adapter.itemDelegate;

import com.richfit.barcodesystemproduct.R;
import com.richfit.common_lib.baseadapterrv.base.ItemViewDelegate;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.RefDetailEntity;

/**
 * Created by monday on 2017/5/25.
 */

public class LocQTParentHeaderItemDelegate implements ItemViewDelegate<RefDetailEntity> {
    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_locqt_detail_parent_header;
    }

    @Override
    public boolean isForViewType(RefDetailEntity item, int position) {
        return item.getViewType() == Global.PARENT_NODE_HEADER_TYPE;
    }

    @Override
    public void convert(ViewHolder holder, RefDetailEntity data, int position) {
        holder.setText(R.id.lineNum, data.lineNum)
                //物料号
                .setText(R.id.materialNum, data.materialNum)
                //物资描述
                .setText(R.id.materialDesc, data.materialDesc)
                //物料组
                .setText(R.id.materialGroup, data.materialGroup)
                //计量单位
                .setText(R.id.materialUnit, data.unit)
                //单据数量
                .setText(R.id.orderQuantity, data.orderQuantity)
                //允许进行上下架的数量
                .setText(R.id.actQuantity, data.actQuantity)
                //累计上下架数量
                .setText(R.id.totalQuantity, data.totalQuantity)
                //特殊库存标识
                .setText(R.id.specailInventoryFlag, data.specialInvFlag)
                //库存类型
                .setText(R.id.invType, data.invType)
                //工厂
                .setText(R.id.work, data.workCode)
                //库存地点
                .setText(R.id.inv, data.invCode);
    }
}
