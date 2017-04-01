package com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto101;

import android.text.TextUtils;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_collect.BaseASCollectFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_collect.imp.ASCollectPresenterImp;
import com.richfit.domain.bean.RefDetailEntity;

/**
 * 工厂，仓位，批次统一对应接收工厂，接收仓位，接收批次。
 * 接收批次非必输
 * Created by monday on 2017/2/15.
 */

public class QingHaiUbSto101CollectFragment extends BaseASCollectFragment<ASCollectPresenterImp> {

    @Override
    protected void initView() {
        tvWorkName.setText("接收工厂");
        tvBatchFlagName.setText("接收批次");
        tvLocationName.setText("接收仓位");
        super.initView();
    }

    @Override
    public void initEvent() {
        super.initEvent();
        etLocation.setOnRichEditTouchListener((view, location) -> {
            hideKeyboard(view);
            getTransferSingle(getString(etBatchFlag), location);
        });
    }

    /**
     * 重写获取单条缓存，不检查批次
     *
     * @param batchFlag
     * @param location
     */
    @Override
    protected void getTransferSingle(String batchFlag, String location) {

        if (spRefLine.getSelectedItemPosition() == 0) {
            showMessage("请先选择单据行");
            return;
        }
        //检验是否选择了库存地点
        if (spInv.getSelectedItemPosition() == 0) {
            showMessage("请先选择库存地点");
            return;
        }

        if (TextUtils.isEmpty(location)) {
            showMessage("请先输入上架仓位");
            return;
        }

        final RefDetailEntity lineData = getLineData(mSelectedRefLineNum);
        final String invId = mInvDatas.get(spInv.getSelectedItemPosition()).invId;
        mPresenter.checkLocation("04", lineData.workId, invId, batchFlag, location);
    }

    @Override
    public boolean checkCollectedDataBeforeSave() {
        if (!isLocationChecked) {
            showMessage("您输入的仓位不存在");
            return false;
        }

        if (mRefData == null) {
            showMessage("请先在抬头界面获取单据数据");
            return false;
        }

        if (TextUtils.isEmpty(mSelectedRefLineNum)) {
            showMessage("请先获取物料信息");
            return false;
        }

        //检查数据是否可以保存
        if (spRefLine.getSelectedItemPosition() <= 0) {
            showMessage("请先选择单据行");
            return false;
        }
        //库存地点
        if (spInv.getSelectedItemPosition() <= 0) {
            showMessage("请先选择库存地点");
            return false;
        }

        //物资条码
        if (TextUtils.isEmpty(getString(etMaterialNum))) {
            showMessage("请先输入物料条码");
            return false;
        }

        //这里不检查是否输入了批次，但是需要检查批次必须一致
        if (mIsOpenBatchManager && !isQmFlag && !isBatchValidate) {
            showMessage("批次输入有误，请检查批次是否与缓存批次输入一致");
            return false;
        }
        if (mIsOpenBatchManager && !etBatchFlag.isEnabled() && !isBatchValidate) {
            showMessage("批次输入有误，请检查批次是否与缓存批次输入一致");
            return false;
        }

        //实发数量
        if (!cbSingle.isChecked() && TextUtils.isEmpty(getString(etQuantity))) {
            showMessage("请先输入实发数量");
            return false;
        }

        if (!refreshQuantity(cbSingle.isChecked() ? "1" : getString(etQuantity))) {
            return false;
        }

        //检查额外字段是否合格
        if (!checkExtraData(mSubFunEntity.collectionConfigs)) {
            showMessage("请检查输入数据");
            return false;
        }

        if (!checkExtraData(mSubFunEntity.locationConfigs)) {
            showMessage("请检查输入数据");
            return false;
        }
        return true;
    }

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected int getOrgFlag() {
        return 0;
    }
}
