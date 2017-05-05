package com.richfit.barcodesystemproduct.module_locationadjust.collect;

import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.module_locationadjust.collect.imp.LACollectPresenterImp;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.utils.CommonUtil;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.common_lib.widget.RichEditText;
import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.MaterialEntity;
import com.richfit.domain.bean.ResultEntity;

import butterknife.BindView;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;

/**
 * 注意在获取库存的时候，如果启用了WM那么使用04从SAP获取有效库存，
 * 如果没有启用WM，那么使用03获取有效库存
 * Created by monday on 2017/2/7.
 */

public class LACollectFragment extends BaseFragment<LACollectPresenterImp>
        implements ILACollectView {

    @BindView(R.id.et_material_num)
    RichEditText etMaterialNum;
    @BindView(R.id.tv_material_desc)
    TextView tvMaterialDesc;
    @BindView(R.id.tv_material_group)
    TextView tvMaterialGroup;
    @BindView(R.id.tv_material_unit)
    TextView tvMaterialUnit;
    @BindView(R.id.et_batch_flag)
    EditText etBatchFlag;
    @BindView(R.id.et_send_location)
    RichEditText etSendLocation;
    @BindView(R.id.tv_send_inv_quantity)
    TextView tvSendInvQuantity;
    @BindView(R.id.et_rec_location)
    EditText etRecLocation;
    @BindView(R.id.et_adjust_quantity)
    EditText etRecQuantity;

    @Override
    public void handleBarCodeScanResult(String type, String[] list) {
        if (list != null && list.length > 2) {
            final String materialNum = list[Global.MATERIAL_POS];
            final String batchFlag = list[Global.BATCHFALG_POS];
            etMaterialNum.setText(materialNum);
            etBatchFlag.setText(batchFlag);
            loadMaterialInfo(materialNum, batchFlag);
        } else {
            final String location = list[Global.LOCATION_POS];
            //目标仓位
            if (etRecLocation.hasFocus() && etRecLocation.isFocused()) {
                etRecLocation.setText(location);
            } else {
                //源仓位
                etSendLocation.setText(location);
            }
        }
    }


    @Override
    protected int getContentId() {
        return R.layout.fragment_la_collect;
    }

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    public void initDataLazily() {
        super.initDataLazily();
        etMaterialNum.setEnabled(false);
        if (mRefData == null) {
            showMessage("在先在抬头界面选择相关的信息");
            return;
        }

        if (TextUtils.isEmpty(mRefData.workCode)) {
            showMessage("请现在抬头界面选择工厂");
            return;
        }

        if (TextUtils.isEmpty(mRefData.invCode)) {
            showMessage("请先在抬头界面选择库存地点");
            return;
        }
        if (TextUtils.isEmpty(mRefData.storageNum)) {
            showMessage("未获取到仓位号,请重新在太头界面合适的工厂和库存地点");
            return;
        }
        etMaterialNum.setEnabled(true);
    }


    @Override
    public void initEvent() {
        super.initEvent();
        //获取物料
        etMaterialNum.setOnRichEditTouchListener((view, materialNum) -> loadMaterialInfo(materialNum, getString(etBatchFlag)));
        //获取仓位的库存
        etSendLocation.setOnRichEditTouchListener((view, location) -> loadInventoryInfo(location));
    }

    /**
     * 获取物料信息入口
     *
     * @param materialNum
     * @param batchFlag
     */
    private void loadMaterialInfo(String materialNum, String batchFlag) {
        if (TextUtils.isEmpty(materialNum)) {
            showMessage("物料编码为空,请重新输入");
            return;
        }
        clearAllUI();

        mPresenter.getMaterialInfo("01", materialNum);
    }

    @Override
    public void getMaterialInfoSuccess(MaterialEntity materialEntity) {
        etMaterialNum.setTag(materialEntity.id);
        tvMaterialDesc.setText(materialEntity.materialDesc);
        tvMaterialGroup.setText(materialEntity.materialGroup);
        tvMaterialUnit.setText(materialEntity.unit);
        if (TextUtils.isEmpty(getString(etBatchFlag))) {
            etBatchFlag.setText(materialEntity.batchFlag);
        }
    }

    @Override
    public void getMaterialInfoFail(String message) {
        showMessage(message);
    }

    /**
     * 获取库存信息
     *
     * @param location
     */
    protected void loadInventoryInfo(String location) {
        Object tag = etMaterialNum.getTag();
        if (tag == null || TextUtils.isEmpty(tag.toString())) {
            showMessage("请先获取物料信息");
            return;
        }

        if (TextUtils.isEmpty(location)) {
            showMessage("先输入目标仓位");
            return;
        }
        //获取某一仓位的库存，必须检查仓位是否存在
        final String queryType = getString(R.string.inventoryQueryTypeSAPLocation);
//        if ("04".equals(queryType)) {
//            showMessage("仓位为空");
//            return;
//        }

        mPresenter.getInventoryInfo(queryType, mRefData.workId, mRefData.invId, mRefData.workCode,
                mRefData.invCode, mRefData.storageNum, getString(etMaterialNum), tag.toString(),
                "", "", getString(etBatchFlag), location, "", "", getString(R.string.invTypeNorm), "");
    }

    @Override
    public void getInventorySuccess(InventoryEntity inventoryEntity) {
        tvSendInvQuantity.setText(inventoryEntity.invQuantity);
    }

    @Override
    public void getInventoryFail(String message) {
        showMessage(message);
    }

    @Override
    public void saveCollectedDataSuccess(String message) {
        showSuccessDialog(message);
        clearAllUI();
        clearCommonUI(etMaterialNum, etBatchFlag);
    }

    @Override
    public void saveCollectedDataFail(String message) {
        showErrorDialog(message);
    }

    private void clearAllUI() {
        clearCommonUI(tvMaterialDesc, tvMaterialGroup, tvMaterialUnit, etSendLocation,
                tvSendInvQuantity, etRecLocation, etRecQuantity);
    }

    @Override
    public void showOperationMenuOnCollection(final String companyCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("提示");
        builder.setMessage("您真的需要保存数据吗?点击确定将保存数据.");
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("确定", (dialog, which) -> {
            dialog.dismiss();
            saveCollectedData();
        });
        builder.show();
    }

    @Override
    public boolean checkCollectedDataBeforeSave() {
        if (TextUtils.isEmpty(mRefData.bizType)) {
            showMessage("业务类型为空");
            return false;
        }

        if (TextUtils.isEmpty(getString(etBatchFlag))) {
            showMessage("请输入批次");
            return false;
        }

        if (TextUtils.isEmpty(mRefData.workId)) {
            showMessage("请先在抬头界面选择工厂");
            return false;
        }

        if (TextUtils.isEmpty(mRefData.invId)) {
            showMessage("请先在抬头界面选择库存地点");
            return false;
        }
        Object tag = etMaterialNum.getTag();
        if (tag == null || TextUtils.isEmpty(tag.toString())) {
            showMessage("请先获取物料信息");
            return false;
        }

        final String location = getString(etSendLocation);
        if (TextUtils.isEmpty(location) || location.length() > 10) {
            showMessage("目标仓位不合理");
            return false;
        }

        final String recLocation = getString(etRecLocation);
        if (TextUtils.isEmpty(recLocation) || recLocation.length() > 10) {
            showMessage("目标仓位不合理");
            return false;
        }

        if (TextUtils.isEmpty(getString(tvSendInvQuantity))) {
            showMessage("请先获取有效库存");
            return false;
        }

        if (!refreshQuantity(getString(etRecQuantity))) {
            showMessage("调整数量有误");
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
            ResultEntity result = new ResultEntity();
            result.businessType = mRefData.bizType;
            result.batchFlag = CommonUtil.toUpperCase(getString(etBatchFlag));
            result.workId = mRefData.workId;
            result.invId = mRefData.invId;
            result.materialId = CommonUtil.Obj2String(etMaterialNum.getTag());
            result.location = CommonUtil.toUpperCase(getString(etSendLocation));
            result.recLocation = CommonUtil.toUpperCase(getString(etRecLocation));
            result.quantity = getString(etRecQuantity);
            result.userId = Global.USER_ID;
            result.invType = "01";
            emitter.onNext(result);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER).compose(TransformerHelper.io2main())
                .subscribe(result -> mPresenter.uploadCollectionDataSingle(result));
    }

    protected boolean refreshQuantity(final String quantity) {
        if (Float.valueOf(quantity) <= 0.0f) {
            showMessage("输入数量不合理");
            return false;
        }
        final float recQuantityV = UiUtil.convertToFloat(getString(etRecQuantity), 0.0f);
        final float quantityV = UiUtil.convertToFloat(quantity, 0.0f);
        if (Float.compare(quantityV, recQuantityV) > 0.0f) {
            showMessage("输入数量有误，请重新输入");
            return false;
        }
        return true;
    }

    @Override
    public void retry(String action) {
        switch (action) {
            case Global.RETRY_QUERY_MATERIAL_INFO:
                loadMaterialInfo(getString(etMaterialNum), getString(etBatchFlag));
                break;
            case Global.RETRY_LOAD_INVENTORY_ACTION:
                loadInventoryInfo(getString(etRecLocation));
                break;
        }
        super.retry(action);
    }

    @Override
    public void _onPause() {
        super._onPause();
        clearAllUI();
        clearCommonUI(etMaterialNum, etBatchFlag);
    }
}
