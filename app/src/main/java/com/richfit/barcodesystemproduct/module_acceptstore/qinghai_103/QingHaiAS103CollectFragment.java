package com.richfit.barcodesystemproduct.module_acceptstore.qinghai_103;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_collect.BaseASCollectFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_collect.imp.ASCollectPresenterImp;
import com.richfit.common_lib.utils.Global;


/**
 * 注意103入库不上架也不打开批次管理
 * Created by monday on 2017/2/17.
 */

public class QingHaiAS103CollectFragment extends BaseASCollectFragment<ASCollectPresenterImp> {

    @Override
    public void handleBarCodeScanResult(String type, String[] list) {
        if (list != null && list.length > 12) {
            final String materialNum = list[Global.MATERIAL_POS];
            final String batchFlag = list[Global.BATCHFALG_POS];
            if (cbSingle.isChecked() && materialNum.equalsIgnoreCase(getString(etMaterialNum))) {
                //如果已经选中单品，那么说明已经扫描过一次。必须保证每一次的物料都一样
                saveCollectedData();
            } else {
                //在单品模式下，扫描不同的物料
                etMaterialNum.setText(materialNum);
                loadMaterialInfo(materialNum, batchFlag);
            }
        }
    }

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected void initVariable(@Nullable Bundle savedInstanceState) {
        super.initVariable(savedInstanceState);
        //不上架，不能输入仓位，单条缓存有库存地点触发
        isNLocation = true;
    }

    @Override
    public void initView() {
        llBatchFlag.setVisibility(View.GONE);
        llLocation.setVisibility(View.GONE);
        llLocationQuantity.setVisibility(View.GONE);
        super.initView();
    }



    @Override
    public void bindCommonCollectUI() {
        super.bindCommonCollectUI();
        //强制不进行批次检查
        isOpenBatchManager = false;
    }

    @Override
    public boolean checkCollectedDataBeforeSave() {

        if (mRefData == null) {
            showMessage("请先在抬头界面获取单据数据");
            return false;
        }
        //检查数据是否可以保存
        if (spRefLine.getSelectedItemPosition() == 0) {
            showMessage("请先选择单据行");
            return false;
        }
        //库存地点
        if (spInv.getSelectedItemPosition() == 0) {
            showMessage("请先选择库存地点");
            return false;
        }

        //物资条码
        if (TextUtils.isEmpty(getString(etMaterialNum))) {
            showMessage("请先输入物料条码");
            return false;
        }

        //实发数量
        if (!cbSingle.isChecked() && TextUtils.isEmpty(getString(etQuantity))) {
            showMessage("请先输入数量");
            return false;
        }

        if (!refreshQuantity(cbSingle.isChecked() ? "1" : getString(etQuantity))) {
            showMessage("实收数量有误");
            return false;
        }

        return true;
    }


    @Override
    protected int getOrgFlag() {
        return 0;
    }
}
