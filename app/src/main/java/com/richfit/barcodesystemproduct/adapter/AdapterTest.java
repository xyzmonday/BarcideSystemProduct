package com.richfit.barcodesystemproduct.adapter;

import android.content.Context;

import com.richfit.barcodesystemproduct.R;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.basetreerv.CommonTreeAdapter;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.RowConfig;

import java.util.List;
import java.util.Map;

/**
 * Created by monday on 2017/3/18.
 */

public class AdapterTest extends CommonTreeAdapter<RefDetailEntity> {

    public AdapterTest(Context context, int layoutId, List<RefDetailEntity> allNodes, List<RowConfig> parentNodeConfigs, List<RowConfig> childNodeConfigs) {
        super(context, layoutId, allNodes, parentNodeConfigs, childNodeConfigs);
    }

    @Override
    protected void convert(ViewHolder holder, RefDetailEntity data, int position) {
        holder.setText(R.id.rowNum, (position + 1) + "");
        holder.setText(R.id.lineNum, data.lineNum);
        //检验批
        holder.setText(R.id.insLot, data.insLot);
        //参考单据行
        holder.setText(R.id.refLineNum, data.lineNum105);
        holder.setText(R.id.materialNum, data.materialNum);
        holder.setText(R.id.materialDesc, data.materialDesc);
        holder.setText(R.id.materialGroup, data.materialGroup);
        holder.setText(R.id.materialUnit, data.unit);
        //应收数量
        holder.setText(R.id.actQuantity, data.actQuantity);
        //累计实收数量
        holder.setText(R.id.totalQuantity, data.totalQuantity);
        //工厂
        holder.setText(R.id.work, data.workCode);
        //库存地点
        holder.setText(R.id.inv, data.invCode);

    }

    @Override
    public void notifyParentNodeChanged(int childNodePosition, int parentNodePosition) {

    }

    @Override
    public void notifyNodeChanged(int position) {

    }

    @Override
    protected Map<String, Object> provideExtraData(int position) {
        return null;
    }
}
