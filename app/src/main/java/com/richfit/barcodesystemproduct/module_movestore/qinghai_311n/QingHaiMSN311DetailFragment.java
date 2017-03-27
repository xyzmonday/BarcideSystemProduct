package com.richfit.barcodesystemproduct.module_movestore.qinghai_311n;

import android.text.TextUtils;
import android.view.View;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_detail.BaseMSNDetailFragment;
import com.richfit.barcodesystemproduct.module_movestore.qinghai_311n.imp.QingHaiMSN311DetailPresenterImp;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.domain.bean.BottomMenuEntity;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 没有接收批次
 * Created by monday on 2017/2/16.
 */

public class QingHaiMSN311DetailFragment extends BaseMSNDetailFragment<QingHaiMSN311DetailPresenterImp> {

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected void initView() {
        setVisibility(View.GONE,recBatchFlag);
        super.initView();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int viewType) {
        if(Global.PARENT_NODE_ITEM_TYPE == viewType) {
            holder.setVisible(R.id.recBatchFlag,false);
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

        if (!checkExtraData(mSubFunEntity.headerConfigs, mRefData.mapExt)) {
            showMessage("请先在抬头界面输入必要的信息");
            return;
        }
        super.initDataLazily();
    }

    @Override
    public void showTurnConfirmDialog() {
        if (isNeedTurn && !isTurnSuccess) {
            new SweetAlertDialog(mActivity).setTitleText("温馨提示")
                    .setContentText("您需要先寄售转自有，请点击确定;您也可以点击直接过账，不进行寄售转自有。").setConfirmText("确定")
                    .setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismiss();
                        startTurnOwnSupplies("07");
                    }).setCancelText("直接过账")
                    .setConfirmClickListener(sweetAlertDialog -> {
                        //直接调用过账方法。
                        submit2BarcodeSystem(mBottomMenus.get(0).transToSapFlag);
                    })
                    .show();
            return;
        }
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
