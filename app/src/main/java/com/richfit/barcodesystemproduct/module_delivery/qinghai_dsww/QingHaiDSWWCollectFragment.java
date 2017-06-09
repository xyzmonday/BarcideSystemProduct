package com.richfit.barcodesystemproduct.module_delivery.qinghai_dsww;

import android.text.TextUtils;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_ds_collect.BaseDSCollectFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_ds_collect.imp.DSCollectPresenterImp;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.RefDetailEntity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;

/**
 * Created by monday on 2017/3/5.
 */

public class QingHaiDSWWCollectFragment extends BaseDSCollectFragment<DSCollectPresenterImp> {

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }


    protected boolean refreshQuantity(final String quantity) {
        if (Float.valueOf(quantity) <= 0.0f) {
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

        float totalQuantityV = 0.0f;
        //累计数量
        totalQuantityV += UiUtil.convertToFloat(getString(tvTotalQuantity), 0.0f);
        //应该数量
        final float actQuantityV = UiUtil.convertToFloat(getString(tvActQuantity), 0.0f);
        //本次出库数量
        final float quantityV = UiUtil.convertToFloat(quantity, 0.0f);
        if (Float.compare(quantityV + totalQuantityV, actQuantityV) > 0.0f) {
            showMessage("输入数量有误，请重新输入");
            etQuantity.setText("");
            return false;
        }
        //该仓位的历史出库数量
        final float historyQuantityV = UiUtil.convertToFloat(getString(tvLocQuantity), 0.0f);
        //该仓位的库存数量
        final float invQuantityV = UiUtil.convertToFloat(getString(tvInvQuantity), 0.0f);
        if (Float.compare(quantityV + historyQuantityV, invQuantityV) > 0.0f) {
            showMessage("输入数量有误，请重新输入");
            etQuantity.setText("");
            return false;
        }
        return true;
    }


    /**
     * 通过物料编码和批次匹配单据明细的行。这里我们返回的所有行的insLot集合
     *
     * @param materialNum
     * @param batchFlag
     * @return
     */
    @Override
    protected Flowable<ArrayList<String>> matchMaterialInfo(final String materialNum, final String batchFlag) {
        if (mRefData == null || mRefData.billDetailList == null ||
                mRefData.billDetailList.size() == 0 || TextUtils.isEmpty(materialNum)) {
            return Flowable.error(new Throwable("请先获取单据明细"));
        }
        ArrayList<String> refLineIds = new ArrayList<>();
        List<RefDetailEntity> list = mRefData.billDetailList;

        for (RefDetailEntity entity : list) {
            if (isOpenBatchManager) {
                final String refLineId = entity.refLineId;
                //如果打开了批次，那么在看明细中是否有批次
                if (!TextUtils.isEmpty(entity.batchFlag) && !TextUtils.isEmpty(batchFlag)) {
                    if (materialNum.equalsIgnoreCase(entity.materialNum) &&
                            batchFlag.equalsIgnoreCase(entity.batchFlag) &&
                            !TextUtils.isEmpty(refLineId))

                        refLineIds.add(refLineId);
                } else {
                    if (materialNum.equalsIgnoreCase(entity.materialNum) &&
                            !TextUtils.isEmpty(refLineId))
                        refLineIds.add(refLineId);
                }
            } else {
                final String refLineId = entity.refLineId;
                //如果明细中没有打开了批次管理,那么只匹配物料编码
                if (materialNum.equalsIgnoreCase(entity.materialNum) && !TextUtils.isEmpty(refLineId))
                    refLineIds.add(refLineId);

            }
        }
        if (refLineIds.size() == 0) {
            return Flowable.error(new Throwable("未获取到匹配的物料"));
        }
        return Flowable.just(refLineIds);
    }

    /**
     * 通过单据行的检验批得到该行在单据明细列表中的位置
     *
     * @param refLineId:物料+批次
     * @return 返回该行号对应的行明细在明细列表的索引
     */
    @Override
    protected int getIndexByLineNum(String refLineId) {
        int index = -1;
        if (TextUtils.isEmpty(refLineId))
            return index;

        if (mRefData == null || mRefData.billDetailList == null
                || mRefData.billDetailList.size() == 0)
            return index;

        for (RefDetailEntity item : mRefData.billDetailList) {
            index++;
            if (!TextUtils.isEmpty(item.refLineId) && refLineId.equals(item.refLineId)) {
                break;
            }
        }
        return index;
    }

    @Override
    protected String getInvType() {
        return "01";
    }

    @Override
    protected String getInventoryQueryType() {
        return getString(R.string.inventoryQueryTypeSAPLocation);
    }

    @Override
    protected int getOrgFlag() {
        return 0;
    }
}
