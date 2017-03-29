package com.richfit.barcodesystemproduct.module_returnstore.qingyang_rsy;

import android.support.annotation.NonNull;
import android.view.ViewStub;
import android.widget.TextView;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_header.BaseASHeaderFragment;

/**
 * Created by monday on 2017/3/29.
 */

public class QingYangRSYHeaderFragment extends BaseASHeaderFragment {

    TextView tvMoveType;
    TextView tvCostCenter;
    TextView tvNetNum;

    @NonNull
    @Override
    protected String getMoveType() {
        return "6";
    }

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected void initView() {
        ViewStub stub = (ViewStub) mActivity.findViewById(R.id.viewstub_qingyang_rsy_header);
        stub.inflate();
        //移动类型
        tvMoveType = (TextView) mActivity.findViewById(R.id.tv_move_type);
        //成本中心
        tvCostCenter = (TextView) mActivity.findViewById(R.id.tv_cost_center);
        //网络编号
        tvNetNum = (TextView) mActivity.findViewById(R.id.tv_net_num);
        super.initView();
    }

    /**
     * 为公共控件绑定数据
     */
    @Override
    public void bindCommonHeaderUI() {
        if (mRefData != null) {
            tvMoveType.setText(mRefData.moveType);
            tvCostCenter.setText(mRefData.costCenter);
            tvNetNum.setText(mRefData.netNum);
        }
        //调用父类刷新其他的公共组件
        super.bindCommonHeaderUI();
    }

    @Override
    public void _onPause() {
        clearCommonUI(tvCostCenter,tvMoveType,tvNetNum);
        super._onPause();
    }
}
