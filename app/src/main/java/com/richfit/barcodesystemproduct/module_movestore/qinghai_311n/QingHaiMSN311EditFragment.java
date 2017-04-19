package com.richfit.barcodesystemproduct.module_movestore.qinghai_311n;

import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_edit.BaseMSNEditFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_edit.imp.MSNEditPresenterImp;

/**
 * Created by monday on 2017/2/17.
 */

public class QingHaiMSN311EditFragment extends BaseMSNEditFragment<MSNEditPresenterImp> {

    String specialConvert = "N";

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    public void initData() {
        etRecLoc.setEnabled(false);
        super.initData();
    }

    @Override
    public void showOperationMenuOnCollection(final String companyCode) {
        //每一次保存之前需要重置该字段
        specialConvert = "N";
        boolean isTurn = false;
        final int position = spSendLoc.getSelectedItemPosition();
        if (position >= 0 && !TextUtils.isEmpty(mInventoryDatas.get(position).specialInvFlag)
                && !TextUtils.isEmpty(mInventoryDatas.get(position).specialInvNum)) {
            isTurn = true;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("温馨提示");
        String message = isTurn ? "检测到有寄售库存,您是否要进行寄售转自有" : "您真的确定要保存本次修改的数据?";
        builder.setMessage(message);
        //  第一个按钮
        builder.setPositiveButton("直接修改", (dialog, which) -> {
            dialog.dismiss();
            saveCollectedData();
        });
        if (isTurn) {
            builder.setNeutralButton("寄售转自有", (dialog, which) -> {
                dialog.dismiss();
                specialConvert = "Y";
                saveCollectedData();
            });
        }
        //  第三个按钮
        builder.setNegativeButton("取消修改", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }


    @Override
    public boolean checkCollectedDataBeforeSave() {
        final String recLocation = getString(etRecLoc);
        if(TextUtils.isEmpty(recLocation) || recLocation.length() > 10) {
            showMessage("您输入的接收仓位不合理");
            return false;
        }
        return super.checkCollectedDataBeforeSave();
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
