package com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_edit;

import android.os.Bundle;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxAdapterView;
import com.jakewharton.rxbinding2.widget.RxAutoCompleteTextView;
import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.LocationAdapter;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.base.base_edit.BaseEditFragment;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.utils.CommonUtil;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.LocationInfoEntity;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.ResultEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;

/**
 * Created by monday on 2016/11/22.
 */

public abstract class BaseMSNEditFragment<P extends IMSNEditPresenter> extends BaseEditFragment<P>
        implements IMSNEditView {

    @BindView(R.id.tv_material_num)
    protected TextView tvMaterialNum;
    @BindView(R.id.tv_material_desc)
    TextView tvMaterialDesc;
    @BindView(R.id.tv_material_group)
    TextView tvMaterialGroup;
    @BindView(R.id.tv_send_inv)
    protected TextView tvSendInv;
    @BindView(R.id.et_quantity)
    protected EditText etQuantity;
    @BindView(R.id.sp_send_location)
    protected Spinner spSendLoc;
    @BindView(R.id.tv_send_batch_flag)
    protected TextView tvSendBatchFlag;
    @BindView(R.id.tv_inv_quantity)
    TextView tvInvQuantity;
    @BindView(R.id.tv_location_quantity)
    protected TextView tvLocQuantity;
    @BindView(R.id.auto_rec_location)
    protected AppCompatAutoCompleteTextView autoRecLoc;
    @BindView(R.id.tv_rec_batch_flag)
    protected TextView tvRecBatchFlag;

    protected String mLocationId;
    String mQuantity;
    /*修改前的发出仓位*/
    protected String mSendLocation;
    /*修改前的其他子节点的发出仓位列表*/
    ArrayList<String> mSendLocations;
    ArrayList<String> mRecLocations;
    /*库存列表*/
    protected List<InventoryEntity> mInventoryDatas;
    private LocationAdapter mSendLocAdapter;
    /*缓存的历史仓位数量*/
    protected List<RefDetailEntity> mHistoryDetailList;
    /*缓存的仓位级别的额外字段*/
    Map<String, Object> mExtraLocationMap;
    /*缓存的行级别的额外字段*/
    Map<String, Object> mExtraLineMap;
    protected String mSpecialInvFlag;
    protected String mSpecialInvNum;
    protected boolean isWareHouseSame;
    /*接收仓位*/
    protected String mDeviceId;

    @Override
    protected int getContentId() {
        return R.layout.fragment_base_msn_edit;
    }

    @Override
    public void initVariable(Bundle savedInstanceState) {
        mInventoryDatas = new ArrayList<>();
    }

    @Override
    protected void initView() {
         /*生成额外控件*/
        createExtraUI(mSubFunEntity.collectionConfigs, BaseFragment.EXTRA_VERTICAL_ORIENTATION_TYPE);
        createExtraUI(mSubFunEntity.locationConfigs, BaseFragment.EXTRA_VERTICAL_ORIENTATION_TYPE);
    }

    @Override
    public void initEvent() {
        //选择下架仓位，刷新库存并且请求缓存
        RxAdapterView
                .itemSelections(spSendLoc)
                .filter(position -> position >= 0 && isValidatedSendLocation())
                .subscribe(position -> {
                    //库存数量
                    tvInvQuantity.setText(mInventoryDatas.get(position).invQuantity);
                    //获取缓存
                    loadLocationQuantity(position);
                });
        //点击自动提示控件，显示默认列表
        RxView.clicks(autoRecLoc)
                .filter(a -> autoRecLoc.getAdapter() != null)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(a -> {
                    hideKeyboard(autoRecLoc);
                    showAutoCompleteConfig(autoRecLoc);
                });

        //用户选择自动提示控件的某一条数据，隐藏输入法
        RxAutoCompleteTextView.itemClickEvents(autoRecLoc)
                .subscribe(a -> hideKeyboard(autoRecLoc));
    }

    @Override
    public void initData() {
        Bundle bundle = getArguments();
        //物料编码
        final String materialNum = bundle.getString(Global.EXTRA_MATERIAL_NUM_KEY);
        final String materialId = bundle.getString(Global.EXTRA_MATERIAL_ID_KEY);
        //发出库位
        final String invId = bundle.getString(Global.EXTRA_INV_ID_KEY);
        final String invCode = bundle.getString(Global.EXTRA_INV_CODE_KEY);
        //发出仓位
        mSendLocation = bundle.getString(Global.EXTRA_LOCATION_KEY);
        //发出批次
        final String batchFlag = bundle.getString(Global.EXTRA_BATCH_FLAG_KEY);
        //接收仓位
        final String recLocation = bundle.getString(Global.EXTRA_REC_LOCATION_KEY);
        //接收批次
        final String recBatchFlag = bundle.getString(Global.EXTRA_REC_BATCH_FLAG_KEY);
        mDeviceId = bundle.getString(Global.EXTRA_DEVICE_ID_KEY);
        //移库数量
        mQuantity = bundle.getString(Global.EXTRA_QUANTITY_KEY);
        //其他子节点的发出仓位列表
        mSendLocations = bundle.getStringArrayList(Global.EXTRA_LOCATION_LIST_KEY);
        mSpecialInvFlag = bundle.getString(Global.EXTRA_SPECIAL_INV_FLAG_KEY);
        mSpecialInvNum = bundle.getString(Global.EXTRA_SPECIAL_INV_NUM_KEY);
        //其他子节点的接收仓位列表
        mRecLocations = bundle.getStringArrayList(Global.EXTRA_REC_LOCATION_LIST_KEY);
        mLocationId = bundle.getString(Global.EXTRA_LOCATION_ID_KEY);
        //绑定数据
        tvMaterialNum.setText(materialNum);
        tvMaterialNum.setTag(materialId);
        tvSendInv.setText(invCode);
        tvSendInv.setTag(invId);
        etQuantity.setText(mQuantity);
        tvSendBatchFlag.setText(batchFlag);
        isWareHouseSame = TextUtils.isEmpty(recLocation) ? false : true;
        if (isWareHouseSame) {
            autoRecLoc.setText(recLocation);
            autoRecLoc.setEnabled(true);
        } else {
            autoRecLoc.setText("");
            autoRecLoc.setEnabled(false);
        }
        tvRecBatchFlag.setText(recBatchFlag);

        //获取缓存信息
        mPresenter.getTransferInfoSingle(mRefData.bizType, materialNum,
                Global.USER_ID, mRefData.workId, mRefData.invId, mRefData.recWorkId,
                mRefData.recInvId, batchFlag, "", -1);
    }

    @Override
    public void onBindCommonUI(ReferenceEntity refData, String batchFlag) {
        if (refData != null) {
            RefDetailEntity data = refData.billDetailList.get(0);
            //刷新UI
            tvMaterialNum.setTag(data.materialId);
            tvMaterialDesc.setText(data.materialDesc);
            tvMaterialGroup.setText(data.materialGroup);
            tvSendBatchFlag.setText(!TextUtils.isEmpty(data.batchFlag) ? data.batchFlag :
                    batchFlag);
            tvRecBatchFlag.setText(!TextUtils.isEmpty(data.batchFlag) ? data.batchFlag :
                    batchFlag);
            mHistoryDetailList = refData.billDetailList;
        }
    }

    @Override
    public void loadTransferSingleInfoComplete() {
        //获取库存信息
        mPresenter.getInventoryInfo(getInventoryQueryType(), mRefData.workId,
                CommonUtil.Obj2String(tvSendInv.getTag()), mRefData.workCode, getString(tvSendInv),
                "", getString(tvMaterialNum), tvMaterialNum.getTag().toString(),
                "", getString(tvSendBatchFlag), "", "", getInvType(), "");
    }

    @Override
    public void loadTransferSingleInfoFail(String message) {
        showMessage(message);
    }

    @Override
    public void showInventory(List<InventoryEntity> list) {
        mInventoryDatas.clear();
        mInventoryDatas.addAll(list);
        if (mSendLocAdapter == null) {
            mSendLocAdapter = new LocationAdapter(mActivity, R.layout.item_simple_sp, mInventoryDatas);
            spSendLoc.setAdapter(mSendLocAdapter);
        } else {
            mSendLocAdapter.notifyDataSetChanged();
        }

        //自动选定用户修改前的发出仓位
        //默认选择已经下架的仓位
        if (TextUtils.isEmpty(mSendLocation)) {
            spSendLoc.setSelection(0);
            return;
        }

        String locationCombine = null;
        if (!TextUtils.isEmpty(mSpecialInvFlag) && !TextUtils.isEmpty(mSpecialInvNum)) {
            locationCombine = mSendLocation + mSpecialInvFlag + mSpecialInvNum;
        } else {
            locationCombine = mSendLocation;
        }

        //自动匹配已经选中的发出仓位
        int pos = -1;
        for (InventoryEntity loc : mInventoryDatas) {
            pos++;
            if (!TextUtils.isEmpty(locationCombine) && locationCombine.equalsIgnoreCase(loc.locationCombine)) {
                break;
            } else if (mSendLocation.equalsIgnoreCase(loc.location)) {
                break;
            }
        }
        if (pos >= 0 && pos < list.size()) {
            spSendLoc.setSelection(pos);
        } else {
            spSendLoc.setSelection(0);
        }
    }

    /**
     * 加载发出库存完毕
     */
    @Override
    public void loadInventoryComplete() {
        //开始加载接收的库存
        if (mRefData == null) {
            showMessage("请先在抬头界面选择合适的移库数据");
            return;
        }
        if (TextUtils.isEmpty(mRefData.recWorkId)) {
            showMessage("接收工厂为空");
            return;
        }
        if (TextUtils.isEmpty(mRefData.recInvId)) {
            showMessage("接收库位为空");
            return;
        }
        mPresenter.getInventoryInfoOnRecLocation(getInventoryQueryType(), mRefData.recWorkId, mRefData.recInvId,
                mRefData.recWorkCode, mRefData.recInvCode, "", getString(tvMaterialNum),
                CommonUtil.Obj2String(tvMaterialNum.getTag()), "",
                getString(tvSendBatchFlag), "", "", getInvType(), mDeviceId);
    }

    @Override
    public void loadInventoryFail(String message) {
        showMessage(message);
    }



    @Override
    public void showRecLocations(List<String> recLocations) {
        ArrayList<String> tmp = new ArrayList<>();
        tmp.addAll(recLocations);
        if (mHistoryDetailList != null) {
            for (RefDetailEntity detail : mHistoryDetailList) {
                List<LocationInfoEntity> locationList = detail.locationList;
                if (locationList != null && locationList.size() > 0) {
                    for (LocationInfoEntity locationInfo : locationList) {
                        if(!TextUtils.isEmpty(locationInfo.recLocation) &&!recLocations.contains(locationInfo.recLocation)) {
                            tmp.add(locationInfo.recLocation);
                        }
                    }
                }
            }
        }
        if (tmp.size() > 0)
            setAutoCompleteConfig(autoRecLoc, tmp, autoRecLoc.getWidth());
    }

    @Override
    public void loadRecLocationsFail(String message) {
        //清除显示下拉列表
        autoRecLoc.setAdapter(null);
        ArrayList<String> tmp = new ArrayList<>();
        if (mHistoryDetailList != null) {
            for (RefDetailEntity detail : mHistoryDetailList) {
                List<LocationInfoEntity> locationList = detail.locationList;
                if (locationList != null && locationList.size() > 0) {
                    for (LocationInfoEntity locationInfo : locationList) {
                        if(!TextUtils.isEmpty(locationInfo.recLocation)) {
                            tmp.add(locationInfo.recLocation);
                        }
                    }
                }
            }
        }
        if (tmp.size() > 0)
            setAutoCompleteConfig(autoRecLoc, tmp, autoRecLoc.getWidth());
    }

    /**
     * 设置auto控件适配器以及下拉列表的宽度
     *
     * @param autoComplete
     * @param datas
     * @param width
     */
    private void setAutoCompleteConfig(AutoCompleteTextView autoComplete, List<String> datas, int width) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity,
                android.R.layout.simple_dropdown_item_1line, datas);
        autoRecLoc.setAdapter(adapter);
        autoComplete.setThreshold(1);
        autoComplete.setDropDownWidth(width);
        autoComplete.isPopupShowing();
    }

    /**
     * 用户选择发出仓位，匹配该仓位上的仓位数量
     */
    private void loadLocationQuantity(int position) {

        final String invQuantity = mInventoryDatas.get(position).invQuantity;
        final String locationCombine = mInventoryDatas.get(position).locationCombine;
        final String batchFlag = mInventoryDatas.get(position).batchFlag;

        tvInvQuantity.setText(invQuantity);

        if (TextUtils.isEmpty(locationCombine)) {
            showMessage("发出仓位为空");
            return;
        }

        if (mHistoryDetailList == null) {
            showMessage("请先获取物料信息");
            return;
        }

        String locQuantity = "0";
        String recLocation = "";
        String recBatchFlag = getString(tvRecBatchFlag);

        for (RefDetailEntity detail : mHistoryDetailList) {
            List<LocationInfoEntity> locationList = detail.locationList;
            if (locationList != null && locationList.size() > 0) {
                for (LocationInfoEntity locationInfo : locationList) {
                    if (!TextUtils.isEmpty(batchFlag)) {
                        if (locationCombine.equalsIgnoreCase(locationInfo.locationCombine)
                                && batchFlag.equalsIgnoreCase(locationInfo.batchFlag)) {
                            locQuantity = locationInfo.quantity;
                            recLocation = locationInfo.recLocation;
                            recBatchFlag = locationInfo.recBatchFlag;
                            mExtraLocationMap = locationInfo.mapExt;
                            mExtraLineMap = detail.mapExt;
                            break;
                        }
                    } else {
                        if (locationCombine.equalsIgnoreCase(locationInfo.locationCombine)) {
                            locQuantity = locationInfo.quantity;
                            recLocation = locationInfo.recLocation;
                            recBatchFlag = locationInfo.recBatchFlag;
                            mExtraLocationMap = locationInfo.mapExt;
                            mExtraLineMap = detail.mapExt;
                            break;
                        }
                    }
                }
            }
        }
        //绑定额外字段数据
        bindExtraUI(mSubFunEntity.locationConfigs, mExtraLocationMap);
        bindExtraUI(mSubFunEntity.collectionConfigs, mExtraLineMap);
        tvLocQuantity.setText(locQuantity);
        //注意如果缓存中没有接收批次或者接收仓位，或者已经手动赋值,那么不用缓存更新它们
        if (!TextUtils.isEmpty(recBatchFlag) && !TextUtils.isEmpty(getString(tvRecBatchFlag)))
            tvRecBatchFlag.setText(recBatchFlag);
    }

    /**
     * 用户修改的仓位不允许与其他子节点的仓位一致
     *
     * @return
     */
    private boolean isValidatedSendLocation() {
        if (TextUtils.isEmpty(mSendLocation)) {
            return false;
        }
        if (mSendLocations == null || mSendLocations.size() == 0)
            return true;
        for (String location : mSendLocations) {
            if (mSendLocation.equalsIgnoreCase(location)) {
                showMessage("您修改的仓位不合理,请重新输入");
                spSendLoc.setSelection(0);
                return false;
            }
        }
        return true;
    }

    protected boolean isValidatedRecLocation(String recLocation) {
        if (mRecLocations == null || mRecLocations.size() == 0)
            return true;
        for (String location : mRecLocations) {
            if (recLocation.equalsIgnoreCase(location)) {
                autoRecLoc.setText("");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean checkCollectedDataBeforeSave() {

        //检查是否合理，可以保存修改后的数据
        if (TextUtils.isEmpty(getString(etQuantity))) {
            showMessage("请输入移库数量");
            return false;
        }

        if (TextUtils.isEmpty(getString(tvInvQuantity))) {
            showMessage("请先获取库存");
            return false;
        }

        if (Float.parseFloat(getString(etQuantity)) <= 0.0f) {
            showMessage("输入移库数量有误，请重新输入");
            return false;
        }


        //检查接收仓位
        if (!isValidatedRecLocation(getString(autoRecLoc))) {
            showMessage("您输入的接收仓位不合理,请重新输入");
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

        //修改后的出库数量
        float quantityV = UiUtil.convertToFloat(getString(etQuantity), 0.0f);
        //是否满足本次录入数量<=库存数量
        final float invQuantityV = UiUtil.convertToFloat(getString(tvInvQuantity), 0.0f);
        if (Float.compare(quantityV, invQuantityV) > 0.0f) {
            showMessage("移库数量有误,请重新输入");
            etQuantity.setText("");
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
            result.voucherDate = mRefData.voucherDate;
            result.moveType = mRefData.moveType;
            result.userId = Global.USER_ID;
            result.workId = mRefData.workId;
            result.locationId = mLocationId;
            result.invType = result.invId = CommonUtil.Obj2String(tvSendInv.getTag());
            result.recWorkId = mRefData.recWorkId;
            result.recInvId = mRefData.recInvId;
            result.materialId = CommonUtil.Obj2String(tvMaterialNum.getTag());
            result.batchFlag = getString(tvSendBatchFlag);
            result.deviceId = mDeviceId;
            int locationPos = spSendLoc.getSelectedItemPosition();
            result.location = mInventoryDatas.get(locationPos).location;
            result.specialInvFlag = mInventoryDatas.get(locationPos).specialInvFlag;
            result.specialInvNum = mInventoryDatas.get(locationPos).specialInvNum;
            result.specialConvert = !TextUtils.isEmpty(result.specialInvFlag) && !TextUtils.isEmpty(result.specialInvNum) ?
                    "Y" : "N";
            result.recLocation = getString(autoRecLoc);
            result.recBatchFlag = getString(tvRecBatchFlag);
            result.quantity = getString(etQuantity);
            result.modifyFlag = "Y";
            result.invType = getInvType();
            result.mapExHead = createExtraMap(Global.EXTRA_HEADER_MAP_TYPE, mExtraLineMap, mExtraLocationMap);
            result.mapExLine = createExtraMap(Global.EXTRA_LINE_MAP_TYPE, mExtraLineMap, mExtraLocationMap);
            result.mapExLocation = createExtraMap(Global.EXTRA_LOCATION_MAP_TYPE, mExtraLineMap, mExtraLocationMap);
            emitter.onNext(result);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER)
                .compose(TransformerHelper.io2main())
                .subscribe(result -> mPresenter.uploadCollectionDataSingle(result));
    }

    @Override
    public void saveEditedDataSuccess(String message) {
        super.saveEditedDataSuccess(message);
        tvLocQuantity.setText(getString(etQuantity));
    }


    @Override
    public void retry(String retryAction) {
        switch (retryAction) {
            case Global.RETRY_LOAD_SINGLE_CACHE_ACTION:
                mPresenter.getTransferInfoSingle(mRefData.bizType, getString(tvMaterialNum),
                        Global.USER_ID, mRefData.workId, mRefData.invId, mRefData.recWorkId,
                        mRefData.recInvId, getString(tvSendBatchFlag), "", -1);
                break;
            case Global.RETRY_SAVE_COLLECTION_DATA_ACTION:
                saveCollectedData();
                break;
            case Global.RETRY_LOAD_INVENTORY_ACTION:
                mPresenter.getInventoryInfo(getInventoryQueryType(), mRefData.workId,
                        CommonUtil.Obj2String(tvSendInv.getTag()), mRefData.workCode, getString(tvSendInv),
                        "", getString(tvMaterialNum), tvMaterialNum.getTag().toString(),
                        "", getString(tvSendBatchFlag), "", "", getInvType(), "");
                break;
            case Global.RETRY_LOAD_REC_INVENTORY_ACTION:
                mPresenter.getInventoryInfoOnRecLocation(getInventoryQueryType(), mRefData.recWorkId, mRefData.recInvId,
                        mRefData.recWorkCode, mRefData.recInvCode, "", getString(tvMaterialNum),
                        CommonUtil.Obj2String(tvMaterialNum.getTag()), "",
                        getString(tvSendBatchFlag), "", "", getInvType(), mDeviceId);
                break;
        }
        super.retry(retryAction);
    }

    /**
     * 子类返回获取库存的类型
     * 0:表示代管库存,1:表示正常库存
     */
    protected abstract String getInvType();

    protected abstract String getInventoryQueryType();
}

