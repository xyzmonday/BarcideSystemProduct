package com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto351;

import android.text.TextUtils;
import android.widget.EditText;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_ms_edit.BaseMSEditFragment;
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
 * Created by monday on 2017/4/14.
 */

public class QingHaiUbSto351LEditFragment extends BaseMSEditFragment {

    @BindView(R.id.et_send_location)
    RichEditText etSendLocation;
    @BindView(R.id.et_special_inv_flag)
    EditText etSpecialInvFlag;
    @BindView(R.id.et_special_inv_num)
    EditText etSpecialInvNum;

    @Override
    public int getContentId() {
        return R.layout.fragment_lmsy_edit;
    }

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    public void initEvent() {
        etSendLocation.setOnRichEditTouchListener((view, location) -> {
            hideKeyboard(view);
            //获取缓存
            String locationCombine = location;
            if (!TextUtils.isEmpty(getString(etSpecialInvFlag)) && !TextUtils.isEmpty(getString(etSpecialInvNum))) {
                locationCombine = location + "_" + getString(etSpecialInvFlag) + "_" + getString(etSpecialInvNum);
            }
            mPresenter.getTransferInfoSingle(mRefData.refCodeId, mRefData.refType,
                    mRefData.bizType, mRefLineId, getString(tvBatchFlag), locationCombine
                    , "", -1, Global.USER_ID);
        });
    }

    @Override
    public void initView() {
        spLocation.setEnabled(false);
    }

    @Override
    public void initData() {
        super.initData();
        etSendLocation.setText(mSelectedLocation);
        etSpecialInvFlag.setText(mSpecialInvFlag);
        etSpecialInvNum.setText(mSpecialInvNum);

        //获取缓存
        String locationCombine = mSelectedLocation;
        if (!TextUtils.isEmpty(getString(etSpecialInvFlag)) && !TextUtils.isEmpty(getString(etSpecialInvNum))) {
            locationCombine = mSelectedLocation + "_" + getString(etSpecialInvFlag) + "_" + getString(etSpecialInvNum);
        }
        mPresenter.getTransferInfoSingle(mRefData.refCodeId, mRefData.refType,
                mRefData.bizType, mRefLineId, getString(tvBatchFlag), locationCombine
                , "", -1, Global.USER_ID);
    }

    @Override
    public boolean checkCollectedDataBeforeSave() {
        //检查是否合理，可以保存修改后的数据
        if (TextUtils.isEmpty(getString(etQuantity))) {
            showMessage("请输入入库数量");
            return false;
        }

        if (Float.parseFloat(getString(etQuantity)) <= 0.0f) {
            showMessage("输入出库数量不合理,请重新输入");
            return false;
        }

        if(TextUtils.isEmpty(getString(etSendLocation)) || getString(etSendLocation).length() > 10) {
            showMessage("您输入的发出仓位不合理");
            return false;
        }

        final String specialInvFlag = getString(etSpecialInvFlag);
        final String specialInvNum = getString(etSpecialInvNum);
        if(!TextUtils.isEmpty(specialInvFlag) && "K".equalsIgnoreCase(specialInvFlag)
                && TextUtils.isEmpty(specialInvNum)) {
            showMessage("请先输入特殊库存编号");
            return false;
        }


        //是否满足本次录入数量+累计数量-上次已经录入的出库数量<=应出数量
        float actQuantityV = UiUtil.convertToFloat(getString(tvActQuantity), 0.0f);
        float totalQuantityV = UiUtil.convertToFloat(getString(tvTotalQuantity), 0.0f);
        float collectedQuantity = UiUtil.convertToFloat(mQuantity, 0.0f);
        //修改后的出库数量
        float quantityV = UiUtil.convertToFloat(getString(etQuantity), 0.0f);
        float residualQuantity = totalQuantityV - collectedQuantity + quantityV;//减去已经录入的数量
        if (Float.compare(residualQuantity, actQuantityV) > 0.0f) {
            showMessage("输入移库数量有误");
            etQuantity.setText("");
            return false;
        }

        mQuantity = quantityV + "";
        mTotalQuantity = residualQuantity;
        return true;
    }

    @Override
    public void saveCollectedData() {
        if (!checkCollectedDataBeforeSave()) {
            return;
        }
        Flowable.create((FlowableOnSubscribe<ResultEntity>) emitter -> {
            RefDetailEntity lineData = mRefData.billDetailList.get(mPosition);
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
            result.locationId = mLocationId;
            result.invId = CommonUtil.Obj2String(tvInv.getTag());
            result.materialId = lineData.materialId;
            result.location = getString(etSendLocation);
            result.specialInvFlag = getString(etSpecialInvFlag);
            result.specialInvNum = getString(etSpecialInvNum);
            result.batchFlag = getString(tvBatchFlag);
            result.quantity = getString(etQuantity);
            result.unit = TextUtils.isEmpty(lineData.recordUnit) ? lineData.materialUnit : lineData.recordUnit;
            result.unitRate = Float.compare(lineData.unitRate, 0.0f) == 0 ? 1.f : 0.f;
            result.modifyFlag = "Y";
            emitter.onNext(result);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER).compose(TransformerHelper.io2main())
                .subscribe(result -> mPresenter.uploadCollectionDataSingle(result));

    }

    @Override
    protected String getInvType() {
        return "01";
    }

    @Override
    protected String getInventoryQueryType() {
        return getString(R.string.inventoryQueryTypeSAPLocation);
    }
}
