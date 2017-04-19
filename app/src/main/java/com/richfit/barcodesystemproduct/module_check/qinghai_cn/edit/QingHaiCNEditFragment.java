package com.richfit.barcodesystemproduct.module_check.qinghai_cn.edit;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.base.base_edit.BaseEditFragment;
import com.richfit.barcodesystemproduct.module_check.qinghai_cn.edit.imp.CNEditPresenterImp;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.utils.CommonUtil;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.ResultEntity;

import butterknife.BindView;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;

/**
 * Created by monday on 2016/12/6.
 */

public class QingHaiCNEditFragment extends BaseEditFragment<CNEditPresenterImp>
        implements ICNEditView {

    @BindView(R.id.et_check_location)
    TextView etCheckLocation;
    @BindView(R.id.ll_check_location)
    LinearLayout llCheckLocation;
    @BindView(R.id.tv_material_num)
    TextView tvMaterialNum;
    @BindView(R.id.tv_material_desc)
    TextView tvMaterialDesc;
    @BindView(R.id.tv_material_group)
    TextView tvMaterialGroup;
    @BindView(R.id.tv_special_inv_flag)
    TextView tvSpecialInvFlag;
    @BindView(R.id.tv_special_inv_num)
    TextView tvSpecialInvNum;
    @BindView(R.id.tv_inv_quantity)
    TextView tvInvQuantity;
    @BindView(R.id.et_check_quantity)
    EditText etCheckQuantity;
    @BindView(R.id.tv_total_quantity)
    TextView tvTotalQuantity;

    String mCheckLineId;
    String mWorkId;
    String mInvId;

    @Override
    protected int getContentId() {
        return R.layout.fragment_cn_edit;
    }

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    public void initData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCheckLineId = bundle.getString(Global.EXTRA_REF_LINE_ID_KEY);
            String materialNum = bundle.getString(Global.EXTRA_MATERIAL_NUM_KEY);
            String materialId = bundle.getString(Global.EXTRA_MATERIAL_ID_KEY);
            String materialGroup = bundle.getString(Global.EXTRA_MATERIAL_GROUP_KEY);
            String materialDesc = bundle.getString(Global.EXTRA_MATERIAL_DESC_KEY);
            String location = bundle.getString(Global.EXTRA_LOCATION_KEY);
            String quantity = bundle.getString(Global.EXTRA_QUANTITY_KEY);
            String invQuantity = bundle.getString(Global.EXTRA_INV_QUANTITY_KEY);
            String specialInvFlag = bundle.getString(Global.EXTRA_SPECIAL_INV_FLAG_KEY);
            String specialInvNum = bundle.getString(Global.EXTRA_SPECIAL_INV_NUM_KEY);
            mWorkId = bundle.getString(Global.EXTRA_WORK_ID_KEY);
            mInvId = bundle.getString(Global.EXTRA_INV_ID_KEY);
            tvMaterialNum.setText(materialNum);
            tvMaterialNum.setTag(materialId);
            tvMaterialDesc.setText(materialDesc);
            tvMaterialGroup.setText(materialGroup);
            tvSpecialInvFlag.setText(specialInvFlag);
            tvSpecialInvNum.setText(specialInvNum);
            etCheckLocation.setText(location);
            tvInvQuantity.setText(invQuantity);
            etCheckQuantity.setText(quantity);
            tvTotalQuantity.setText(quantity);
            //如果是库存级别的不允许修改盘点仓位
            etCheckLocation.setEnabled(false);
            if (mRefData != null && (!TextUtils.isEmpty(mRefData.checkLevel))) {
                if ("02".equals(mRefData.checkLevel)) {
                    llCheckLocation.setVisibility(View.GONE);
                    etCheckLocation.setEnabled(false);
                }
            }
        }
    }

    @Override
    public boolean checkCollectedDataBeforeSave() {
        if (TextUtils.isEmpty(mCheckLineId)) {
            showMessage("该行的盘点Id为空");
            return false;
        }

        final String checkLevel = mRefData.checkLevel;
        if (TextUtils.isEmpty(checkLevel)) {
            showMessage("请先在抬头选择盘点类型");
            return false;
        }

        //如果是库存级需要检查工厂和库存地点
        if ("02".equals(checkLevel) && TextUtils.isEmpty(mWorkId)) {
            showMessage("盘点工厂Id为空");
            return false;
        }


        if ("02".equals(checkLevel) && TextUtils.isEmpty(mInvId)) {
            showMessage("盘点库存地点Id为空");
            return false;
        }
        if (TextUtils.isEmpty(getString(etCheckQuantity))) {
            showMessage("请输入盘点数量");
            return false;
        }
        if (etCheckLocation.isEnabled() && TextUtils.isEmpty(getString(etCheckLocation))) {
            showMessage("请输入盘点仓位");
            return false;
        }

        if (etCheckLocation.isEnabled() && getString(etCheckLocation).length() > 10) {
            showMessage("您输入的盘点仓位不合理");
            return false;
        }

        float quantity = UiUtil.convertToFloat(getString(etCheckQuantity), 0.0F);
        if (quantity <= 0.0F) {
            showMessage("输入盘点数量小于零");
            return false;
        }
        return true;
    }

    @Override
    public void saveCollectedData() {
        Flowable.create((FlowableOnSubscribe<ResultEntity>) emitter -> {
            ResultEntity result = new ResultEntity();
            result.businessType = mRefData.bizType;
            result.checkId = mRefData.checkId;
            result.checkLineId = mCheckLineId;
            result.specialInvFlag = getString(tvSpecialInvFlag);
            result.specialInvNum = getString(tvSpecialInvNum);
            result.location = getString(etCheckLocation);
            result.voucherDate = mRefData.voucherDate;
            result.userId = Global.USER_ID;
            result.workId = mRefData.workId;
            result.invId = mRefData.invId;
            result.materialId = CommonUtil.Obj2String(tvMaterialNum.getTag());
            result.quantity = getString(etCheckQuantity);
            result.modifyFlag = "Y";
            emitter.onNext(result);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER).compose(TransformerHelper.io2main())
                .subscribe(result -> mPresenter.uploadCheckDataSingle(result));
    }

    @Override
    public void saveEditedDataSuccess(String message) {
        super.saveEditedDataSuccess(message);
        tvTotalQuantity.setText(getString(etCheckQuantity));
        etCheckQuantity.setText("");
    }

    @Override
    public void saveEditedDataFail(String message) {
        super.saveEditedDataFail(message);
        etCheckQuantity.setText("");
    }

    @Override
    public void networkConnectError(String retryAction) {
        showNetConnectErrorDialog(retryAction);
    }

    @Override
    public void retry(String retryAction) {
        saveCollectedData();
        super.retry(retryAction);
    }
}
