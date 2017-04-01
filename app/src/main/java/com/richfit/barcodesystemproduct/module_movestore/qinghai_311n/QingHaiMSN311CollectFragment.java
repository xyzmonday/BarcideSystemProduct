package com.richfit.barcodesystemproduct.module_movestore.qinghai_311n;

import android.text.TextUtils;
import android.view.View;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_collect.BaseMSNCollectFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_collect.imp.MSNCollectPresenterImp;

/**
 * 工厂内311移库，不需要接收批次。接收仓位默认与发出仓位一致，而且不允许修改
 * Created by monday on 2017/2/16.
 */

public class QingHaiMSN311CollectFragment extends BaseMSNCollectFragment<MSNCollectPresenterImp> {

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected void initView() {
        //工厂内移库不需要接收批次
        setVisibility(View.GONE, llRecBatch);
        //青海的接收仓位默认与发出仓位一致，而且不允许修改
        setVisibility(View.GONE, llRecLocation);
        super.initView();
    }

    /**
     * 如果打开了WM那么需要检查仓库号是否一致。
     * 对于工厂内的转储，没有接收工厂，那么接收工厂Id默认为发出工厂
     */
    @Override
    protected void checkWareHouseNum(int position) {
        if (position <= 0) {
            return;
        }
        final String workId = mRefData.workId;
        final String invCode = mSendInvs.get(position).invCode;
        final String recInvCode = mRefData.recInvCode;
        if (isOpenWM) {
            //没有打开WM，不需要检查ERP仓库号是否一致
            isWareHouseSame = true;
            return;
        }
        if (TextUtils.isEmpty(workId)) {
            showMessage("工厂为空");
            return;
        }
        if (TextUtils.isEmpty(invCode)) {
            showMessage("发出库位为空");
            return;
        }

        if (TextUtils.isEmpty(recInvCode)) {
            showMessage("接收库位为空");
            return;
        }

        mPresenter.checkWareHouseNum(isOpenWM, workId, invCode, workId, recInvCode, getOrgFlag());
    }

    @Override
    protected boolean checkHeaderData() {
        if (TextUtils.isEmpty(mRefData.workId)) {
            showMessage("请先选择工厂");
            return false;
        }

        if (TextUtils.isEmpty(mRefData.invId)) {
            showMessage("请先选择发出库位");
            return false;
        }

        if (TextUtils.isEmpty(mRefData.recInvId)) {
            showMessage("请先选择接收库位");
            return false;
        }
        return true;
    }

    @Override
    public boolean checkCollectedDataBeforeSave() {
        //发出工厂
        if (TextUtils.isEmpty(mRefData.workId)) {
            showMessage("请先选择工厂");
            return false;
        }
        //检查发出批次
        if (mIsOpenBatchManager && TextUtils.isEmpty(getString(etSendBatchFlag))) {
            showMessage("批次为空");
            return false;
        }

        if (spSendLoc.getSelectedItemPosition() <= 0) {
            showMessage("请先选择发出仓位");
            return false;
        }

        return super.checkCollectedDataBeforeSave();
    }

    @Override
    protected String getInvType() {
        return getString(R.string.invTypeNorm);
    }

    @Override
    protected String getInventoryQueryType() {
        return getString(R.string.inventoryQueryTypeSAPLocation);
    }

    @Override
    protected boolean getWMOpenFlag() {
        return false;
    }

    @Override
    protected int getOrgFlag() {
        return 0;
    }
}
