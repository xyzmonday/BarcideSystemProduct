package com.richfit.barcodesystemproduct.module_acceptstore.qinghai_105n;

import android.view.View;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_detail.BaseASDetailFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_detail.imp.ASDetailPresenterImp;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.BottomMenuEntity;

import java.util.List;

/**
 * 青海物资入库105费非必检数据明细界面。与标准入库的区别在于显示参考单据行
 * Created by monday on 2017/2/20.
 */

public class QingHaiAS105NDetailFragment extends BaseASDetailFragment<ASDetailPresenterImp> {

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected void initView() {
        //显示参考单据行
        setVisibility(View.VISIBLE,tvRefLineNum);
        super.initView();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int viewType) {
        if(Global.PARENT_NODE_HEADER_TYPE == viewType) {
            //显示参考单据行
            holder.setVisible(R.id.refLineNum,true);
        }
    }

    @Override
    protected String getSubFunName() {
        return "物资出库105-非必检";
    }

    @Override
    public List<BottomMenuEntity> provideDefaultBottomMenu() {
        List<BottomMenuEntity> menus = super.provideDefaultBottomMenu();
        menus.get(0).transToSapFlag = "01";
        menus.get(1).transToSapFlag = "05";
        return menus.subList(0, 2);
    }
}
