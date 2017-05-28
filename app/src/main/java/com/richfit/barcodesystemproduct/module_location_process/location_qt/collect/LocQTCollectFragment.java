package com.richfit.barcodesystemproduct.module_location_process.location_qt.collect;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxAdapterView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.LocationAdapter;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.module_location_process.location_qt.collect.imp.LocQTCollectPresenterImp;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.L;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.common_lib.widget.RichEditText;
import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.LocationInfoEntity;
import com.richfit.domain.bean.RefDetailEntity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * 这里需要根据是否是上架还是下架处理进行具体的业务分发。
 * 相同的地方在于获取物料明细(也就是确定当前操作的明细行)
 * Created by monday on 2017/5/26.
 */

public class LocQTCollectFragment extends BaseFragment<LocQTCollectPresenterImp>
        implements ILocQTCollectView {


    @BindView(R.id.sp_ref_line_num)
    Spinner spRefLine;
    @BindView(R.id.et_material_num)
    RichEditText etMaterialNum;
    @BindView(R.id.tv_material_desc)
    TextView tvMaterialDesc;
    @BindView(R.id.tv_special_inv_flag)
    TextView tvSpecialInvFlag;
    @BindView(R.id.tv_work_name)
    TextView tvWorkName;
    @BindView(R.id.tv_work)
    TextView tvWork;
    @BindView(R.id.tv_inv)
    TextView tvInv;
    @BindView(R.id.tv_act_quantity)
    TextView tvActQuantity;
    @BindView(R.id.tv_inv_type)
    TextView tvInvType;
    @BindView(R.id.et_batch_flag)
    EditText etBatchFlag;
    //下架仓位
    @BindView(R.id.sp_x_loc)
    Spinner spXLoc;
    //库存数量
    @BindView(R.id.tv_inv_quantity)
    TextView tvInvQuantity;
    //上架仓位
    @BindView(R.id.et_s_location)
    RichEditText etSLocation;
    //仓位数量
    @BindView(R.id.tv_location_quantity)
    TextView tvLocQuantity;
    @BindView(R.id.et_quantity)
    EditText etQuantity;
    @BindView(R.id.cb_single)
    CheckBox cbSingle;
    @BindView(R.id.tv_total_quantity)
    TextView tvTotalQuantity;

    /*当前匹配的行明细（行号）*/
    protected ArrayList<String> mRefLines;
    /*单据行适配器*/
    ArrayAdapter<String> mRefLineAdapter;
    /*库存信息*/
    private List<InventoryEntity> mInventoryDatas;
    private LocationAdapter mXLocAdapter;
    /*当前选中的单据行*/
    protected String mSelectedRefLineNum;
    /*校验仓位是否存在，如果false表示校验该仓位不存在或者没有校验该仓位，不允许保存数据*/
    protected boolean isLocationChecked = false;
    /*批次一致性检查*/
    protected boolean isBatchValidate = true;

    @Override
    protected int getContentId() {
        return R.layout.fragment_locqt_collect;
    }

    @Override
    public void initInjector() {

    }

    @Override
    protected void initVariable(@Nullable Bundle savedInstanceState) {
        mRefLines = new ArrayList<>();
        mInventoryDatas = new ArrayList<>();
    }

    /**
     * 绑定公共事件，子类自己根据是否上架，是否需要检查上架是否存在
     * 重写上架仓位监听
     */
    @Override
    public void initEvent() {
        //扫描后者手动输入物资条码
        etMaterialNum.setOnRichEditTouchListener((view, materialNum) -> {
            hideKeyboard(etMaterialNum);
            //手动输入没有批次
            loadMaterialInfo(materialNum, getString(etBatchFlag));
        });

        //选择单据行
        RxAdapterView
                .itemSelections(spRefLine)
                .filter(position -> position > 0)
                .subscribe(position -> bindCommonCollectUI());

        //单品(注意单品仅仅控制实收数量，累计数量是由行信息里面控制)
        cbSingle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etQuantity.setText(isChecked ? "1" : "");
            etQuantity.setEnabled(!isChecked);
        });

        //监听上架仓位时时变化
        RxTextView.textChanges(etSLocation)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(a -> {
                    tvLocQuantity.setText("");
                    tvTotalQuantity.setText("");
                });

        //点击上架仓位获取缓存
        etSLocation.setOnRichEditTouchListener((view, location) -> getTransferSingle(getString(etBatchFlag), location));

        //下架仓位,选择下架仓位刷新库存数量，并且获取缓存
        RxAdapterView
                .itemSelections(spXLoc)
                .filter(position -> (mInventoryDatas != null && mInventoryDatas.size() > 0 &&
                        position.intValue() <= mInventoryDatas.size() - 1))
                .subscribe(position -> {
                    RefDetailEntity data = getLineData(mSelectedRefLineNum);
                    InventoryEntity invData = mInventoryDatas.get(position);
                    String invQuantity = calQuantityByUnitRate(invData.invQuantity, data.recordUnit, data.unitRate);
                    invData.invQuantity = invQuantity;
                    tvInvQuantity.setText(invData.invQuantity);
                    getTransferSingle(position);
                });
    }


    /**
     * 检查抬头界面的必要的字段是否已经赋值
     */
    @Override
    public void initDataLazily() {
        etMaterialNum.setEnabled(false);
        if (mRefData == null) {
            showMessage("请先在抬头界面获取单据数据");
            return;
        }
        if (TextUtils.isEmpty(mRefData.bizType)) {
            showMessage("未获取到业务类型");
            return;
        }

        if (TextUtils.isEmpty(mRefData.moveType)) {
            showMessage("未获取到移动类型");
            return;
        }

        if (TextUtils.isEmpty(mRefData.refType)) {
            showMessage("请先在抬头界面获取单据数据");
            return;
        }

        String transferKey = (String) SPrefUtil.getData(mBizType + mRefType, "0");
        if ("1".equals(transferKey)) {
            showMessage("本次采集已经过账,请先到数据明细界面进行数据上传操作");
            return;
        }
        etMaterialNum.setEnabled(true);
        etBatchFlag.setEnabled(mIsOpenBatchManager);
    }

    /**
     * 输入或者扫描物料条码后系统自动去匹配单据行明细，并且初始化默认选择的明细数据.
     * 由于在初始化单据行下拉列表的同时也需要出发页面刷星，所以页面刷新统一延迟到选择单据
     * 行列表的item之后。
     * 具体流程loadMaterialInfo->setupRefLineAdapter->bindCommonCollectUI
     */
    @Override
    public void loadMaterialInfo(@NonNull String materialNum, @NonNull String batchFlag) {
        if (!etMaterialNum.isEnabled()) {
            return;
        }
        if (TextUtils.isEmpty(materialNum)) {
            showMessage("请输入物资条码");
            return;
        }
        clearAllUI();
        //刷新界面(在单据行明细查询是否有该物料条码，如果有那么刷新界面)
        matchMaterialInfo(materialNum, batchFlag)
                .compose(TransformerHelper.io2main())
                .subscribe(details -> setupRefLineAdapter(details), e -> showMessage(e.getMessage()));
    }


    /**
     * 设置单据行
     *
     * @param refLines
     */
    @Override
    public void setupRefLineAdapter(ArrayList<String> refLines) {
        mRefLines.clear();
        mRefLines.add(getString(R.string.default_choose_item));
        if (refLines != null)
            mRefLines.addAll(refLines);

        //如果未查询到提示用户
        if (mRefLines.size() == 1) {
            showMessage("该单据中未查询到该物料,请检查物资编码或者批次是否正确");
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

    /**
     * 绑定UI。
     */
    @Override
    public void bindCommonCollectUI() {
        mSelectedRefLineNum = mRefLines.get(spRefLine.getSelectedItemPosition());
        RefDetailEntity lineData = getLineData(mSelectedRefLineNum);
        etQuantity.setText("");
        //物资描述
        tvMaterialDesc.setText(lineData.materialDesc);
        //特殊库存标识
        tvSpecialInvFlag.setText(lineData.specialInvFlag);
        //工厂
        tvWork.setText(lineData.workName);
        //库存地点
        tvInv.setText(lineData.invName);
        //库存类型
        tvInvType.setText(lineData.invType);
        //允许进行上下架的数量(应收数量)
        tvActQuantity.setText(lineData.actQuantity);
        if (TextUtils.isEmpty(getString(etBatchFlag))) {
            etBatchFlag.setText(mIsOpenBatchManager ? lineData.batchFlag : "");
        }
        //这里需要根据具体是上架还是下架进行接下来的业务分发，如果是上架那么不做处理了
        if (true) {
            //如果是上架
            tvLocQuantity.setText("");
            tvTotalQuantity.setText("");
        } else {
            //如果是下架
            loadInventory();
        }
    }

    /**
     * 下架处理时加载库存
     */
    private void loadInventory() {
        tvInvQuantity.setText("");
        tvLocQuantity.setText("");
        tvTotalQuantity.setText("");
        if (mXLocAdapter != null) {
            mInventoryDatas.clear();
            mXLocAdapter.notifyDataSetChanged();
        }
        if (mIsOpenBatchManager && TextUtils.isEmpty(getString(etBatchFlag))) {
            showMessage("请输入批次");
            return;
        }
        final RefDetailEntity lineData = getLineData(mSelectedRefLineNum);

        //需要确定库存类型
        mPresenter.getInventoryInfo("02", lineData.workId,
                lineData.invId, lineData.workCode, lineData.invCode, "", getString(etMaterialNum),
                lineData.materialId, "", getString(etBatchFlag), "", "", lineData.invType, "");
    }

    /**
     * 加载库存成功
     *
     * @param list
     */
    @Override
    public void showInventory(List<InventoryEntity> list) {
        mInventoryDatas.clear();
        InventoryEntity tmp = new InventoryEntity();
        tmp.locationCombine = "请选择";
        mInventoryDatas.add(tmp);
        mInventoryDatas.addAll(list);
        if (mXLocAdapter == null) {
            mXLocAdapter = new LocationAdapter(mActivity, R.layout.item_simple_sp, mInventoryDatas);
            spXLoc.setAdapter(mXLocAdapter);
        } else {
            mXLocAdapter.notifyDataSetChanged();
        }
        spXLoc.setSelection(0);
    }

    /**
     * 加载库存失败
     *
     * @param message
     */
    @Override
    public void loadInventoryFail(String message) {
        showMessage(message);
    }


    /**
     * 上架处理时获取单条缓存
     **/
    private void getTransferSingle(String batchFlag, String location) {

        if (spRefLine.getSelectedItemPosition() <= 0) {
            showMessage("请先选择单据行");
            return;
        }
        final RefDetailEntity lineData = getLineData(mSelectedRefLineNum);

        //检查库存地点
        if (TextUtils.isEmpty(lineData.invId)) {
            showMessage("未获取到库存点");
            return;
        }

        //批次处理。打开了批次管理而且必须输入，那么检查是否输入了批次
        if (mIsOpenBatchManager && etBatchFlag.isEnabled())
            if (TextUtils.isEmpty(batchFlag)) {
                showMessage("请先输入批次");
                return;
            }
        if (TextUtils.isEmpty(location)) {
            showMessage("请先输入上架仓位");
            return;
        }
        isBatchValidate = false;
        //这里不考虑是否上架
        mPresenter.checkLocation("04", lineData.workId, lineData.invId, batchFlag, location);
    }

    /**
     * 下架处理时获取单条缓存
     */
    private void getTransferSingle(int position) {
        final String invQuantity = mInventoryDatas.get(position).invQuantity;
        //这里给出的是location+specialInvFlag+specialInvNum的组合字段
        final String location = mInventoryDatas.get(position).locationCombine;
        final String batchFlag = getString(etBatchFlag);

        if (position <= 0) {
            resetLocation();
            return;
        }
        if (spRefLine.getSelectedItemPosition() <= 0) {
            showMessage("请先选择单据行");
            return;
        }

        final RefDetailEntity lineData = getLineData(mSelectedRefLineNum);

        //检查库存地点
        if (TextUtils.isEmpty(lineData.invId)) {
            showMessage("未获取到库存点");
            return;
        }

        if (mIsOpenBatchManager && TextUtils.isEmpty(batchFlag)) {
            showMessage("请先输入批次");
            return;
        }

        if (TextUtils.isEmpty(location)) {
            showMessage("请先输入下架仓位");
            resetLocation();
            return;
        }

        tvInvQuantity.setText(invQuantity);

        final String refCodeId = mRefData.refCodeId;
        final String refType = mRefData.refType;
        final String bizType = mRefData.bizType;
        final String refLineId = lineData.refLineId;
        isBatchValidate = true;
        mPresenter.getTransferInfoSingle(refCodeId, refType, bizType, refLineId,
                getString(etMaterialNum), batchFlag, location, lineData.refDoc,
                UiUtil.convertToInt(lineData.refDocItem),
                Global.USER_ID);
    }


    private void resetLocation() {
        spXLoc.setSelection(0, true);
        tvInvQuantity.setText("");
        tvLocQuantity.setText("");
        tvTotalQuantity.setText("");
    }


    @Override
    public void onBindCache(RefDetailEntity cache, String batchFlag, String location) {
        if (cache != null) {
            tvTotalQuantity.setText(cache.totalQuantity);
            //匹配缓存
            List<LocationInfoEntity> locationInfos = cache.locationList;
            if (locationInfos == null || locationInfos.size() == 0) {
                //没有缓存
                tvLocQuantity.setText("0");
                return;
            }
            tvLocQuantity.setText("0");
            /**
             * 这里匹配缓存是通过批次+仓位匹配的，但是批次即便是在打开了批次管理的情况下
             * 也可能没有批次。
             */
            for (LocationInfoEntity cachedItem : locationInfos) {
                //缓存和输入的都为空或者都不为空而且相等,那么系统默认批次匹配
                boolean isMatch;

                isBatchValidate = mIsOpenBatchManager && ((TextUtils.isEmpty(cachedItem.batchFlag) && TextUtils.isEmpty(batchFlag)) ||
                        (!TextUtils.isEmpty(cachedItem.batchFlag) && !TextUtils.isEmpty(batchFlag) && batchFlag.equalsIgnoreCase(cachedItem.batchFlag)));

                isMatch = mIsOpenBatchManager ? (TextUtils.isEmpty(cachedItem.batchFlag) && TextUtils.isEmpty(batchFlag) &&
                        location.equalsIgnoreCase(cachedItem.location)) || (
                        !TextUtils.isEmpty(cachedItem.batchFlag) && !TextUtils.isEmpty(batchFlag) &&
                                location.equalsIgnoreCase(cachedItem.location))
                        : location.equalsIgnoreCase(cachedItem.location);

                L.e("isBatchValidate = " + isBatchValidate + "; isMatch = " + isMatch);

                //注意它没有匹配次成功可能是批次页可能是仓位。
                if (isMatch) {
                    tvLocQuantity.setText(cachedItem.quantity);
                    break;
                }
            }

            if (!isBatchValidate) {
                showMessage("批次输入有误，请检查批次是否与缓存批次输入一致");
            }
        }
    }


    @Override
    public void loadCacheSuccess() {
        if (cbSingle.isChecked() && checkCollectedDataBeforeSave()) {
            saveCollectedData();
        }
    }

    @Override
    public void loadCacheFail(String message) {
        showMessage(message);
        isBatchValidate = true;
        //如果没有获取到任何缓存
        tvLocQuantity.setText("0");
        tvTotalQuantity.setText("0");
        if (cbSingle.isChecked() && checkCollectedDataBeforeSave()) {
            saveCollectedData();
        }
    }

    @Override
    public void checkLocationFail(String message) {
        showMessage(message);
        isLocationChecked = false;
    }

    @Override
    public void checkLocationSuccess(String batchFlag, String location) {
        isLocationChecked = true;
        final RefDetailEntity lineData = getLineData(mSelectedRefLineNum);
        final String refCodeId = mRefData.refCodeId;
        final String refLineId = lineData.refLineId;
        final String refType = mRefData.refType;
        final String bizType = mRefData.bizType;
        mPresenter.getTransferInfoSingle(refCodeId, refType, bizType, refLineId,
                getString(etMaterialNum), batchFlag, location, lineData.refDoc, UiUtil.convertToInt(lineData.refDocItem), Global.USER_ID);
    }

    /**
     * 不论扫描的是否是同一个物料，都清除控件的信息。
     */
    private void clearAllUI() {
        clearCommonUI(tvMaterialDesc, tvWork, tvActQuantity, tvInv, tvInvType, tvLocQuantity,
                etQuantity, tvLocQuantity, tvSpecialInvFlag, tvInvQuantity, tvTotalQuantity, cbSingle);

        //单据行
        if (mRefLineAdapter != null) {
            mRefLines.clear();
            mRefLineAdapter.notifyDataSetChanged();
            spRefLine.setBackgroundColor(0);
        }

        //下架仓位
        if (mXLocAdapter != null) {
            mInventoryDatas.clear();
            mXLocAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void _onPause() {
        super._onPause();
        clearAllUI();
        clearCommonUI(etMaterialNum, etBatchFlag);
    }
}
