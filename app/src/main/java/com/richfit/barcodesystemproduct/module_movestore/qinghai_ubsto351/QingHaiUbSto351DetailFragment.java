package com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto351;

import android.view.View;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_ms_detail.BaseMSDetailFragment;
import com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto351.imp.QingHaiUnSto351DetailPresenterImp;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.domain.bean.BottomMenuEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 351接收没有接收仓位和接收批次,同时不需要显示发出工厂
 * Created by monday on 2017/2/10.
 */

public class QingHaiUbSto351DetailFragment extends BaseMSDetailFragment<QingHaiUnSto351DetailPresenterImp> {


    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    public void initView() {
        //父节点不显示发出工厂
        setVisibility(View.GONE, tvSendWork);
        super.initView();
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int viewType) {
        if (Global.CHILD_NODE_HEADER_TYPE == viewType) {
            //子节点仅仅显示发出库位和发出批次
            viewHolder.setVisible(R.id.recLocation, false)
                    .setVisible(R.id.recBatchFlag, false);
        }
    }

    @Override
    public List<BottomMenuEntity> provideDefaultBottomMenu() {
        List<BottomMenuEntity> tmp = super.provideDefaultBottomMenu();
        tmp.get(0).transToSapFlag = "01";
        tmp.get(2).transToSapFlag = "05";
        //注意351是发出
        ArrayList menus = new ArrayList();
        menus.add(tmp.get(0));
        menus.add(tmp.get(2));
        return menus;
    }


    @Override
    protected String getSubFunName() {
        return "351移库";
    }


    @Override
    protected boolean checkTransStateBeforeRefresh() {
        String transferKey = (String) SPrefUtil.getData(mBizType + mRefType, "0");
        if ("1".equals(transferKey)) {
            setRefreshing(false, "本次采集已经过账,请直接进行其他转储操作");
            return false;
        }
        return true;
    }

    @Override
    public void refreshComplete() {

    }
}
