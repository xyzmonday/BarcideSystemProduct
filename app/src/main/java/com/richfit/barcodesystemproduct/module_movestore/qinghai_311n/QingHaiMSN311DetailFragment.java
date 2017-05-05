package com.richfit.barcodesystemproduct.module_movestore.qinghai_311n;

import android.text.TextUtils;
import android.view.View;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_detail.BaseMSNDetailFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_detail.imp.MSNDetailPresenterImp;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.domain.bean.BottomMenuEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 没有接收批次
 * Created by monday on 2017/2/16.
 */

public class QingHaiMSN311DetailFragment extends BaseMSNDetailFragment<MSNDetailPresenterImp> {

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected void initView() {
        setVisibility(View.GONE, recBatchFlag);
        super.initView();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int viewType) {
        if (Global.PARENT_NODE_ITEM_TYPE == viewType) {
            holder.setVisible(R.id.recBatchFlag, false);
        }
    }

    @Override
    public void initDataLazily() {
        if (mRefData == null) {
            setRefreshing(false, "获取明细失败,请现在抬头界面选择相应的参数");
            return;
        }

        if (TextUtils.isEmpty(mRefData.workId) ||
                TextUtils.isEmpty(mRefData.recWorkId)) {
            setRefreshing(false, "获取明细失败,请先选择发出工厂和接收工厂");
            return;
        }

        if (TextUtils.isEmpty(mRefData.recInvId)) {
            setRefreshing(false, "获取明细失败,请先选择接收库位");
            return;
        }
        //注意这必须调用父类的方法将寄售转自有的相关标识清空
        super.initDataLazily();
    }

    /**
     * 1.过账
     */
    protected void submit2BarcodeSystem(String transToSapFlag) {
        //如果需要寄售转自有但是没有成功过，都需要用户需要再次寄售转自有
        if (isNeedTurn && !isTurnSuccess) {
            startTurnOwnSupplies("07");
            return;
        }
        String transferFlag = (String) SPrefUtil.getData(mBizType, "0");
        if ("1".equals(transferFlag)) {
            showMessage("本次采集已经过账,请先进行其他转储操作");
            return;
        }
        mPresenter.submitData2BarcodeSystem(mTransId, mRefData.bizType, mRefType, Global.USER_ID,
                mRefData.voucherDate, transToSapFlag, null);
    }

    @Override
    public List<BottomMenuEntity> provideDefaultBottomMenu() {
        List<BottomMenuEntity> tmp = super.provideDefaultBottomMenu();
        tmp.get(0).transToSapFlag = "01";
        tmp.get(3).transToSapFlag = "05";
        ArrayList menus = new ArrayList();
        menus.add(tmp.get(0));
        menus.add(tmp.get(3));
        return menus;
    }

    @Override
    protected boolean checkTransStateBeforeRefresh() {
        String transferKey = (String) SPrefUtil.getData(mBizType, "0");
        if ("1".equals(transferKey)) {
            setRefreshing(false, "本次采集已经过账,请直接进行记账更改");
            return false;
        }
        return true;
    }

    @Override
    protected String getSubFunName() {
        return "311无参考";
    }
}
