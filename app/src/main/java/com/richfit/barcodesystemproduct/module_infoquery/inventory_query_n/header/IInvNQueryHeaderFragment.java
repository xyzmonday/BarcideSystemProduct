package com.richfit.barcodesystemproduct.module_infoquery.inventory_query_n.header;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.jakewharton.rxbinding2.view.RxView;
import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.InvAdapter;
import com.richfit.barcodesystemproduct.adapter.WorkAdapter;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.module_infoquery.inventory_query_n.header.imp.IInvNQueryHeaderPresenterImp;
import com.richfit.domain.bean.InvEntity;
import com.richfit.domain.bean.WorkEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;

/**
 * 物资库存信息查询-无参考
 * Created by monday on 2017/5/25.
 */

public class IInvNQueryHeaderFragment extends BaseFragment<IInvNQueryHeaderPresenterImp>
        implements IInvNQueryHeaderView {

    @BindView(R.id.sp_work)
    Spinner spWork;
    @BindView(R.id.ll_send_work)
    LinearLayout llSendWork;
    @BindView(R.id.sp_inv)
    Spinner spInv;
    @BindView(R.id.et_material_class)
    EditText etMaterialClass;
    @BindView(R.id.et_material_desc)
    EditText etMaterialDesc;
    @BindView(R.id.floating_button)
    FloatingActionButton mBtnQuery;

    /*工厂*/
    WorkAdapter mWorkAdapter;
    List<WorkEntity> mWorks;

    /*库位*/
    InvAdapter mInvAdapter;
    List<InvEntity> mInvs;

    @Override
    protected int getContentId() {
        return R.layout.fragment_invn_query_header;
    }

    @Override
    public void initInjector() {

    }

    public void initVariable(Bundle savedInstanceState) {
        mWorks = new ArrayList<>();
        mInvs = new ArrayList<>();
        mRefData = null;
    }

    @Override
    public void initEvent() {
        RxView.clicks(mBtnQuery)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(a -> startQueryInvInfo());
    }

    @Override
    public void initData() {
        mPresenter.getWorks(0);
    }

    /**
     * 开始查询库存信息
     */
    private void startQueryInvInfo() {
        if (spWork.getSelectedItemPosition() <= 0) {
            showMessage("请先选择工厂");
            return;
        }

        if (spInv.getSelectedItemPosition() <= 0) {
            showMessage("请先选择库存地点");
            return;
        }

        String workId = mWorks.get(spWork.getSelectedItemPosition()).workId;
        String workCode = mWorks.get(spWork.getSelectedItemPosition()).workCode;
        String invId = mInvs.get(spInv.getSelectedItemPosition()).invCode;
        String invCode = mInvs.get(spInv.getSelectedItemPosition()).invId;
        String materialClass = getString(etMaterialClass);
        String materialDesc = getString(etMaterialDesc);


    }

    /**
     * 查询完毕
     */
    @Override
    public void queryInvInfoComplete() {

    }

    @Override
    public void showWorks(List<WorkEntity> works) {
        mWorks.clear();
        mWorks.addAll(works);
    }

    @Override
    public void loadWorksFail(String message) {
        showMessage(message);
        mWorks.clear();
        if (mWorkAdapter != null) {
            mWorkAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void loadWorksComplete() {
        //绑定适配器
        if (mWorkAdapter == null) {
            mWorkAdapter = new WorkAdapter(mActivity, R.layout.item_simple_sp, mWorks);
            spWork.setAdapter(mWorkAdapter);
        } else {
            mWorkAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showInvs(List<InvEntity> invs) {
        mInvs.clear();
        mInvs.addAll(invs);

    }

    @Override
    public void loadInvsFail(String message) {
        showMessage(message);
        mInvs.clear();
        if (mInvAdapter != null) {
            mInvAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void loadInvsComplete() {
        if (mInvAdapter == null) {
            mInvAdapter = new InvAdapter(mActivity, R.layout.item_simple_sp, mInvs);
            spInv.setAdapter(mInvAdapter);
        } else {
            mInvAdapter.notifyDataSetChanged();
        }
    }
}
