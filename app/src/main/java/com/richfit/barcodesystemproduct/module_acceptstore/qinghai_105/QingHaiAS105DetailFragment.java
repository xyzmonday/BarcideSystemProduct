package com.richfit.barcodesystemproduct.module_acceptstore.qinghai_105;


import android.view.View;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.QingHaiAS105DetailAdapter;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_detail.BaseASDetailFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_detail.imp.ASDetailPresenterImp;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.BottomMenuEntity;
import com.richfit.domain.bean.RefDetailEntity;

import java.util.List;

/**
 * 青海105必检物资入库的明细界面，注意它与标准的明细界面需要显示
 * 检验批，物料凭证号，物料凭证行号，退货交货数量，移动原因，移动原因说明，项目文本,
 * 以及决策代码
 * Created by monday on 2017/3/7.
 */

public class QingHaiAS105DetailFragment extends BaseASDetailFragment<ASDetailPresenterImp> {

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    /**
     * 重写方法，给出105必检的明细界面布局。它与标准入库的最大区别在于，105需要
     * 多显示检验批，物料凭证号，物料凭证行号，退货交货数量，移动原因，移动原因说明，项目文本，决策代码
     * 字段。注意检验批，物料凭证号，物料凭证行号在表中入库中，只是隐藏了。所以这里我们只需要将
     * 这些字段的visibility属性重新设置即可。注意为了考虑UI的性能问题，我们没有将退货交货数量，
     * 移动原因，移动原因说明，项目文本这些字段加入到标准入库中。
     *
     * @return
     */
    @Override
    protected int getContentId() {
        return R.layout.fragment_qinghai_as105_detail;
    }

    @Override
    protected void initView() {
        setVisibility(View.VISIBLE, tvInsLot, tvRefDoc, tvRefDocItem);
        super.initView();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int viewType) {
        if (Global.PARENT_NODE_HEADER_TYPE == viewType) {
            //显示检验批，物料凭证号，物料凭证行号
            holder.setVisible(R.id.insLot, true)
                    .setVisible(R.id.refDoc, true)
                    .setVisible(R.id.refDocItem, true);
        }
    }

    /**
     * 105必检入库与标准入库的明细界面多出很多字段，我们需要重写该方法。
     * 注意这由于我们给出了新的适配器，所以不需要回调onBindViewHolder方法。
     *
     * @param allNodes
     */
    @Override
    public void showNodes(List<RefDetailEntity> allNodes) {
        saveTransId(allNodes);
        if (mAdapter == null) {
            mAdapter = new QingHaiAS105DetailAdapter(mActivity, allNodes, mSubFunEntity.parentNodeConfigs,
                    mSubFunEntity.childNodeConfigs);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemEditAndDeleteListener(this);
            mAdapter.setAdapterStateListener(this);
        } else {
            mAdapter.addAll(allNodes);
        }
    }

    @Override
    protected String getSubFunName() {
        return "物资入库-105必检";
    }

    @Override
    public List<BottomMenuEntity> provideDefaultBottomMenu() {
        List<BottomMenuEntity> menus = super.provideDefaultBottomMenu();
        menus.get(0).transToSapFlag = "Z02";
        menus.get(1).transToSapFlag = "Z03";
        return menus.subList(0, 2);
    }
}
