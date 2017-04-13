package com.richfit.barcodesystemproduct.module_movestore.qinghai_311n;

import android.text.TextUtils;
import android.widget.TextView;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_edit.BaseMSNEditFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_edit.imp.MSNEditPresenterImp;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.utils.CommonUtil;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.widget.RichEditText;
import com.richfit.domain.bean.LocationInfoEntity;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ResultEntity;

import java.util.List;

import butterknife.BindView;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;

/**
 * Created by monday on 2017/4/12.
 */

public class QingHaiLMSNEditFragment extends BaseMSNEditFragment<MSNEditPresenterImp> {


    @BindView(R.id.et_send_location)
    RichEditText etSendLocation;
    @BindView(R.id.tv_special_inv_flag)
    TextView tvSpecialInvFlag;
    @BindView(R.id.tv_special_inv_num)
    TextView tvSpecialInvNum;

    @Override
    public int getContentId() {
        return R.layout.fragment_lmsn_edit;
    }

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    public void initEvent() {
        etSendLocation.setOnRichEditTouchListener((view, location) -> {
            hideKeyboard(view);
            loadLocationQuantity(getString(tvSendBatchFlag), location);
        });
    }

    @Override
    public void loadTransferSingleInfoComplete() {
        //获取缓存成功后不再获取库存，直接加载仓位数量
        loadLocationQuantity(getString(tvSendBatchFlag), getString(etSendLocation));
    }

    @Override
    public void initData() {
        etRecLoc.setEnabled(false);
        super.initData();
        tvSpecialInvFlag.setText(mSpecialInvFlag);
        tvSpecialInvNum.setText(mSpecialInvNum);
        etSendLocation.setText(mSendLocation);

    }

    /**
     * 匹配仓位数量
     *
     * @param batchFlag
     * @param location
     */
    private void loadLocationQuantity(String batchFlag, String location) {
        String locationCombine;
        if (!TextUtils.isEmpty(mSpecialInvFlag) && !TextUtils.isEmpty(mSpecialInvNum)) {
            locationCombine = location + "_" + mSpecialInvFlag + "_" + mSpecialInvNum;
        } else {
            locationCombine = location;
        }
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
                            break;
                        }
                    } else {
                        if (locationCombine.equalsIgnoreCase(locationInfo.locationCombine)) {
                            locQuantity = locationInfo.quantity;
                            recLocation = locationInfo.recLocation;
                            recBatchFlag = locationInfo.recBatchFlag;
                            break;
                        }
                    }
                }
            }
        }
        //绑定额外字段数据
        tvLocQuantity.setText(locQuantity);
        //注意如果缓存中没有接收批次或者接收仓位，或者已经手动赋值,那么不用缓存更新它们
        if (!TextUtils.isEmpty(recLocation) && !TextUtils.isEmpty(getString(etRecLoc)))
            etRecLoc.setText(recLocation);
        if (!TextUtils.isEmpty(recBatchFlag) && !TextUtils.isEmpty(getString(tvRecBatchFlag)))
            tvRecBatchFlag.setText(recBatchFlag);
    }

    @Override
    public boolean checkCollectedDataBeforeSave() {
        final String sendLocation = getString(etSendLocation);
        if (TextUtils.isEmpty(sendLocation) || sendLocation.length() > 10) {
            showMessage("您输入的发出仓位不合理");
            return false;
        }
        final String recLocation = getString(etRecLoc);
        if (TextUtils.isEmpty(recLocation) || recLocation.length() > 10) {
            showMessage("您输入的接收仓位不合理");
            return false;
        }
        //检查是否合理，可以保存修改后的数据
        if (TextUtils.isEmpty(getString(etQuantity))) {
            showMessage("请输入移库数量");
            return false;
        }

        if (Float.parseFloat(getString(etQuantity)) <= 0.0f) {
            showMessage("输入移库数量有误，请重新输入");
            return false;
        }

        //检查接收仓位
        if (!isValidatedRecLocation(getString(etRecLoc))) {
            showMessage("您输入的接收仓位不合理,请重新输入");
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
            result.invType =
                    result.invId = CommonUtil.Obj2String(tvSendInv.getTag());
            result.recWorkId = mRefData.recWorkId;
            result.recInvId = mRefData.recInvId;
            result.materialId = CommonUtil.Obj2String(tvMaterialNum.getTag());
            result.batchFlag = getString(tvSendBatchFlag);

            result.location = getString(etSendLocation).toUpperCase();
            result.specialInvFlag = getString(tvSpecialInvFlag);
            result.specialInvNum = getString(tvSpecialInvNum);
            result.recLocation = getString(etRecLoc);
            result.recBatchFlag = getString(tvRecBatchFlag);
            result.quantity = getString(etQuantity);
            result.modifyFlag = "Y";
            result.invType = getInvType();
            emitter.onNext(result);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER)
                .compose(TransformerHelper.io2main())
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
}
