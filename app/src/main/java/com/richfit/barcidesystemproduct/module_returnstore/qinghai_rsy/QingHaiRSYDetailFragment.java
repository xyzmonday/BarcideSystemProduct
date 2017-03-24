package com.richfit.barcidesystemproduct.module_returnstore.qinghai_rsy;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.module_acceptstore.basedetail.BaseASDetailFragment;
import com.richfit.barcodesystemproduct.module_returnstore.qinghai_rsy.imp.QingHaiRSYDetailPresenterImp;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.BottomMenuEntity;

import java.util.List;

/**
 * Created by monday on 2017/2/27.
 */

public class QingHaiRSYDetailFragment extends BaseASDetailFragment<QingHaiRSYDetailPresenterImp> {


    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected void initView() {
        tvActQuantity.setText("应退数量");
        super.initView();
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder,int viewType) {
        if(viewType == Global.CHILD_NODE_HEADER_TYPE) {
            viewHolder.setText(R.id.quantity,"实退数量");
        }
    }

    @Override
    public List<BottomMenuEntity> provideDefaultBottomMenu() {
        List<BottomMenuEntity> menus = super.provideDefaultBottomMenu();
        menus.get(0).transToSapFlag = "01";
        menus.get(1).transToSapFlag = "05";
        return menus.subList(0, 2);
    }

    @Override
    protected String getSubFunName() {
        return "转储退库";
    }
}
