package com.richfit.barcodesystemproduct.module_returnstore.qinghai_rsn.edit;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.module_returnstore.qinghai_rsn.edit.imp.QingHaiRSNEditPresenterImp;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.utils.CommonUtil;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.L;
import com.richfit.domain.bean.LocationInfoEntity;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.ResultEntity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by monday on 2017/3/2.
 */

public class QingHaiRSNEditFragment extends BaseFragment<QingHaiRSNEditPresenterImp>
        implements IQingHaiRSNEditView {


    @BindView(R.id.tv_material_num)
    TextView tvMaterialNum;
    @BindView(R.id.tv_material_desc)
    TextView tvMaterialDesc;
    @BindView(R.id.tv_material_group)
    TextView tvMaterialGroup;
    @BindView(R.id.tv_batch_flag)
    TextView tvBatchFlag;
    @BindView(R.id.tv_inv)
    TextView tvInv;
    @BindView(R.id.et_location)
    EditText etLocation;
    @BindView(R.id.tv_location_quantity)
    TextView tvLocQuantity;
    @BindView(R.id.et_quantity)
    EditText etQuantity;

    String mQuantity;
    /*缓存的历史仓位数量*/
    private List<RefDetailEntity> mHistoryDetailList;
    /*缓存的仓位级别的额外字段*/
    Map<String, Object> mExtraLocationMap;
    /*缓存的行级别的额外字段*/
    Map<String, Object> mExtraLineMap;
    //修改前的上架仓位
    String mSelectedLocation;
    String mLocationId;


    @Override
    protected int getContentId() {
        return R.layout.fragment_rsn_edit;
    }

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected void initView() {
         /*生成额外控件*/
        createExtraUI(mSubFunEntity.collectionConfigs, BaseFragment.EXTRA_VERTICAL_ORIENTATION_TYPE);
        createExtraUI(mSubFunEntity.locationConfigs, BaseFragment.EXTRA_VERTICAL_ORIENTATION_TYPE);
    }

    @Override
    public void initEvent() {
        //监听上架仓位输入，时时匹配缓存的仓位数量
        RxTextView.textChanges(etLocation)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(location -> !TextUtils.isEmpty(location) && location.length() > 0)
                .debounce(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(location -> loadLocationQuantity(location.toString(), getString(tvBatchFlag)), e -> L.d(e.getMessage()));
    }

    @Override
    public void initData() {
        Bundle bundle = getArguments();
        //物料编码
        final String materialNum = bundle.getString(Global.EXTRA_MATERIAL_NUM_KEY);
        final String materialId = bundle.getString(Global.EXTRA_MATERIAL_ID_KEY);
        //库位
        final String invId = bundle.getString(Global.EXTRA_INV_ID_KEY);
        final String invCode = bundle.getString(Global.EXTRA_INV_CODE_KEY);
        //批次
        final String batchFlag = bundle.getString(Global.EXTRA_BATCH_FLAG_KEY);
        //仓位
        mSelectedLocation = bundle.getString(Global.EXTRA_LOCATION_KEY);
        mLocationId = bundle.getString(Global.EXTRA_LOCATION_ID_KEY);
        //入库数量
        mQuantity = bundle.getString(Global.EXTRA_QUANTITY_KEY);

        //绑定数据
        tvMaterialNum.setText(materialNum);
        tvMaterialNum.setTag(materialId);
        tvInv.setText(invCode);
        tvInv.setTag(invId);
        etQuantity.setText(mQuantity);
        tvBatchFlag.setText(batchFlag);

        etLocation.setEnabled(false);

        //获取缓存信息
        mPresenter.getTransferInfoSingle(mRefData.bizType, materialNum,
                Global.USER_ID, mRefData.workId, mRefData.invId, mRefData.recWorkId,
                mRefData.recInvId, batchFlag, "", -1);
    }

    @Override
    public void onBindCommonUI(ReferenceEntity refData, String batchFlag) {
        RefDetailEntity data = refData.billDetailList.get(0);
        //刷新UI
        tvMaterialNum.setTag(data.materialId);
        tvMaterialDesc.setText(data.materialDesc);
        tvMaterialGroup.setText(data.materialGroup);
        tvBatchFlag.setText(!TextUtils.isEmpty(data.batchFlag) ? data.batchFlag : batchFlag);
        mHistoryDetailList = refData.billDetailList;
    }

    @Override
    public void loadTransferSingleInfoFail(String message) {
        showMessage(message);
    }

    @Override
    public void loadTransferSingeInfoComplete() {
        //设置上架仓位，系统自动匹配最新的缓存
        etLocation.setText(mSelectedLocation);
    }

    /**
     * 加载缓存中仓位数量
     *
     * @param location
     */
    private void loadLocationQuantity(String location, String batchFlag) {

        if (mIsOpenBatchManager && TextUtils.isEmpty(batchFlag)) {
            showMessage("批次为空");
            return;
        }

        if (TextUtils.isEmpty(location)) {
            showMessage("仓位为空");
            return;
        }

        if (mHistoryDetailList == null) {
            showMessage("请先获取物料信息");
            return;
        }

        String locQuantity = "0";

        for (RefDetailEntity detail : mHistoryDetailList) {
            List<LocationInfoEntity> locationList = detail.locationList;
            if (locationList != null && locationList.size() > 0) {
                for (LocationInfoEntity locationInfo : locationList) {
                    if (!TextUtils.isEmpty(batchFlag)) {
                        if (location.equalsIgnoreCase(locationInfo.location)
                                && batchFlag.equalsIgnoreCase(locationInfo.batchFlag)) {
                            locQuantity = locationInfo.quantity;
                            mExtraLocationMap = locationInfo.mapExt;
                            mExtraLineMap = detail.mapExt;
                            break;
                        }
                    } else {
                        if (location.equalsIgnoreCase(locationInfo.location)) {
                            locQuantity = locationInfo.quantity;
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
    }

    @Override
    public boolean checkCollectedDataBeforeSave() {

        //检查是否合理，可以保存修改后的数据
        if (TextUtils.isEmpty(getString(etLocation))) {
            showMessage("请输入上架仓位");
            return false;
        }

        if (TextUtils.isEmpty(getString(etQuantity))) {
            showMessage("请输入移库数量");
            return false;
        }

        if (Float.parseFloat(getString(etQuantity)) <= 0.0f) {
            showMessage("输入移库数量有误，请重新输入");
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
            result.invId = CommonUtil.Obj2String(tvInv.getTag());
            result.recWorkId = mRefData.recWorkId;
            result.recInvId = mRefData.recInvId;
            result.materialId = CommonUtil.Obj2String(tvMaterialNum.getTag());
            result.batchFlag = getString(tvBatchFlag);
            result.location = getString(etLocation);
            result.locationId = mLocationId;
            result.quantity = getString(etQuantity);
            result.supplierId = mRefData.supplierId;
            result.costCenter = mRefData.costCenter;
            result.projectNum = mRefData.projectNum;
            result.invType = getString(R.string.invTypeNorm);
            result.modifyFlag = "Y";
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
    public void saveCollectedDataSuccess() {
        showMessage("修改成功");
        tvLocQuantity.setText(getString(etQuantity));
    }

    @Override
    public void saveCollectedDataFail(String message) {
        showMessage("修改失败");
    }

    @Override
    public void retry(String retryAction) {
        switch (retryAction) {
            case Global.RETRY_LOAD_SINGLE_CACHE_ACTION:
                mPresenter.getTransferInfoSingle(mRefData.bizType, getString(tvMaterialNum),
                        Global.USER_ID, mRefData.workId, mRefData.invId, mRefData.recWorkId,
                        mRefData.recInvId, getString(tvBatchFlag), "", -1);
                break;
            case Global.RETRY_SAVE_COLLECTION_DATA_ACTION:
                saveCollectedData();
                break;
        }
        super.retry(retryAction);
    }

}
