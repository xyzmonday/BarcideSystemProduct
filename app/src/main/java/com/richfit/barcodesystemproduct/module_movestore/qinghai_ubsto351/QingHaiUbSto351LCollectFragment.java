package com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto351;

import android.text.TextUtils;
import android.widget.EditText;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_ms_collect.BaseMSCollectFragment;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.utils.CommonUtil;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.common_lib.widget.RichEditText;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ResultEntity;

import butterknife.BindView;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;

/**
 * Created by monday on 2017/4/13.
 */

public class QingHaiUbSto351LCollectFragment extends BaseMSCollectFragment {

    @BindView(R.id.et_send_location)
    RichEditText etSendLocation;
    @BindView(R.id.et_special_inv_flag)
    EditText etSpecialInvFlag;
    @BindView(R.id.et_special_inv_num)
    EditText etSpecialInvNum;


    @Override
    public void initInjector() {

    }

    @Override
    protected void initView() {
        spSendLoc.setEnabled(false);
        super.initView();
    }

    @Override
    public void initEvent() {
        super.initEvent();
        etSendLocation.setOnRichEditTouchListener((view, location) -> {
            hideKeyboard(view);
            checkLocation(getString(etSendBatchFlag), location);
        });
    }

    private void checkLocation(String batchFlag, String location) {
        tvLocQuantity.setText("");
        isLocationChecked = false;
        if (spRefLine.getSelectedItemPosition() <= 0) {
            showMessage("请先获取物料信息");
            return;
        }

        if (TextUtils.isEmpty(location)) {
            showMessage("请先输入发出仓位");
            return;
        }

        final String invId = mInvDatas.get(spSendInv.getSelectedItemPosition()).invId;
        mPresenter.checkLocation("04", mRefData.workId, invId, batchFlag, location);
    }

    @Override
    public void checkLocationSuccess(String batchFlag, String location) {
        isLocationChecked = true;
        getTransferSingle(-1);
    }

    @Override
    protected void getTransferSingle(int position) {

        final String batchFlag = getString(etSendBatchFlag);
        final String location = getString(etSendLocation);
        final String specialInvFlag = getString(etSpecialInvFlag);
        final String specialInvNum = getString(etSpecialInvNum);

        String locationCombine = location;
        if (!TextUtils.isEmpty(specialInvFlag) && !TextUtils.isEmpty(specialInvNum)) {
            locationCombine = location + "_" + specialInvFlag + "_" + specialInvNum;
        }
        if (spRefLine.getSelectedItemPosition() <= 0) {
            showMessage("请先选择单据行");
            return;
        }
        //检验是否选择了库存地点
        if (spSendInv.getSelectedItemPosition() <= 0) {
            showMessage("请先选择库存地点");
            return;
        }

        if (mIsOpenBatchManager)
            if (TextUtils.isEmpty(batchFlag)) {
                showMessage("请先输入批次");
                return;
            }
        if (TextUtils.isEmpty(locationCombine)) {
            showMessage("请先输入发出仓位");
            return;
        }

        final RefDetailEntity lineData = getLineData(mSelectedRefLineNum);
        final String refCodeId = mRefData.refCodeId;
        final String refType = mRefData.refType;
        final String bizType = mRefData.bizType;
        final String refLineId = lineData.refLineId;
        mCachedBatchFlag = "";
        mCachedExtraLocationMap = null;
        mPresenter.getTransferInfoSingle(refCodeId, refType, bizType, refLineId,
                batchFlag, locationCombine, lineData.refDoc, UiUtil.convertToInt(lineData.refDocItem), Global.USER_ID);
    }


    @Override
    public boolean checkCollectedDataBeforeSave() {
        //检查数据是否可以保存
        if (spRefLine.getSelectedItemPosition() <= 0) {
            showMessage("请先选择单据行");
            return false;
        }
        //物资条码
        if (TextUtils.isEmpty(getString(etMaterialNum))) {
            showMessage("请先输入物料条码");
            return false;
        }
        //发出库位
        if (spSendInv.getSelectedItemPosition() <= 0) {
            showMessage("请先选择库存地点");
            return false;
        }

        //批次
        if (mIsOpenBatchManager && !isBatchValidate) {
            showMessage("批次输入有误，请检查批次是否与缓存批次输入一致");
            return false;
        }

        if (!isLocationChecked) {
            showMessage("请输入的发出仓位不存在");
            return false;
        }

        //发出仓位
        if (TextUtils.isEmpty(getString(etSendLocation)) || getString(etSendLocation).length() > 10) {
            showMessage("您输入的发出仓位不合理");
            return false;
        }

        //实发数量
        if (TextUtils.isEmpty(getString(etQuantity))) {
            showMessage("请先输入数量");
            return false;
        }

        if (Float.valueOf(getString(etQuantity)) < 0.0f) {
            showMessage("移库数量不合理");
            return false;
        }

        return true;
    }

    @Override
    public void saveCollectedData() {
        if (!checkCollectedDataBeforeSave()) {
            return;
        }
        Flowable.create((FlowableOnSubscribe<ResultEntity>) emitter -> {
            RefDetailEntity lineData = getLineData(mSelectedRefLineNum);
            ResultEntity result = new ResultEntity();
            result.businessType = mRefData.bizType;
            result.refCodeId = mRefData.refCodeId;
            result.refCode = mRefData.recordNum;
            result.refLineNum = lineData.lineNum;
            result.voucherDate = mRefData.voucherDate;
            result.refType = mRefData.refType;
            result.moveType = mRefData.moveType;
            result.userId = Global.USER_ID;
            result.refLineId = lineData.refLineId;
            result.workId = lineData.workId;
            result.invId = mInvDatas.get(spSendInv.getSelectedItemPosition()).invId;
            result.materialId = lineData.materialId;
            result.batchFlag = CommonUtil.toUpperCase(getString(etSendBatchFlag));
            result.recBatchFlag = CommonUtil.toUpperCase(getString(etRecBatchFlag));
            result.recLocation = CommonUtil.toUpperCase(getString(etRecLoc));
            result.quantity = getString(etQuantity);
            result.location = getString(etSendLocation);
            result.specialInvFlag = getString(etSpecialInvFlag);
            result.specialInvNum = getString(etSpecialInvNum);
            result.modifyFlag = "N";
            emitter.onNext(result);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER).compose(TransformerHelper.io2main())
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
    protected int getOrgFlag() {
        return getInteger(R.integer.orgNorm);
    }
}
