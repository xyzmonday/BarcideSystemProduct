package com.richfit.barcodesystemproduct.adapter.itemDelegate;

import com.richfit.barcodesystemproduct.R;
import com.richfit.common_lib.baseadapterrv.base.ItemViewDelegate;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.RefDetailEntity;

/**
 * 标准物资出库父节点抬头的Item代理
 * Created by monday on 2017/3/17.
 */

public class ASYParentHeaderItemDelegate implements ItemViewDelegate<RefDetailEntity> {

    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_asy_detail_parent_header;
    }

    @Override
    public boolean isForViewType(RefDetailEntity item, int position) {
        return item.getViewType() == Global.PARENT_NODE_HEADER_TYPE;
    }

    @Override
    public void convert(ViewHolder holder, RefDetailEntity data, int position) {
        holder.setText(R.id.lineNum, data.lineNum)
                //检验批
                .setText(R.id.insLot, data.insLot)
                //参考单据
                .setText(R.id.refLineNum, data.lineNum105)
                //物料凭证
                .setText(R.id.refDoc, data.refDoc)
                //物料凭证行号
                .setText(R.id.refDocItem, String.valueOf(data.refDocItem))
                //物料号
                .setText(R.id.materialNum, data.materialNum)
                //物资描述
                .setText(R.id.materialDesc, data.materialDesc)
                //物料组
                .setText(R.id.materialGroup, data.materialGroup)
                //计量单位
                .setText(R.id.materialUnit, data.unit)
                //特殊库存标识
                .setText(R.id.specailInventoryFlag, data.specialInvFlag)
                //应收数量
                .setText(R.id.actQuantity, data.actQuantity)
                //累计实收数量
                .setText(R.id.totalQuantity, data.totalQuantity)
                //工厂
                .setText(R.id.work, data.workCode)
                //库存地点
                .setText(R.id.inv, data.invCode);
    }
}
