package com.richfit.barcodesystemproduct.module_location_process.location_qt.detail;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.LocQTDetailAdapter;
import com.richfit.barcodesystemproduct.base.base_detail.BaseDetailFragment;
import com.richfit.barcodesystemproduct.module_location_process.location_qt.detail.imp.LocQTPresenterImp;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.TreeNode;

import java.util.List;

import static com.richfit.common_lib.utils.SPrefUtil.getData;

/**
 * Created by monday on 2017/5/25.
 */

public class LocQTDetailFragment extends BaseDetailFragment<LocQTPresenterImp, RefDetailEntity> {

    @Override
    protected int getContentId() {
        return R.layout.fragment_locqt_detail;
    }

    @Override
    public void initInjector() {

    }

    @Override
    public void showNodes(List<RefDetailEntity> allNodes) {
        saveTransId(allNodes);
        if (mAdapter == null) {
            mAdapter = new LocQTDetailAdapter(mActivity, allNodes);
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
    public void editNode(RefDetailEntity node, int position) {
        String state = (String) getData(mBizType + mRefType, "0");
        if (!"0".equals(state)) {
            showMessage("已经过账,不允许修改");
            return;
        }
        mPresenter.editNode(null, null, mRefData, node, mCompanyCode, mBizType,
                mRefType, "上下架处理", -1);
    }

    @Override
    public void deleteNode(RefDetailEntity node, int position) {
        String state = (String) getData(mBizType, "0");
        if (!"0".equals(state)) {
            showMessage("已经过账,不允许删除");
            return;
        }
        TreeNode parentNode = node.getParent();
        String lineDeleteFlag;
        if (parentNode == null) {
            lineDeleteFlag = "N";
        } else {
            lineDeleteFlag = parentNode.getChildren().size() > 1 ? "N" : "Y";
        }
        mPresenter.deleteNode(lineDeleteFlag, node.transId, node.transLineId,
                node.locationId, mRefData.refType, mRefData.bizType, position, mCompanyCode);
    }

    @Override
    public void deleteNodeSuccess(int position) {
        showMessage("删除成功");
        if (mAdapter != null) {
            mAdapter.removeNodeByPosition(position);
        }
        startAutoRefresh();
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
        return true;
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
