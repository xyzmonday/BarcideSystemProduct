package com.richfit.barcodesystemproduct.module_movestore.qingyang_301n;

import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_collect.BaseMSNCollectFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_collect.imp.MSNCollectPresenterImp;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.L;
import com.richfit.domain.bean.ResultEntity;

import butterknife.BindView;

/**
 * 增加设备位号和设备名称。注意设备位号和设备名称只能通过设备id通过接口获取。
 * 主要的逻辑是如果通过扫描条码未获取到设备Id那么用户不能做该业务，如果存在
 * 设备Id,那么在获取物料信息后，先尝试调用getDeviceInfo的接口获取相关的
 * 信息，不管获取成功或者失败都应该让用户继续操作。
 * Created by monday on 2017/2/8.
 */

public class QingYangMSN301CollectFragment extends BaseMSNCollectFragment<MSNCollectPresenterImp> {

    /*设备位号*/
    @BindView(R.id.ll_device_location)
    LinearLayout llDeviceLocation;
    @BindView(R.id.tv_device_location)
    TextView tvDeviceLocation;
    /*设备名称*/
    @BindView(R.id.ll_device_name)
    LinearLayout llDeviceName;
    @BindView(R.id.tv_device_name)
    TextView tvDeviceName;


    /**
     * 处理扫描。重写该方法的目的是增加设备Id的处理逻辑
     *
     * @param type
     * @param list
     */
    @Override
    public void handleBarCodeScanResult(String type, String[] list) {
        if (list != null && list.length >= 12) {
            if (!etMaterialNum.isEnabled()) {
                showMessage("请先在抬头界面获取相关数据");
                return;
            }
            final String materialNum = list[Global.MATERIAL_POS];
            final String batchFlag = list[Global.BATCHFALG_POS];
            mDeviceId = list[list.length - 2];
//            mDeviceId = "5DEA6C98E1865F172D6604B702F779F3";
            L.e("扫描到的设备Id = " + mDeviceId);
            //如果设备Id为空那么不允许在操作.isContinue为true表示设备Id不为空可以继续该业务
            final boolean isContinue = !isEmpty(mDeviceId);
            //这里通过设备Id禁用掉获取物料信息
            etMaterialNum.setEnabled(isContinue);
            if (!isContinue) {
                showMessage("未获取到设备Id,请检查您的条码内容是否正确");
                return;
            }

            if (cbSingle.isChecked() && materialNum.equalsIgnoreCase(getString(etMaterialNum))) {
                //如果已经选中单品，那么说明已经扫描过一次。必须保证每一次的物料都一样
                saveCollectedData();
            } else {
                etMaterialNum.setText(materialNum);
                etSendBatchFlag.setText(batchFlag);
                loadMaterialInfo(materialNum, batchFlag);
            }
        } else if (list != null && list.length == 1 & !cbSingle.isChecked()) {
            final String location = list[0];
            if (etRecLoc.isFocused()) {
                etRecLoc.setText(location);
                return;
            }
            //扫描发出仓位
            if (spSendLoc.getAdapter() != null) {
                final int size = mInventoryDatas.size();
                for (int i = 0; i < size; i++) {
                    if (mInventoryDatas.get(i).location.equalsIgnoreCase(location)) {
                        spSendLoc.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    public void initView() {
        //打开设备相关的控件
        setVisibility(View.VISIBLE, llDeviceLocation, llDeviceName);
        super.initView();
    }

    @Override
    protected void loadMaterialInfo(String materialNum, String batchFlag) {
        if (TextUtils.isEmpty(materialNum)) {
            showMessage("物料编码为空,请重新输入");
            return;
        }
        if (isEmpty(mDeviceId)) {
            showMessage("设备位Id为空");
            return;
        }
        clearAllUI();
        mHistoryDetailList = null;
        mPresenter.getTransferInfoSingle(mRefData.bizType, materialNum,
                Global.USER_ID, mRefData.workId, mRefData.invId, mRefData.recWorkId,
                mRefData.recInvId, batchFlag, "", -1);
    }

    /**
     * 重写该方法的目的是改变获取发出库位的时机，也就是说必须先去尝试获取设备相关的信息
     */
    @Override
    public void loadTransferSingleInfoComplete() {
        if (isEmpty(mDeviceId)) {
            showMessage("设备Id为空");
            return;
        }
        mPresenter.getDeviceInfo(mDeviceId);
    }

    /**
     * 获取设备信息成功，开始将信息显示
     *
     * @param result
     */
    @Override
    public void getDeviceInfoSuccess(ResultEntity result) {
        tvDeviceLocation.setText(result.deviceLocation);
        tvDeviceName.setText(result.deviceName);
    }

    /**
     * 如果设备信息失败不允许做其他的业务
     */
    @Override
    public void getDeviceInfoFail(String message) {
        showMessage(message);
        //禁用掉发出库位，以便禁止业务继续
        mSendInvs.clear();
        if(mSendInvAdapter != null) {
            mSendInvAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void getDeviceInfoComplete() {
        mPresenter.getSendInvsByWorks(mRefData.workId, getOrgFlag());
    }

    @Override
    protected boolean checkHeaderData() {
        if (TextUtils.isEmpty(mRefData.workId)) {
            showMessage("请先选择发出工厂");
            return false;
        }

        if (TextUtils.isEmpty(mRefData.invId)) {
            showMessage("请先选择发出库位");
            return false;
        }

        if (TextUtils.isEmpty(mRefData.recWorkId)) {
            showMessage("请先选择接收工厂");
            return false;
        }

        if (TextUtils.isEmpty(mRefData.recInvId)) {
            showMessage("请先选择接收库位");
            return false;
        }
        return true;
    }

    @Override
    public boolean checkCollectedDataBeforeSave() {
        if (isEmpty(mDeviceId)) {
            showMessage("设备Id为空");
            return false;
        }
        //发出工厂
        if (TextUtils.isEmpty(mRefData.workId)) {
            showMessage("请先选择发出工厂");
            return false;
        }
        //接收工厂
        if (TextUtils.isEmpty(mRefData.recWorkId)) {
            showMessage("请先选择接收工厂");
            return false;
        }
        //检查发出批次
        if (mIsOpenBatchManager && TextUtils.isEmpty(getString(etSendBatchFlag))) {
            showMessage("发出批次为空");
            return false;
        }

        //检查接收批次
        if (mIsOpenBatchManager && TextUtils.isEmpty(getString(etRecBatchFlag))) {
            showMessage("请输入接收批次");
            return false;
        }

        final int sendLocPos = spSendLoc.getSelectedItemPosition();
        if(sendLocPos <= 0) {
            showMessage("请先选择发出仓位");
            return false;
        }
        final String sendLocation = mInventoryDatas.get(sendLocPos).location;
        if(!TextUtils.isEmpty(sendLocation) && sendLocation.length() != 11) {
            showMessage("您选择的发出仓位格式不合理");
            return false;
        }

        //检查接收仓位
        if (isWareHouseSame && TextUtils.isEmpty(getString(etRecLoc))) {
            showMessage("请输入接收仓位");
            return false;
        }

        if(isWareHouseSame && getString(etRecLoc).length() != 11) {
            showMessage("您输入的接收仓位格式不对");
            return false;
        }

        return super.checkCollectedDataBeforeSave();
    }

    @Override
    protected String getInvType() {
        return getString(R.string.invTypeDaiGuan);
    }

    @Override
    protected String getInventoryQueryType() {
        return getString(R.string.inventoryQueryTypePrecise);
    }

    @Override
    public void _onPause() {
        clearCommonUI(tvDeviceLocation,tvDeviceName);
        super._onPause();
    }

    @Override
    protected boolean getWMOpenFlag() {
        return false;
    }

    @Override
    protected int getOrgFlag() {
        return getInteger(R.integer.orgSecond);
    }
}
