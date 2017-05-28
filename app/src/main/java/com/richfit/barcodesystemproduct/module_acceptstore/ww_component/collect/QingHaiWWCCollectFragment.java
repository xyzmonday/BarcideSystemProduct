package com.richfit.barcodesystemproduct.module_acceptstore.ww_component.collect;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxAdapterView;
import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.WWCInventoryAdapter;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ResultEntity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;

import static com.richfit.barcodesystemproduct.R.id.act_quantity_name;

/**
 * 通过明细里面的lineNum初始化单据行，选择某一行后那么获取库存，选择批次获取单条缓存
 * Created by monday on 2017/3/10.
 */

public class QingHaiWWCCollectFragment extends BaseFragment<QingHaiWWCCollectPresenterImp>
        implements QingHaiWWCCollectContract.QingHaiWWCCollectView {


    @BindView(R.id.sp_ref_line_num)
    Spinner spRefLine;
    @BindView(R.id.tv_material_num)
    TextView tvMaterialNum;
    @BindView(R.id.tv_material_desc)
    TextView tvMaterialDesc;
    @BindView(R.id.tv_special_inv_flag)
    TextView tvSpecialInvFlag;
    @BindView(R.id.tv_work_name)
    TextView tvWorkName;
    @BindView(R.id.tv_work)
    TextView tvWork;
    @BindView(act_quantity_name)
    TextView actQuantityName;
    @BindView(R.id.tv_act_quantity)
    TextView tvActQuantity;
    @BindView(R.id.sp_batch_flag)
    Spinner spBatchFlag;
    @BindView(R.id.quantity_name)
    TextView quantityName;
    @BindView(R.id.et_quantity)
    EditText etQuantity;
    @BindView(R.id.tv_total_quantity)
    TextView tvTotalQuantity;

    String mSelectedRefLineNum;
    ArrayList<String> mRefLines;
    ArrayAdapter<String> mRefLineAdapter;
    List<InventoryEntity> mInventoryDatas;
    WWCInventoryAdapter mInventoryAdapter;

    @Override
    protected int getContentId() {
        return R.layout.fragment_qinghai_wwc_collect;
    }

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected void initVariable(@Nullable Bundle savedInstanceState) {
        mInventoryDatas = new ArrayList<>();
    }

    @Override
    public void initEvent() {
        /*单据行*/
        RxAdapterView
                .itemSelections(spRefLine)
                .filter(position -> position > 0)
                .subscribe(position -> bindCommonCollectUI());

        /*选择批次获获取缓存，初始化仓位数量*/
        RxAdapterView.itemSelections(spBatchFlag)
                .filter(position -> position > 0)
                .subscribe(position -> loadLocationQuantity(position.intValue()));
    }

    @Override
    protected void initView() {
        actQuantityName.setText("应发数量");
        super.initView();
    }

    @Override
    public void initDataLazily() {
        if (mRefDetail == null) {
            clearAll();
            if(mRefLineAdapter != null) {
                mRefLines.clear();
                mRefLineAdapter.notifyDataSetChanged();
            }
            showMessage("请现在明细界面获取明细数据");
            return;
        }
        setupRefLineAdapter();
    }

    /**
     * 这里显示的组件明细的RefDocItem
     */
    @Override
    public void setupRefLineAdapter() {
        if (mRefLines == null) {
            mRefLines = new ArrayList<>();
        }
        mRefLines.clear();
        mRefLines.add("请选择");
        for (RefDetailEntity item : mRefDetail) {
            mRefLines.add(String.valueOf(item.refDocItem));
        }
        //初始化单据行适配器
        if (mRefLineAdapter == null) {
            mRefLineAdapter = new ArrayAdapter<>(mActivity, R.layout.item_simple_sp, mRefLines);
            spRefLine.setAdapter(mRefLineAdapter);
        } else {
            mRefLineAdapter.notifyDataSetChanged();
        }
        //默认选择第一个
        spRefLine.setSelection(1);
    }

    @Override
    public void bindCommonCollectUI() {
        clearAll();
        mSelectedRefLineNum = mRefLines.get(spRefLine.getSelectedItemPosition());
        RefDetailEntity lineData = getLineData(mSelectedRefLineNum);
        tvMaterialNum.setText(lineData.materialNum);
        etQuantity.setText("");
        //物资描述
        tvMaterialDesc.setText(lineData.materialDesc);
        //特殊库存标识
        tvSpecialInvFlag.setText(lineData.specialInvFlag);
        //工厂
        tvWork.setText(lineData.workName);
        //应收数量
        tvActQuantity.setText(lineData.actQuantity);
        //库存标识
        tvSpecialInvFlag.setText("O");
        //先将累计消耗数量置空
        tvTotalQuantity.setText("0");
        //获取库存
        mPresenter.getInventoryInfo("01", lineData.workId,
                "", lineData.workCode, "", "", getString(tvMaterialNum),
                lineData.materialId, "", "", "O", mRefData.supplierNum, "1", "");
    }

    @Override
    public void showInventory(List<InventoryEntity> list) {
        mInventoryDatas.clear();
        InventoryEntity tmp = new InventoryEntity();
        tmp.batchFlag = "请选择";

        mInventoryDatas.add(tmp);
        mInventoryDatas.addAll(list);

        if (mInventoryAdapter == null) {
            mInventoryAdapter = new WWCInventoryAdapter(mActivity, R.layout.item_simple_sp, mInventoryDatas);
            spBatchFlag.setAdapter(mInventoryAdapter);
        } else {
            mInventoryAdapter.notifyDataSetChanged();
        }


        //获取上一次的缓存的批次，如果有那么锁定它
        RefDetailEntity lineData = getLineData(mSelectedRefLineNum);
        final String cachedBatchFlag = lineData.batchFlag;
        int pos = -1;
        if (!TextUtils.isEmpty(cachedBatchFlag)) {
            for (int i = 0, size = mInventoryDatas.size(); i < size; i++) {
                ++pos;
                if (cachedBatchFlag.equals(mInventoryDatas.get(i).batchFlag)) {
                    break;
                }
            }
        }
        spBatchFlag.setEnabled(pos < 0);
        //如果没有获取到缓存的批次，那么默认选择第一条
        spBatchFlag.setSelection(pos < 0 ? 1 : pos);
    }

    @Override
    public void loadInventoryFail(String message) {
        showMessage(message);
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spBatchFlag.getAdapter();
        if (adapter != null) {
            mInventoryDatas.clear();
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 加载单条缓存
     */
    private void loadLocationQuantity(int position) {

        if (TextUtils.isEmpty(mRefData.refCodeId)) {
            showMessage("参考单据的Id为空");
            return;
        }
        RefDetailEntity lineData = getLineData(mSelectedRefLineNum);
        if (lineData == null) {
            showMessage("未获取到" + mSelectedRefLineNum + "对应的明细数据");
            return;
        }
        final String refLineId = lineData.refLineId;
        final String batchFlag = mInventoryDatas.get(position).batchFlag;

        mPresenter.getTransferInfoSingle(mRefData.refCodeId, mRefType, mBizType, refLineId, batchFlag, "",
                lineData.refDoc, UiUtil.convertToInt(lineData.refDocItem), Global.USER_ID);
    }

    /**
     * 通过批次匹配出缓存的仓位数量。
     */
    @Override
    public void onBindCache(RefDetailEntity cache, String batchFlag, String location) {
        if (cache != null) {
            tvTotalQuantity.setText(cache.totalQuantity);
        }
    }

    @Override
    public void loadCacheSuccess() {
        showMessage("获取缓存成功");
    }

    @Override
    public void loadCacheFail(String message) {
        showMessage(message);
        tvTotalQuantity.setText("0");
    }

    /**
     * 获取行明细(这里获取的是界面得到的mRefDetail)
     *
     * @param refDocItem:单据行号
     * @return
     */
    @Override
    protected RefDetailEntity getLineData(String refDocItem) {
        final int index = getIndexByLineNum(Integer.valueOf(refDocItem));
        if (index < 0) {
            return mRefDetail.get(0);
        }
        return mRefDetail.get(index);
    }

    /**
     * 通过refDocItem获取行号
     *
     * @param refDocItem:单据行号
     * @return
     */
    private int getIndexByLineNum(int refDocItem) {
        int index = -1;
        for (RefDetailEntity detailEntity : mRefDetail) {
            index++;
            if (refDocItem == detailEntity.refDocItem)
                break;

        }
        return index;
    }

    protected boolean refreshQuantity(final String quantity) {
        //将已经录入的所有的子节点的仓位数量累加
        final float totalQuantityV = UiUtil.convertToFloat(getString(tvTotalQuantity), 0.0f);
        final float actQuantityV = UiUtil.convertToFloat(getString(tvActQuantity), 0.0f);
        final float quantityV = UiUtil.convertToFloat(quantity, 0.0f);
        if (Float.compare(quantityV, 0.0f) <= 0.0f) {
            showMessage("输入数量不合理");
            return false;
        }

        /*lastFlag 委外出库行数量判断标识如果 lastFlag = 'X'  则累计录入数量不能大于 应发数量*/
        RefDetailEntity lineData = getLineData(mSelectedRefLineNum);
        if (lineData != null) {
            if (!"X".equalsIgnoreCase(lineData.lastFlag)) {
                return true;
            }
        }

        if (Float.compare(quantityV + totalQuantityV, actQuantityV) > 0.0f) {
            showMessage("输入数量有误，请出现输入");
            return false;
        }
        return true;
    }


    @Override
    public boolean checkCollectedDataBeforeSave() {

        if (mRefData == null) {
            showMessage("请先在抬头界面获取单据数据");
            return false;
        }

        if (TextUtils.isEmpty(mSelectedRefLineNum) || "请选择".equals(mSelectedRefLineNum)) {
            showMessage("请先获取物料信息");
            return false;
        }

        //检查数据是否可以保存
        if (spRefLine.getSelectedItemPosition() <= 0) {
            showMessage("请先选择单据行");
            return false;
        }

        if (!refreshQuantity(getString(etQuantity))) {
            return false;
        }

        if (spBatchFlag.getSelectedItemPosition() <= 0) {
            showMessage("请选择批次");
            return false;
        }

        return true;
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
    public void saveCollectedData() {
        if (!checkCollectedDataBeforeSave()) {
            return;
        }
        Flowable.create((FlowableOnSubscribe<ResultEntity>) emitter -> {
            RefDetailEntity lineData = getLineData(mSelectedRefLineNum);
            ResultEntity result = new ResultEntity();
            result.businessType = mBizType;
            result.refCodeId = mRefData.refCodeId;
            result.refCode = mRefData.recordNum;
            result.refLineNum = lineData.lineNum;
            result.refLineId = lineData.refLineId;
            result.voucherDate = mRefData.voucherDate;
            result.refType = mRefType;
            result.moveType = mRefData.moveType;
            result.userId = Global.USER_ID;
            result.workId = lineData.workId;
            result.materialId = lineData.materialId;
            result.location = "barcode";
            result.batchFlag = mInventoryDatas.get(spBatchFlag.getSelectedItemPosition()).batchFlag;
            result.quantity = getString(etQuantity);
            result.modifyFlag = "N";
            result.refDoc = lineData.refDoc;
            result.refDocItem = lineData.refDocItem;
            result.supplierNum = mRefData.supplierNum;
            result.specialInvFlag = getString(tvSpecialInvFlag);
            result.specialInvNum = mRefData.supplierNum;
            result.unit = TextUtils.isEmpty(lineData.recordUnit) ? lineData.materialUnit : lineData.recordUnit;
            result.unitRate = Float.compare(lineData.unitRate, 0.0f) == 0 ? 1.f : 0.f;
            emitter.onNext(result);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER).compose(TransformerHelper.io2main())
                .subscribe(result -> mPresenter.uploadCollectionDataSingle(result));
    }

    @Override
    public void saveCollectedDataSuccess() {
        showMessage("保存数据成功");
        final float quantityV = UiUtil.convertToFloat(getString(etQuantity), 0.0f);
        final float totalQuantity = UiUtil.convertToFloat(getString(tvTotalQuantity), 0.0f);
        tvTotalQuantity.setText(String.valueOf(totalQuantity + quantityV));
        etQuantity.setText("");
    }

    @Override
    public void saveCollectedDataFail(String message) {
        showMessage("保存数据失败;" + message);
    }

    @Override
    public void _onPause() {
        clearAll();
        //清除下拉
        if (mRefLineAdapter != null) {
            spRefLine.setSelection(0);
        }
    }

    private void clearAll() {
        clearCommonUI(tvMaterialNum, tvMaterialDesc, tvSpecialInvFlag, tvWork, tvActQuantity, tvTotalQuantity);

        if (mInventoryAdapter != null) {
            spBatchFlag.setSelection(0);
            mInventoryDatas.clear();
            mInventoryAdapter.notifyDataSetChanged();
        }
    }
}
