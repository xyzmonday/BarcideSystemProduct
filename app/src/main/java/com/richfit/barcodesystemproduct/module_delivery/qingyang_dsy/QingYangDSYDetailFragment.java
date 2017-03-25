package com.richfit.barcodesystemproduct.module_delivery.qingyang_dsy;

import com.richfit.barcodesystemproduct.module_delivery.basedetail.BaseDSDetailFragment;
import com.richfit.barcodesystemproduct.module_delivery.basedetail.imp.DSDetailPresenterImp;
import com.richfit.domain.bean.BottomMenuEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by monday on 2017/1/17.
 */

public class QingYangDSYDetailFragment extends BaseDSDetailFragment<DSDetailPresenterImp>{

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    /**
     * 注意庆阳的物资有参考出库目前给出的标准的过账和下架操作
     * @return
     */
    @Override
    public List<BottomMenuEntity> provideDefaultBottomMenu() {
        List<BottomMenuEntity> tmp = super.provideDefaultBottomMenu();
        ArrayList menus = new ArrayList();
        menus.add(tmp.get(0));
        menus.add(tmp.get(2));
        return menus;
    }

    @Override
    protected String getSubFunName() {
        return "物资出库";
    }
}
