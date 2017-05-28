package com.richfit.barcodesystemproduct.module_approval.xinanGD_ao.detail;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.XiNanGDAODetailAdaper;
import com.richfit.barcodesystemproduct.base.base_detail.BaseDetailFragment;
import com.richfit.barcodesystemproduct.module_approval.xinanGD_ao.detail.imp.IXiNanGDAODetailPresenterImp;
import com.richfit.domain.bean.RefDetailEntity;

import java.util.List;

/**
 * Created by monday on 2017/5/26.
 */

public class XiNanGDDetailFragment extends BaseDetailFragment<IXiNanGDAODetailPresenterImp, RefDetailEntity>
        implements IXiNanGDAODetailView {

    @Override
    protected int getContentId() {
        return R.layout.fragment_xinangd_ao_detail;
    }

    @Override
    public void initInjector() {

    }

    @Override
    public void deleteNode(RefDetailEntity node, int position) {

    }

    @Override
    public void editNode(RefDetailEntity node, int position) {

    }

    @Override
    public void showNodes(List<RefDetailEntity> allNodes) {
        saveTransId(allNodes);
        if (mAdapter == null) {
            mAdapter = new XiNanGDAODetailAdaper(mActivity, allNodes);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemEditAndDeleteListener(this);
            mAdapter.setAdapterStateListener(this);
        } else {
            mAdapter.addAll(allNodes);
        }
    }

    @Override
    public void refreshComplete() {

    }

    @Override
    public void deleteNodeSuccess(int position) {

    }

    @Override
    public void submitBarcodeSystemSuccess() {

    }

    @Override
    public void submitBarcodeSystemFail(String message) {

    }

    @Override
    public void submitSAPSuccess() {

    }

    @Override
    public void submitSAPFail(String[] messages) {

    }

    @Override
    public void upAndDownLocationFail(String[] messages) {

    }

    @Override
    public void upAndDownLocationSuccess() {

    }

    @Override
    protected boolean checkTransStateBeforeRefresh() {
        return false;
    }

    @Override
    protected void submit2BarcodeSystem(String tranToSapFlag) {

    }

    @Override
    protected void submit2SAP(String tranToSapFlag) {

    }

    @Override
    protected void sapUpAndDownLocation(String transToSapFlag) {

    }


}
