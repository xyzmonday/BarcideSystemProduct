package com.richfit.barcodesystemproduct.module_movestore.qinghai_311n;

import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_collect.BaseMSNCollectFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_collect.imp.MSNCollectPresenterImp;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.utils.CommonUtil;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.ResultEntity;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;

/**
 * 工厂内311移库，不需要接收批次。接收仓位默认与发出仓位一致，而且不允许修改
 * Created by monday on 2017/2/16.
 */

public class QingHaiMSN311CollectFragment extends BaseMSNCollectFragment<MSNCollectPresenterImp> {

    //默认都是进行寄售转自有的
    private String specialConvert = "N";

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
    public void showOperationMenuOnCollection(final String companyCode) {
        //每一次保存之前需要重置该字段
        specialConvert = "N";
        boolean isTurn = false;
        final int position = spSendLoc.getSelectedItemPosition();
        if (position >= 0 && !TextUtils.isEmpty(mInventoryDatas.get(position).specialInvFlag)
                && !TextUtils.isEmpty(mInventoryDatas.get(position).specialInvNum)) {
            isTurn = true;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("温馨提示");
        String message = isTurn ? "检测到有寄售库存,您是否要进行寄售转自有" : "您真的确定要保存本次采集的数据?";
        builder.setMessage(message);
        //  第一个按钮
        builder.setPositiveButton("直接保存", (dialog, which) -> {
            dialog.dismiss();
            saveCollectedData();
        });
        if (isTurn) {
            builder.setNeutralButton("寄售转自有", (dialog, which) -> {
                dialog.dismiss();
                specialConvert = "Y";
                saveCollectedData();
            });
        }
        //  第三个按钮
        builder.setNegativeButton("取消保存", (dialog, which) -> dialog.dismiss());
        builder.create().show();
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

    public void saveCollectedData() {
        if (!checkCollectedDataBeforeSave()) {
            return;
        }
        Flowable.create((FlowableOnSubscribe<ResultEntity>) emitter -> {
            ResultEntity result = new ResultEntity();
            result.businessType = mRefData.bizType;
            result.voucherDate = mRefData.voucherDate;
            result.moveType = mRefData.moveType;
            result.userId = Global.USER_ID;
            result.workId = mRefData.workId;
            result.invId = mSendInvs.get(spSendInv.getSelectedItemPosition()).invId;
            result.recWorkId = mRefData.recWorkId;
            result.recInvId = mRefData.recInvId;
            result.materialId = etMaterialNum.getTag().toString();
            result.batchFlag = CommonUtil.toUpperCase(getString(etSendBatchFlag));
            result.recBatchFlag = CommonUtil.toUpperCase(getString(etRecBatchFlag));
            result.recLocation = CommonUtil.toUpperCase(getString(etRecLoc));
            result.quantity = getString(etQuantity);
            result.invType = getInvType();
            result.modifyFlag = "N";

            result.deviceId = mDeviceId;
            int locationPos = spSendLoc.getSelectedItemPosition();
            result.location = mInventoryDatas.get(locationPos).location;
            result.specialInvFlag = mInventoryDatas.get(locationPos).specialInvFlag;
            result.specialInvNum = mInventoryDatas.get(locationPos).specialInvNum;
            result.specialConvert = specialConvert;
            emitter.onNext(result);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER)
                .compose(TransformerHelper.io2main())
                .subscribe(result -> mPresenter.uploadCollectionDataSingle(result));
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
