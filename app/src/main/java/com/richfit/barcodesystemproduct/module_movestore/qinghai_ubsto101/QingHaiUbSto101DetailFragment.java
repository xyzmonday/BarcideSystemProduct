package com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto101;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.module_acceptstore.basedetail.BaseASDetailFragment;
import com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto101.imp.QingHaiUbSto101DetailPresenterImp;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.BottomMenuEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 101转储接收，注意父节点显示的节点字段为接收库位和接收工厂;
 * 子节点字段为接收仓位，接收批次
 * Created by monday on 2017/2/15.
 */

public class QingHaiUbSto101DetailFragment extends BaseASDetailFragment<QingHaiUbSto101DetailPresenterImp> {


    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected void initView() {
        //父节点显示接收库位和接收工厂
        tvInv.setText("接收库位");
        tvWork.setText("接收工厂");
        super.initView();
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int viewType) {
        if (Global.CHILD_NODE_HEADER_TYPE == viewType) {
            //子节点仅仅显示接收仓位和接收批次
            viewHolder.setText(R.id.location, "接收仓位")
                    .setText(R.id.batchFlag, "接收批次");
        }
    }

    @Override
    public List<BottomMenuEntity> provideDefaultBottomMenu() {
        List<BottomMenuEntity> tmp = super.provideDefaultBottomMenu();
        tmp.get(0).transToSapFlag = "01";
        tmp.get(1).transToSapFlag = "05";
        ArrayList menus = new ArrayList();
        menus.add(tmp.get(0));
        menus.add(tmp.get(1));
        return menus;
    }

    @Override
    protected String getSubFunName() {
        return "101移库";
    }


}
