package com.richfit.barcidesystemproduct.module_acceptstore.qinghai_ww;

import com.richfit.barcodesystemproduct.module_acceptstore.basedetail.BaseASDetailFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qinghai_ww.imp.QingHaiASWWDetialPresenterImp;
import com.richfit.domain.bean.BottomMenuEntity;

import java.util.List;

/**
 * 物资委外入库
 * Created by monday on 2017/2/20.
 */

public class QingHaiASWWDetailFragment extends BaseASDetailFragment<QingHaiASWWDetialPresenterImp> {

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
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
        return "委外入库";
    }
}
