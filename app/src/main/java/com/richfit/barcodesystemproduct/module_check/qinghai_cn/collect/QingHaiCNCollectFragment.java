package com.richfit.barcodesystemproduct.module_check.qinghai_cn.collect;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxAdapterView;
import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.module_check.qinghai_cn.collect.imp.CNCollectPresenterImp;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.utils.CommonUtil;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.common_lib.widget.RichEditText;
import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.ResultEntity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;

/**
 * 当盘点级别为02(库存级)是盘点仓位不显示。这里通过盘点仓位的etCheckLocation控件的属性
 * enable来控制是否显示和检查盘点仓位的输入。
 * Created by monday on 2017/3/3.
 */

public class QingHaiCNCollectFragment extends BaseFragment<CNCollectPresenterImp>
        implements ICNCollectView {

    @BindView(R.id.sp_ref_line_num)
    Spinner spRefLine;
    @BindView(R.id.et_check_location)
    EditText etCheckLocation;
    @BindView(R.id.ll_check_location)
    LinearLayout llCheckLocation;
    @BindView(R.id.et_material_num)
    RichEditText etMaterialNum;
    @BindView(R.id.tv_material_desc)
    TextView tvMaterialDesc;
    @BindView(R.id.et_special_inv_flag)
    EditText etSpecialInvFlag;
    @BindView(R.id.et_special_inv_num)
    EditText etSpecialInvNum;
    @BindView(R.id.tv_inv_quantity)
    TextView tvInvQuantity;
    @BindView(R.id.et_quantity)
    EditText etQuantity;
    @BindView(R.id.cb_single)
    CheckBox cbSingle;
    @BindView(R.id.tv_total_quantity)
    TextView tvTotalQuantity;

    List<InventoryEntity> mCurrentInventoryList;
    /*当前匹配的行明细（行号）*/
    ArrayList<String> mRefLines;
    /*单据行适配器*/
    ArrayAdapter<String> mRefLineAdapter;
    /*是否新增标识*/
    boolean isNewFlag = false;

    @Override
    public void handleBarCodeScanResult(String type, String[] list) {
        if (list != null && list.length > 12) {
            final String materialNum = list[Global.MATERIAL_POS];
            if (cbSingle.isChecked() && materialNum.equalsIgnoreCase(getString(etMaterialNum))) {
                saveCollectedData();
            } else if (!cbSingle.isChecked()) {
                etMaterialNum.setText(materialNum);
                getCheckTransferInfoSingle(materialNum, getString(etCheckLocation));
            }

        } else if (list != null && list.length == 2 & !cbSingle.isChecked()) {
            final String location = list[1];
            etCheckLocation.setText("");
            etCheckLocation.setText(location);
        }
    }

    @Override
    protected int getContentId() {
        return R.layout.fragment_cn_collect;
    }

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected void initVariable(@Nullable Bundle savedInstanceState) {
        mRefLines = new ArrayList<>();
    }

    /**
     * 注册所有UI事件
     */
    @Override
    public void initEvent() {
       /*扫描后者手动输入物资条码*/
        etMaterialNum.setOnRichEditTouchListener((view, materialNum) -> {
            hideKeyboard(etMaterialNum);
            getCheckTransferInfoSingle(materialNum, getString(etCheckLocation));
        });
         /*单据行*/
        RxAdapterView
                .itemSelections(spRefLine)
                .filter(position -> position > 0)
                .subscribe(position -> bindCommonCollectUI());
       /*单品(注意单品仅仅控制实收数量，累计数量是由行信息里面控制)*/
        cbSingle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etQuantity.setText(isChecked ? "1" : "");
            etQuantity.setEnabled(!isChecked);
        });
    }

    /**
     * 检查抬头界面的必要的字段是否已经赋值
     */
    @Override
    public void initDataLazily() {
        etMaterialNum.setEnabled(false);
        if (mRefData == null) {
            showMessage("请先在抬头页面初始化本次盘点");
            return;
        }

        if (isEmpty(mRefData.bizType)) {
            showMessage("未获取到业务类型");
            return;
        }

        if (isEmpty(mRefData.checkId)) {
            showMessage("请先在抬头界面初始化本次盘点");
            return;
        }

        if ("01".equals(mRefData.checkLevel) && TextUtils.isEmpty(mRefData.storageNum)) {
            showMessage("请先在抬头界面选择仓库号");
            return;
        }
        if ("02".equals(mRefData.checkLevel) && TextUtils.isEmpty(mRefData.workId)) {
            showMessage("请先在抬头界面选择工厂");
            return;
        }

        if ("02".equals(mRefData.checkLevel) && TextUtils.isEmpty(mRefData.invId)) {
            showMessage("请先在抬头界面选择库存");
            return;
        }

        if (isEmpty(mRefData.voucherDate)) {
            showMessage("请先在抬头界面选择过账日期");
            return;
        }
        String transferKey = (String) SPrefUtil.getData(mBizType, "0");
        if ("1".equals(transferKey)) {
            showMessage("本次采集已经过账,请先到数据明细界面进行数据上传操作");
            return;
        }
        //处理盘点仓位
        final boolean isShowCheckLocation = "02".equals(mRefData.checkLevel);
        llCheckLocation.setVisibility(isShowCheckLocation ? View.GONE : View.VISIBLE);
        etCheckLocation.setEnabled(!isShowCheckLocation);
        //处理特殊标识
        final String specialFlag = mRefData.specialFlag;
        final boolean isEditable = "Y".equals(specialFlag) ? true : false;
        etSpecialInvFlag.setEnabled(isEditable);
        etSpecialInvNum.setEnabled(isEditable);
        etMaterialNum.setEnabled(true);
    }

    @Override
    public void getCheckTransferInfoSingle(String materialNum, String location) {
        if (!etMaterialNum.isEnabled()) {
            return;
        }
        if (TextUtils.isEmpty(materialNum)) {
            showMessage("请输入物资条码");
            return;
        }

        if (etCheckLocation.isEnabled() && TextUtils.isEmpty(location)) {
            showMessage("请先输入盘点仓位");
            return;
        }

        clearAllUI();
        mCurrentInventoryList = null;
        mPresenter.getCheckTransferInfoSingle(mRefData.checkId, location, "01", materialNum, mBizType);
    }

    /**
     * 获取盘点库存成功。保存到内存。
     *
     * @param list
     */
    @Override
    public void loadInventorySuccess(List<InventoryEntity> list) {
        mCurrentInventoryList = list;
    }

    @Override
    public void loadInventoryComplete() {
        if (mCurrentInventoryList != null) {
            ArrayList<String> list = new ArrayList<>();
            for (InventoryEntity item : mCurrentInventoryList) {
                list.add(!TextUtils.isEmpty(item.lineNum) ? item.lineNum : "");
            }
            for (InventoryEntity item : mCurrentInventoryList) {
                if("X".equalsIgnoreCase(item.newFlag)) {
                    isNewFlag = true;
                    break;
                }
            }
            setupRefLineAdapter(list);
        }
    }

    @Override
    public void setupRefLineAdapter(ArrayList<String> refLines) {
        mRefLines.clear();
        mRefLines.add(getString(R.string.default_choose_item));
        if (refLines != null)
            mRefLines.addAll(refLines);

        //如果未查询到提示用户
        if (mRefLines.size() == 1) {
            showMessage("该未查询到该物料,请检查物资编码或者盘点仓位是否正确");
            spRefLine.setSelection(0);
            return;
        }

        //初始化单据行适配器
        if (mRefLineAdapter == null) {
            mRefLineAdapter = new ArrayAdapter<>(mActivity, R.layout.item_simple_sp, mRefLines);
            spRefLine.setAdapter(mRefLineAdapter);

        } else {
            mRefLineAdapter.notifyDataSetChanged();
        }
        //如果多行设置颜色
        spRefLine.setBackgroundColor(ContextCompat.getColor(mActivity, mRefLines.size() >= 3 ?
                R.color.colorPrimary : R.color.white));
        //默认选择第一个
        spRefLine.setSelection(1);
    }

    @Override
    public void loadInventoryFail(String message) {
        mCurrentInventoryList = null;
        showMessage(message);
    }

    /**
     * 将当前盘点的库存明细显示
     */
    @Override
    public void bindCommonCollectUI() {
        //注意这里由于加上了"请选择"，所以需要减去该item
        final int position = spRefLine.getSelectedItemPosition() - 1;
        InventoryEntity data = mCurrentInventoryList.get(position);
        tvMaterialDesc.setText(data.materialDesc);
        tvInvQuantity.setText(data.invQuantity);
        etSpecialInvFlag.setText(data.specialInvFlag);
        etSpecialInvNum.setText(data.specialInvNum);
        tvTotalQuantity.setText(data.totalQuantity);
    }

    @Override
    public void showOperationMenuOnCollection(final String companyCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("提示");
        builder.setMessage("您真的确定要提交本次盘点结果吗?");
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("确定", (dialog, which) -> {
            dialog.dismiss();
            saveCollectedData();
        });
        builder.show();
    }

    @Override
    public boolean checkCollectedDataBeforeSave() {
        final float quantityV = UiUtil.convertToFloat(getString(etQuantity), 0.0F);
        if (Float.compare(quantityV, 0.0f) <= 0.0f) {
            showMessage("您输入的盘点数量不合理");
            return false;
        }
        if (mRefData == null) {
            showMessage("请现在抬头页面初始化本次盘点");
            return false;
        }

        if (TextUtils.isEmpty(mRefData.checkLevel)) {
            showMessage("未获取到盘点级别");
            return false;
        }

        if ("01".equals(mRefData.checkLevel) && TextUtils.isEmpty(mRefData.storageNum)) {
            showMessage("未获取到仓库号");
            return false;
        }
        //如果是新增那么不需要检查单据行
        if (!isNewFlag && spRefLine.getSelectedItemPosition() == 0) {
            showMessage("请选择单据行");
            return false;
        }

        if (TextUtils.isEmpty(getString(tvInvQuantity))) {
            showMessage("请先获取库存数量");
            return false;
        }

        if (isEmpty(mRefData.checkId)) {
            showMessage("请先在抬头界面初始化本次盘点");
            return false;
        }

        if (etCheckLocation.isEnabled() && isEmpty(getString(etCheckLocation))) {
            showMessage("请输入盘点仓位");
            return false;
        }

        if (mCurrentInventoryList == null && mCurrentInventoryList.size() < 1) {
            showMessage("请先获取需要盘点的库存明细");
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
            InventoryEntity data = mCurrentInventoryList.get(spRefLine.getSelectedItemPosition() - 1);
            ResultEntity result = new ResultEntity();
            result.businessType = mRefData.bizType;
            result.checkId = mRefData.checkId;
            result.checkLineId = data.checkLineId;
            result.specialInvFlag = getString(etSpecialInvFlag);
            result.specialInvNum = getString(etSpecialInvNum);
            result.location = CommonUtil.toUpperCase(getString(etCheckLocation));
            result.workId = mRefData.workId;
            result.invId = mRefData.invId;
            result.storageNum = mRefData.storageNum;
            result.voucherDate = mRefData.voucherDate;
            result.userId = Global.USER_ID;
            result.workId = mRefData.workId;
            result.invId = mRefData.invId;
            result.materialId = data.materialId;
            result.quantity = getString(etQuantity);
            result.modifyFlag = "N";
            emitter.onNext(result);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER).compose(TransformerHelper.io2main())
                .subscribe(result -> mPresenter.uploadCheckDataSingle(result));
    }


    @Override
    public void saveCollectedDataSuccess() {
        showMessage("盘点成功");
        final float totalQuantityV = UiUtil.convertToFloat(getString(tvTotalQuantity), 0.0f);
        final float quantityV = UiUtil.convertToFloat(getString(etQuantity), 0.0f);
        tvTotalQuantity.setText(String.valueOf(totalQuantityV + quantityV));
        if (!cbSingle.isChecked()) {
            etQuantity.setText("");
        }
    }

    @Override
    public void saveCollectedDataFail(String message) {
        showMessage(message);
    }

    private void clearAllUI() {
        clearCommonUI(etSpecialInvFlag, etSpecialInvNum, tvMaterialDesc, tvInvQuantity,
                etQuantity, tvTotalQuantity);
        //单据行
        if (mRefLineAdapter != null) {
            mRefLines.clear();
            mRefLineAdapter.notifyDataSetChanged();
            spRefLine.setBackgroundColor(0);
        }
    }

    @Override
    public void _onPause() {
        super._onPause();
        clearAllUI();
        clearCommonUI(etMaterialNum, etCheckLocation);
    }

    @Override
    public void retry(String action) {
        switch (action) {
            case Global.RETRY_LOAD_INVENTORY_ACTION:
                getCheckTransferInfoSingle(getString(etMaterialNum), getString(etCheckLocation));
                break;
            case Global.RETRY_TRANSFER_DATA_ACTION:

                break;
        }
        super.retry(action);
    }
}
