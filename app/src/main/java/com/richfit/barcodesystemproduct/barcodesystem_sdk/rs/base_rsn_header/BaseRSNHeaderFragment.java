package com.richfit.barcodesystemproduct.barcodesystem_sdk.rs.base_rsn_header;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxAdapterView;
import com.jakewharton.rxbinding2.widget.RxAutoCompleteTextView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.WorkAdapter;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.rs.base_rsn_header.imp.RSNPresenterImp;
import com.richfit.barcodesystemproduct.base.base_header.BaseHeaderFragment;
import com.richfit.common_lib.utils.DateChooseHelper;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.common_lib.widget.RichEditText;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.WorkEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;

/**
 * Created by monday on 2017/3/2.
 */

public abstract class BaseRSNHeaderFragment extends BaseHeaderFragment<RSNPresenterImp>
        implements IRSNHeaderView {

    private static final int ORG_FLAG = 0;

    @BindView(R.id.sp_work)
    Spinner spWork;
    @BindView(R.id.et_auto_comp)
    AutoCompleteTextView etAutoComp;
    @BindView(R.id.tv_auto_comp_name)
    TextView tvAutoCompName;
    @BindView(R.id.et_transfer_date)
    RichEditText etTransferDate;

    WorkAdapter mWorkAdapter;
    List<WorkEntity> mWorks;

    List<String> mCostCenters;
    ArrayAdapter mCostCenterAdapter;

    @Override
    protected int getContentId() {
        return R.layout.fragment_rsn_header;
    }

    @Override
    public void initVariable(Bundle savedInstanceState) {
        mRefData = null;
        mWorks = new ArrayList<>();
        mCostCenters = new ArrayList<>();
    }

    /**
     * 注册点击事件
     */
    @Override
    public void initEvent() {
        //过账日期
        etTransferDate.setOnRichEditTouchListener((view, text) ->
                DateChooseHelper.chooseDateForEditText(mActivity, etTransferDate, Global.GLOBAL_DATE_PATTERN_TYPE1));

        //选择工厂，初始化供应商或者项目编号
        RxAdapterView.itemSelections(spWork)
                .filter(position -> position.intValue() > 0)
                .subscribe(position -> mPresenter.getAutoCompleteList(mWorks.get(position).workCode,
                        getString(etAutoComp), 100, ORG_FLAG, mBizType));

        //点击自动提示控件，显示默认列表
        RxView.clicks(etAutoComp)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(a -> showAutoCompleteConfig(etAutoComp));

        //修改自动提示控件，说明用户需要锁乳关键字进行搜索，如果默认的列表中存在，那么不在向数据库进行查询
        RxTextView.textChanges(etAutoComp)
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter(str -> !TextUtils.isEmpty(str) && mCostCenters != null &&
                        mCostCenters.size() > 0 && !filterKeyWord(str) && spWork.getSelectedItemPosition() > 0)
                .subscribe(a -> mPresenter.getAutoCompleteList(mWorks.get(spWork.getSelectedItemPosition()).workCode,
                        getString(etAutoComp), 100, ORG_FLAG, mBizType));

        //用户选择自动提示控件的某一条数据，隐藏输入法
        RxAutoCompleteTextView.itemClickEvents(etAutoComp)
                .subscribe(a -> hideKeyboard(etAutoComp));

        //删除历史数据
        mPresenter.deleteCollectionData("", mBizType, Global.USER_ID, mCompanyCode);
    }

    @Override
    protected void initView() {
        //如果BizType是26那么显示成本中心,否者显示项目编号
        if ("47".equalsIgnoreCase(mBizType)) {
            tvAutoCompName.setText("项目编号");
        }
    }

    @Override
    public void initData() {
        SPrefUtil.saveData(mBizType, "0");
        etTransferDate.setText(UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1));
        //获取发出工厂列表
        mPresenter.getWorks(ORG_FLAG);
    }

    @Override
    public void showWorks(List<WorkEntity> works) {
        mWorks.clear();
        mWorks.addAll(works);
        //绑定适配器
        if (mWorkAdapter == null) {
            mWorkAdapter = new WorkAdapter(mActivity, R.layout.item_simple_sp, mWorks);
            spWork.setAdapter(mWorkAdapter);
        } else {
            mWorkAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void loadWorksFail(String message) {
        showMessage(message);
    }

    @Override
    public void showAutoCompleteList(List<String> list) {
        mCostCenters.clear();
        mCostCenters.addAll(list);
        if (mCostCenterAdapter == null) {
            mCostCenterAdapter = new ArrayAdapter<>(mActivity,
                    android.R.layout.simple_dropdown_item_1line, mCostCenters);
            etAutoComp.setAdapter(mCostCenterAdapter);
            setAutoCompleteConfig(etAutoComp);
        } else {
            mCostCenterAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void loadAutoCompleteFail(String message) {
        hideKeyboard(etAutoComp);
        showFailDialog(message);
    }

    @Override
    public void deleteCacheSuccess(String message) {
        showMessage(message);
    }

    @Override
    public void deleteCacheFail(String message) {
        showMessage(message);
    }

    private boolean filterKeyWord(CharSequence keyWord) {
        for (String item : mCostCenters) {
            if (item.contains(keyWord)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void _onPause() {
        super._onPause();
        if (spWork.getAdapter() != null && spWork.getSelectedItemPosition() > 0) {
            if (mRefData == null) {
                mRefData = new ReferenceEntity();
            }
            final int position = spWork.getSelectedItemPosition();
            mRefData.workId = mWorks.get(position).workId;
            mRefData.workCode = mWorks.get(position).workCode;
            mRefData.workName = mWorks.get(position).workName;

            if ("47".equalsIgnoreCase(mBizType)) {
                mRefData.projectNum = getString(etAutoComp).split("_")[0];
            } else {
                mRefData.costCenter = getString(etAutoComp).split("_")[0];
            }
            mRefData.bizType = mBizType;
            mRefData.voucherDate = getString(etTransferDate);
        }

    }

    @Override
    public void networkConnectError(String retryAction) {

    }

    @Override
    public boolean isNeedShowFloatingButton() {
        return false;
    }

    @Override
    public void clearAllUIAfterSubmitSuccess() {

    }
}
