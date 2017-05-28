package com.richfit.barcodesystemproduct.module_infoquery.inventory_query_y.header;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.InvYQueryRefDetailAdapter;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.module_infoquery.inventory_query_y.header.imp.InvYQueryHeaderPresenterImp;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.widget.RichEditText;
import com.richfit.domain.bean.ReferenceEntity;

import butterknife.BindView;

/**
 * Created by monday on 2017/5/25.
 */

public class InvYQueryHeaderFragment extends BaseFragment<InvYQueryHeaderPresenterImp>
        implements IInvYQueryHeaderView {

    private static final String MOVE_TYPE = "1";

    @BindView(R.id.et_ref_num)
    RichEditText etRefNum;
    //单据明细
    @BindView(R.id.base_detail_recycler_view)
    RecyclerView mRecyclerView;
    InvYQueryRefDetailAdapter mAdapter;

    @Override
    public void handleBarCodeScanResult(String type, String[] list) {
        if (list != null && list.length == 1) {
            getRefData(list[0]);
        }
    }

    @Override
    protected int getContentId() {
        return R.layout.fragment_invy_query_header;
    }

    @Override
    public void initInjector() {

    }

    @Override
    public void initVariable(Bundle savedInstanceState) {
        mRefData = null;
    }

    protected void getRefData(String refNum) {
        mRefData = null;
        clearAllUI();
        mPresenter.getReference(refNum, mRefType, mBizType, MOVE_TYPE, "", Global.USER_ID);
    }

    @Override
    public void getReferenceSuccess(ReferenceEntity refData) {
        refData.bizType = mBizType;
        refData.moveType = MOVE_TYPE;
        refData.refType = mRefType;
        mRefData = refData;
    }

    @Override
    public void getReferenceFail(String message) {
        showMessage(message);
        mRefData = null;
        //清除所有控件绑定的数据
        clearAllUI();
    }

    @Override
    public void getReferenceComplete() {
        if (mAdapter != null) {
            mAdapter = new InvYQueryRefDetailAdapter(mActivity, R.layout.item_invy_query_ref,
                    mRefData.billDetailList);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void clearAllUI() {
        clearCommonUI(etRefNum);
        if (mAdapter != null) {
            mAdapter.removeAllVisibleNodes();
        }
    }

    @Override
    public void retry(String action) {
        switch (action) {
            case Global.RETRY_LOAD_REFERENCE_ACTION:
                mPresenter.getReference(getString(etRefNum), mRefType, mBizType, MOVE_TYPE, "", Global.LOGIN_ID);
                break;
        }
        super.retry(action);
    }

}
