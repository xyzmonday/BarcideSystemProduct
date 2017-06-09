package com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_collect;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog.Builder;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxAdapterView;
import com.jakewharton.rxbinding2.widget.RxAutoCompleteTextView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.InvAdapter;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.L;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.common_lib.widget.RichAutoEditText;
import com.richfit.common_lib.widget.RichEditText;
import com.richfit.domain.bean.InvEntity;
import com.richfit.domain.bean.LocationInfoEntity;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ResultEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * 物资入库数据采集界面。注意批次和上架仓位的处理。
 * 对于批次，有两个检查：
 * 第一个是是否需要输入批次，该情况有etBatchFlag控件的enable属性控制,如果enable=false，不需要检查是否输入;
 * 第二个是检查批次是否一致的问题
 * 又分为一下两种情况:
 * 1. 打开了批次管理，对于非必检物资做一致性检查
 * 2. 打开了批次管理，对于enable=false的做一致性检查
 * <p>
 * 2017年06月07日:将上架仓位修改成可下拉的，数据源从仓位主数据里面获取
 *
 * @param <P>
 */

public abstract class BaseASCollectFragment<P extends IASCollectPresenter> extends BaseFragment<P>
        implements IASCollectView {

    @BindView(R.id.sp_ref_line_num)
    protected Spinner spRefLine;
    @BindView(R.id.et_material_num)
    protected RichEditText etMaterialNum;
    @BindView(R.id.tv_material_desc)
    protected TextView tvMaterialDesc;
    @BindView(R.id.tv_special_inv_flag)
    protected TextView tvSpecialInvFlag;
    @BindView(R.id.tv_batch_flag_name)
    protected TextView tvBatchFlagName;
    @BindView(R.id.tv_work)
    protected TextView tvWork;
    @BindView(R.id.tv_work_name)
    protected TextView tvWorkName;
    @BindView(R.id.act_quantity_name)
    protected TextView tvActQuantityName;
    @BindView(R.id.tv_act_quantity)
    protected TextView tvActQuantity;
    @BindView(R.id.ll_batch_flag)
    protected LinearLayout llBatchFlag;
    @BindView(R.id.et_batch_flag)
    protected EditText etBatchFlag;
    @BindView(R.id.sp_inv)
    protected Spinner spInv;
    @BindView(R.id.tv_location_name)
    protected TextView tvLocationName;
    @BindView(R.id.et_location)
    protected RichAutoEditText etLocation;
    @BindView(R.id.tv_location_quantity)
    protected TextView tvLocQuantity;
    @BindView(R.id.quantity_name)
    protected TextView tvQuantityName;
    @BindView(R.id.et_quantity)
    protected EditText etQuantity;
    @BindView(R.id.cb_single)
    protected CheckBox cbSingle;
    @BindView(R.id.tv_total_quantity)
    protected TextView tvTotalQuantity;
    @BindView(R.id.ll_location)
    protected LinearLayout llLocation;
    @BindView(R.id.ll_location_quantity)
    protected LinearLayout llLocationQuantity;
    @BindView(R.id.ll_inslot_quantity)
    protected LinearLayout llInsLostQuantity;
    @BindView(R.id.tv_insLost_quantity)
    protected TextView tvInsLostQuantity;


    /*当前匹配的行明细（行号）*/
    protected ArrayList<String> mRefLines;
    /*单据行适配器*/
    ArrayAdapter<String> mRefLineAdapter;
    /*库存地点列表*/
    protected ArrayList<InvEntity> mInvDatas;
    /*库存地点适配器*/
    private InvAdapter mInvAdapter;
    /*当前选中的单据行*/
    protected String mSelectedRefLineNum;
    /*校验仓位是否存在，如果false表示校验该仓位不存在或者没有校验该仓位，不允许保存数据*/
    protected boolean isLocationChecked = false;
    /*是否是质检物资*/
    protected boolean isQmFlag = false;
    /*是否不上架。false表示上架处理，那么用户必须输入上架仓位，true表示不做上架处理，保存数据默认传barcode*/
    protected boolean isNLocation = false;
    /*批次一致性检查*/
    protected boolean isBatchValidate = true;
    /*上架仓位列表适配器*/
    ArrayAdapter<String> mLocationAdapter;
    List<String> mLocationList;

    @Override
    protected int getContentId() {
        return R.layout.fragment_base_asy_collect;
    }

    /**
     * 在没有勾选单品的情况下：
     * 1）不论扫当前和是其他的物料统统先清除控件的信息;
     * 如果勾选了单品，那么：
     * 1）如果扫的是当前的物料，那么仓位数量和累计数量+1,直接保存;
     * 2）如果扫的不是当前的物料，那么清空所有的控件，并且重新走没有单品的情况下的逻辑；
     * 3）如果扫描的是仓位，那么不处理。
     *
     * @param type
     * @param list
     */
    @Override
    public void handleBarCodeScanResult(String type, String[] list) {
        if (list != null && list.length > 12) {
            if (!etMaterialNum.isEnabled()) {
                showMessage("请先在抬头界面获取相关数据");
                return;
            }
            final String materialNum = list[Global.MATERIAL_POS];
            final String batchFlag = list[Global.BATCHFALG_POS];
            if (cbSingle.isChecked() && materialNum.equalsIgnoreCase(getString(etMaterialNum))) {
                //如果已经选中单品，那么说明已经扫描过一次。必须保证每一次的物料都一样
                getTransferSingle(batchFlag, getString(etLocation));
            } else {
                //在非单品模式下，扫描不同的物料。注意这里必须用新的物料和批次更新UI,因为clearAllUI方法没有
                //清除显示在屏幕上的物料和批次信息
                etMaterialNum.setText(materialNum);
                etBatchFlag.setText(batchFlag);
                loadMaterialInfo(materialNum, batchFlag);
            }
            //处理仓位
        } else if (list != null && list.length == 1 & !cbSingle.isChecked()) {
            final String location = list[0];
            etLocation.setText(location);
            getTransferSingle(getString(etBatchFlag), location);
        }
    }

    @Override
    protected void initVariable(@Nullable Bundle savedInstanceState) {
        mRefLines = new ArrayList<>();
        mInvDatas = new ArrayList<>();
        mLocationList = new ArrayList<>();
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
        RxTextView.textChanges(etLocation)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(a -> {
                    tvLocQuantity.setText("");
                    tvTotalQuantity.setText("");
                });

        //对于质检物资(不上架)通过库存地点来获取缓存，如果需要上架选择库存地点获取上架仓位列表
        RxAdapterView.itemSelections(spInv)
                .filter(pos -> pos > 0)
                .subscribe(pos -> {
                    if (isNLocation) {
                        //如果不上架
                        getTransferSingle(getString(etBatchFlag), getString(etLocation));
                    } else {
                        loadLocationList(getString(etLocation), false);
                    }
                });

        //监听输入的关键字
        RxTextView.textChanges(etLocation)
                .debounce(100, TimeUnit.MILLISECONDS)
                .filter(str -> !TextUtils.isEmpty(str) && mLocationList != null &&
                        mLocationList.size() > 0 && !filterKeyWord(str))
                .subscribe(a -> loadLocationList(getString(etLocation), true));

        //选中上架仓位列表的item，关闭输入法,并且直接匹配出仓位数量
        RxAutoCompleteTextView.itemClickEvents(etLocation)
                .filter(a -> !isNLocation)
                .subscribe(a -> {
                    hideKeyboard(etLocation);
                    getTransferSingle(getString(etBatchFlag), getString(etLocation));
                });

        //点击自动提示控件，显示默认列表
        RxView.clicks(etLocation)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .filter(a -> mLocationList != null && mLocationList.size() > 0)
                .subscribe(a -> showAutoCompleteConfig(etLocation));
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
        if (TextUtils.isEmpty(mRefData.voucherDate)) {
            showMessage("请先在抬头界面选择过账日期");
            return;
        }

        String transferKey = (String) SPrefUtil.getData(mBizType + mRefType, "0");
        if ("1".equals(transferKey)) {
            showMessage("本次采集已经过账,请先到数据明细界面进行数据上传操作");
            return;
        }
        etMaterialNum.setEnabled(true);
        etLocation.setEnabled(!isNLocation);
        //重新复位批次管理标识
        isOpenBatchManager = true;
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
        //再次复位批次管理标识
        isOpenBatchManager = true;
        mangageBatchFlagStatus(etBatchFlag, lineData.batchManagerStatus);
        etQuantity.setText("");
        //物资描述
        tvMaterialDesc.setText(lineData.materialDesc);
        //特殊库存标识
        tvSpecialInvFlag.setText(lineData.specialInvFlag);
        //工厂
        tvWork.setText(lineData.workName);
        //应收数量
        tvActQuantity.setText(lineData.actQuantity);
        //如果打开了批次管理，那么以当前输入的为准，如果没有那么获取单据中的批次
        if (isOpenBatchManager && TextUtils.isEmpty(getString(etBatchFlag))) {
            etBatchFlag.setText(lineData.batchFlag);
        }
        //先将库存地点选择器打开，获取缓存后在判断是否需要锁定
        spInv.setEnabled(true);
        tvLocQuantity.setText("");
        tvTotalQuantity.setText("");
        if (!cbSingle.isChecked())
            mPresenter.getInvsByWorkId(lineData.workId, getOrgFlag());
    }

    @Override
    public void loadInvFail(String message) {
        showMessage(message);
    }

    @Override
    public void showInvs(ArrayList<InvEntity> list) {
        //初始化库存地点
        mInvDatas.clear();
        mInvDatas.addAll(list);
        if (mInvAdapter == null) {
            mInvAdapter = new InvAdapter(mActivity, R.layout.item_simple_sp, mInvDatas);
            spInv.setAdapter(mInvAdapter);
        } else {
            mInvAdapter.notifyDataSetChanged();
        }
        //这里如果不上架，默认选择第一个目的是触发获取缓存
        spInv.setSelection(isNLocation ? 1 : 0);
    }

    @Override
    public void loadLocationList(String keyWord, boolean isDropDown) {
        RefDetailEntity lineData = getLineData(mSelectedRefLineNum);
        InvEntity invEntity = mInvDatas.get(spInv.getSelectedItemPosition());
        mPresenter.getLocationList(lineData.workId, lineData.workCode, invEntity.invId,
                invEntity.invCode, keyWord, 100, getInteger(R.integer.orgNorm), isDropDown);
    }

    /**
     * 如果用户输入的关键字在mLocationList存在，那么不在进行数据查询.
     *
     * @param keyWord
     * @return
     */
    private boolean filterKeyWord(CharSequence keyWord) {
        Pattern pattern = Pattern.compile("^" + keyWord.toString().toUpperCase());
        for (String item : mLocationList) {
            Matcher matcher = pattern.matcher(item);
            while (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void getLocationListFail(String message) {
        showMessage(message);
    }

    @Override
    public void getLocationListSuccess(List<String> list, boolean isDropDown) {
        mLocationList.clear();
        mLocationList.addAll(list);
        if (mLocationAdapter == null) {
            mLocationAdapter = new ArrayAdapter<>(mActivity,
                    android.R.layout.simple_dropdown_item_1line, mLocationList);
            etLocation.setAdapter(mLocationAdapter);
            setAutoCompleteConfig(etLocation);
        } else {
            mLocationAdapter.notifyDataSetChanged();
        }
        if (isDropDown) {
            showAutoCompleteConfig(etLocation);
        }
    }

    /**
     * 获取单条缓存。
     * 在获取仓位数量的缓存之前，必须检查仓位是否合理。注意不同的公司检查的策略不一样。
     */
    protected void getTransferSingle(String batchFlag, String location) {

        if (spRefLine.getSelectedItemPosition() <= 0) {
            showMessage("请先选择单据行");
            return;
        }
        //检验是否选择了库存地点
        if (spInv.getSelectedItemPosition() <= 0) {
            showMessage("请先选择库存地点");
            return;
        }

        //批次处理。打开了批次管理而且必须输入，那么检查是否输入了批次
        if (isOpenBatchManager && etBatchFlag.isEnabled()) {
            if (TextUtils.isEmpty(batchFlag)) {
                showMessage("请先输入批次");
                return;
            }
        }
        if (TextUtils.isEmpty(location) && !isNLocation) {
            showMessage("请先输入上架仓位");
            return;
        }

        isBatchValidate = false;

        if (!isNLocation) {
            //如果上架，那么检查仓位是否存在
            final RefDetailEntity lineData = getLineData(mSelectedRefLineNum);
            final String invId = mInvDatas.get(spInv.getSelectedItemPosition()).invId;
            mPresenter.checkLocation("04", lineData.workId, invId, batchFlag, location);
        } else {
            //如果不上架，那么直接默认仓位检查通过
            checkLocationSuccess(batchFlag, location);
        }
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
                batchFlag, location, lineData.refDoc, UiUtil.convertToInt(lineData.refDocItem), Global.USER_ID);
    }

    @Override
    public void checkLocationFail(String message) {
        showMessage(message);
        isLocationChecked = false;
    }


    /**
     * 不论扫描的是否是同一个物料，都清除控件的信息。
     */
    private void clearAllUI() {
        clearCommonUI(tvMaterialDesc, tvWork, tvActQuantity, etLocation, tvLocQuantity, etQuantity, tvLocQuantity,
                tvTotalQuantity, cbSingle, tvInsLostQuantity);

        //单据行
        if (mRefLineAdapter != null) {
            mRefLines.clear();
            mRefLineAdapter.notifyDataSetChanged();
            spRefLine.setBackgroundColor(0);
        }
        //库存地点
        if (mInvAdapter != null) {
            mInvDatas.clear();
            mInvAdapter.notifyDataSetChanged();
        }

        //上架仓位
        if (mLocationAdapter != null) {
            mLocationList.clear();
            mLocationAdapter.notifyDataSetChanged();
        }

        spInv.setEnabled(true);
    }

    /**
     * 绑定缓存。主要显示缓存的仓位数量，以及扩展字段。
     * 注意在bindCommonCollectUI方法中，系统需要判断是否上架，主要的逻辑是，
     * 用户输入物料和批次后，得到该行的物料信息，该物料信息包含了该物料是否是质检
     * 物资，如果是非质检物质那么isNLocation=false,表示必须上架；如果是质检物质
     * 那么需要通过该父节点下的第一个子节点是否录入了仓位。如果第一个子节点有仓位
     * 那么isNLocation = false,否者isNLocation=true。
     *
     * @param cache：缓存数据
     * @param batchFlag：批次
     * @param location：仓位
     */
    @Override
    public void onBindCache(RefDetailEntity cache, String batchFlag, String location) {
        if (!isNLocation) {
            if (cache != null) {
                tvTotalQuantity.setText(cache.totalQuantity);
                //锁定库存地点
                lockInv(cache.invId);
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

                    isBatchValidate = isOpenBatchManager && ((TextUtils.isEmpty(cachedItem.batchFlag) && TextUtils.isEmpty(batchFlag)) ||
                            (!TextUtils.isEmpty(cachedItem.batchFlag) && !TextUtils.isEmpty(batchFlag) && batchFlag.equalsIgnoreCase(cachedItem.batchFlag)));

                    isMatch = isOpenBatchManager ? (TextUtils.isEmpty(cachedItem.batchFlag) && TextUtils.isEmpty(batchFlag) &&
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
        } else {
            //对于不上架的物资，显示累计数量和锁定库存地点
            if (cache != null) {
                tvTotalQuantity.setText(cache.totalQuantity);
                lockInv(cache.invId);
            }
        }
    }

    /**
     * 锁住库存地点
     *
     * @param cachedInvId:缓存的库存地点
     */
    private void lockInv(String cachedInvId) {
        //锁定库存地点
        if (!TextUtils.isEmpty(cachedInvId)) {
            int pos = -1;
            for (InvEntity data : mInvDatas) {
                pos++;
                if (cachedInvId.equals(data.invId))
                    break;
            }
            spInv.setEnabled(false);
            spInv.setSelection(pos);
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
        spInv.setEnabled(true);
        isBatchValidate = true;
        if (!isNLocation)
            tvLocQuantity.setText("0");
        tvTotalQuantity.setText("0");
        if (cbSingle.isChecked() && checkCollectedDataBeforeSave()) {
            saveCollectedData();
        }
    }

    /**
     * 处理输入实收数量和累计数量
     * 父节点记录了前一次累计数量，所以这里仅仅将当前的入库数量与前一次的累计数量相加即可。
     */
    protected boolean refreshQuantity(final String quantity) {
        //将已经录入的所有的子节点的仓位数量累加
        final float totalQuantityV = UiUtil.convertToFloat(getString(tvTotalQuantity), 0.0f);
        final float actQuantityV = UiUtil.convertToFloat(getString(tvActQuantity), 0.0f);
        final float quantityV = UiUtil.convertToFloat(quantity, 0.0f);
        if (Float.compare(quantityV, 0.0f) <= 0.0f) {
            showMessage("输入数量不合理");
            return false;
        }
        if (Float.compare(quantityV + totalQuantityV, actQuantityV) > 0.0f) {
            showMessage("输入数量有误，请出现输入");
            if (!cbSingle.isChecked())
                etQuantity.setText("");
            return false;
        }
        return true;
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
        //第一步检查是否需要输入批次。打开了批次管理，并且批次可以输入的情况下
        if (isOpenBatchManager && etBatchFlag.isEnabled()) {
            if (TextUtils.isEmpty(getString(etBatchFlag))) {
                showMessage("请先输入批次");
                return false;
            }
        }

        //批次，对于批次的检查比较严格，分为以下几种情况(注意到数据保存这里，仅仅是需要检查是否修改了已经匹配上缓存的批次)
        //1. 打开了批次管理，对于非必检物资必须做批次一致性检查;
        //2. 打开了批次管理，但是批次只能是从单据或者条码中带出来的，那么不需要检查批次是否一致

        if (isOpenBatchManager && !isQmFlag && !isBatchValidate) {
            showMessage("批次输入有误，请检查本次输入批次是否与上一次批次输入一致");
            return false;
        }
        if (isOpenBatchManager && !etBatchFlag.isEnabled() && !isBatchValidate) {
            showMessage("批次输入有误，请检查本次输入批次是否与上一次批次输入一致");
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

        return true;
    }

    @Override
    public void showOperationMenuOnCollection(final String companyCode) {
        Builder builder = new Builder(mActivity);
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
            result.unit = TextUtils.isEmpty(lineData.recordUnit) ? lineData.materialUnit : lineData.recordUnit;
            result.unitRate = Float.compare(lineData.unitRate, 0.0f) == 0 ? 1.f : lineData.unitRate;
            result.invId = mInvDatas.get(spInv.getSelectedItemPosition()).invId;
            result.materialId = lineData.materialId;
            result.location = isNLocation ? "barcode" : getString(etLocation);
            result.batchFlag = getString(etBatchFlag);
            result.quantity = getString(etQuantity);
            result.modifyFlag = "N";
            result.refDoc = lineData.refDoc;
            result.refDocItem = lineData.refDocItem;
            result.supplierNum = mRefData.supplierNum;
            emitter.onNext(result);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER).compose(TransformerHelper.io2main())
                .subscribe(result -> mPresenter.uploadCollectionDataSingle(result));
    }

    @Override
    public void saveCollectedDataSuccess() {
        showMessage("保存数据成功");
        final float quantityV = UiUtil.convertToFloat(getString(etQuantity), 0.0f);
        final float locQuantityV = UiUtil.convertToFloat(getString(tvLocQuantity), 0.0f);
        final float totalQuantity = UiUtil.convertToFloat(getString(tvTotalQuantity), 0.0f);
        tvTotalQuantity.setText(String.valueOf(totalQuantity + quantityV));
        if (!isNLocation) {
            tvLocQuantity.setText(String.valueOf(quantityV + locQuantityV));
        }
        if (!cbSingle.isChecked()) {
            etQuantity.setText("");
        }
    }

    @Override
    public void saveCollectedDataFail(String message) {
        showMessage("保存数据失败;" + message);
    }

    @Override
    public void _onPause() {
        super._onPause();
        clearAllUI();
        clearCommonUI(etMaterialNum, etBatchFlag);
    }

    @Override
    public void retry(String retryAction) {
        switch (retryAction) {
            //获取单条缓存失败
            case Global.RETRY_LOAD_SINGLE_CACHE_ACTION:
                getTransferSingle(getString(etBatchFlag), getString(etLocation));
                break;
        }
        super.retry(retryAction);
    }


    protected abstract int getOrgFlag();
}
