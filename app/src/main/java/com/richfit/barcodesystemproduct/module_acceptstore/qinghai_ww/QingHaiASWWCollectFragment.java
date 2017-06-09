package com.richfit.barcodesystemproduct.module_acceptstore.qinghai_ww;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.BottomMenuAdapter;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_collect.BaseASCollectFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_collect.imp.ASCollectPresenterImp;
import com.richfit.barcodesystemproduct.module.main.MainActivity;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.BottomMenuEntity;
import com.richfit.domain.bean.RefDetailEntity;

import java.util.ArrayList;
import java.util.List;

import static com.richfit.common_lib.utils.Global.COMPANY_CODE;

/**
 * 委外入库逻辑比较复杂:
 * 1. 批次不能输入,只能是通过扫描或则单据中带出来(enable = false,表示可以不输入批次)
 * 2. 对于必检的物料，批次显示为空(isQmFlag = true的物料清空批次);
 * 3. 对于必检的物料，仓位不能输入(isNLocation = true);
 * 4. 对于非必检的物料，批次扫描或则从单据中带出来，而且必须检查批次一致性检查;
 * 5. lineType不等于3表示不是委外单据不允许做该业务
 * Created by monday on 2017/2/20.
 */

public class QingHaiASWWCollectFragment extends BaseASCollectFragment<ASCollectPresenterImp> {

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    public void initEvent() {
        super.initEvent();
        /*监听上架仓位点击事件(注意如果是必检物资该监听无效)*/
        etLocation.setOnRichAutoEditTouchListener((view, location) -> getTransferSingle(getString(etBatchFlag), location));
    }

    @Override
    public void initDataLazily() {
        super.initDataLazily();
        etBatchFlag.setEnabled(false);
    }

    /**
     * 绑定UI。
     */
    @Override
    public void bindCommonCollectUI() {
        mSelectedRefLineNum = mRefLines.get(spRefLine.getSelectedItemPosition());
        RefDetailEntity lineData = getLineData(mSelectedRefLineNum);
        if (lineData != null && !"3".equals(lineData.lineType)) {
            showMessage("该物料不允许做委外入库");
            return;
        }
        super.bindCommonCollectUI();
        //处理必检物资逻辑
        //如果是必检物资，不显示批次(注意也不检查批次的输入)
        isQmFlag = "X".equalsIgnoreCase(lineData.qmFlag);//true表示质检物资
        //如果是质检物资不做上架处理
        isNLocation = isQmFlag;
        etLocation.setEnabled(!isNLocation);
        //如果是质检，不管是否有批次都要清除
        if (isQmFlag)
            etBatchFlag.setText("");
    }


    @Override
    public void showOperationMenuOnCollection(final String companyCode) {
        View rootView = LayoutInflater.from(mActivity).inflate(R.layout.menu_bottom, null);
        GridView menu = (GridView) rootView.findViewById(R.id.gridview);

        BottomMenuAdapter adapter = new BottomMenuAdapter(mActivity, R.layout.item_bottom_menu, provideDefaultBottomMenu());
        menu.setAdapter(adapter);

        final Dialog dialog = new Dialog(mActivity, R.style.MaterialDialogSheet);
        dialog.setContentView(rootView);
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();

        menu.setOnItemClickListener((adapterView, view, position, id) -> {
            switch (position) {
                case 0:
                    //1.保存数据
                    saveCollectedData();
                    break;
                case 1:
                    startComponent();
                    break;

            }
            dialog.dismiss();
        });
    }

    /**
     * 启动组件。必须保证用户已经采集过数据。
     */
    private void startComponent() {

        //如果抬头界面请求没有获取到缓存，或者删除了缓存，那么必须判断保存了本次采集的数据
        if (!TextUtils.isEmpty(mRefData.transId)) {
            //检查缓存的累计数量
            final float totalQuantityV = UiUtil.convertToFloat(getString(tvTotalQuantity), 0.0f);
            if (Float.compare(totalQuantityV, 0.0f) <= 0) {
                showMessage("请先保存采集的数据");
                return;
            }
        }

        if (TextUtils.isEmpty(mSelectedRefLineNum)) {
            showMessage("请选获取物料信息");
            return;
        }

        Intent intent = new Intent(mActivity, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(Global.EXTRA_COMPANY_CODE_KEY, COMPANY_CODE);
        bundle.putString(Global.EXTRA_MODULE_CODE_KEY, "");
        bundle.putString(Global.EXTRA_BIZ_TYPE_KEY, "19_ZJ");
        bundle.putString(Global.EXTRA_REF_TYPE_KEY, mRefType);
        bundle.putString(Global.EXTRA_CAPTION_KEY, "委外入库-组件");
        bundle.putString(Global.EXTRA_REF_LINE_NUM_KEY, mSelectedRefLineNum);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    protected List<BottomMenuEntity> provideDefaultBottomMenu() {
        ArrayList<BottomMenuEntity> menus = new ArrayList<>();
        BottomMenuEntity menu = new BottomMenuEntity();
        menu.menuName = "保存数据";
        menu.menuImageRes = R.mipmap.icon_transfer;
        menus.add(menu);

        menu = new BottomMenuEntity();
        menu.menuName = "组件";
        menu.menuImageRes = R.mipmap.icon_component;
        menus.add(menu);

        return menus;
    }


    @Override
    protected int getOrgFlag() {
        return 0;
    }

    @Override
    public boolean checkCollectedDataBeforeSave() {
        //对于上架仓位的检查
        if (!isNLocation) {
            final String location = getString(etLocation);
            if (TextUtils.isEmpty(location)) {
                showMessage("请输入上架仓位");
                return false;
            }
            if (location.length() > 10) {
                showMessage("您输入的上架不合理");
                return false;
            }
        }
        return super.checkCollectedDataBeforeSave();
    }


}
