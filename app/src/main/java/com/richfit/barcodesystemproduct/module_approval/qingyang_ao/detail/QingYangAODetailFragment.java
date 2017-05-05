package com.richfit.barcodesystemproduct.module_approval.qingyang_ao.detail;

import android.text.TextUtils;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.QingYangAOAdapter;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.base.base_detail.BaseDetailFragment;
import com.richfit.barcodesystemproduct.module_approval.qingyang_ao.detail.imp.ApprovalOtherDetailPresenterImp;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.RefDetailEntity;

import java.util.List;

/**
 * Created by monday on 2016/11/24.
 */

public class QingYangAODetailFragment extends BaseDetailFragment<ApprovalOtherDetailPresenterImp, RefDetailEntity>
        implements IApprovalOtherDetailView<RefDetailEntity> {

    @Override
    protected int getContentId() {
        return R.layout.fragment_qingyang_ao_detail;
    }

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    public void initDataLazily() {
        if (mRefData == null) {
            showMessage("请现在抬头界面获取单据数据");
            return;
        }

        if (TextUtils.isEmpty(mRefData.recordNum)) {
            showMessage("请现在抬头界面输入验收单号");
            return;
        }

        if (TextUtils.isEmpty(mRefData.bizType)) {
            showMessage("未获取到业务类型");
            return;
        }

        startAutoRefresh();
    }

    @Override
    public void showNodes(List<RefDetailEntity> nodes) {
        if (mAdapter == null) {
            mAdapter = new QingYangAOAdapter(mActivity, R.layout.item_qingyang_ao_item, nodes);
            mAdapter.setOnItemEditAndDeleteListener(this);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.addAll(nodes);
        }
    }

    @Override
    public void refreshComplete() {

    }

    /**
     * 自动下拉刷新
     */
    @Override
    public void onRefresh() {
        //单号
        final String recordNum = mRefData.recordNum;
        //业务类型
        final String bizType = mRefData.bizType;
        //单据类型
        final String refType = mRefData.refType;
        //移动类型
        final String moveType = mRefData.moveType;
        //获取缓存累计数量缓存
        mPresenter.getReference(mRefData, recordNum, refType, bizType, moveType, "", Global.USER_ID);
    }

    /**
     * 重写节点修改方法，因为这里通过不同的字段判断是否可以对该节点进行修改
     *
     * @param node
     * @param position
     */
    @Override
    public void editNode(final RefDetailEntity node, int position) {
        if ("Y".equals(node.lineInspectFlag)) {
            showMessage("该行已经过账,不允许编辑");
            return;
        }
        if (TextUtils.isEmpty(node.totalQuantity) || "0".equals(node.totalQuantity)) {
            showMessage("该行还未进行数据采集!");
            return;
        }
        mPresenter.editNode(null, null, null, node, mCompanyCode, mBizType, mRefType, "验收结果修改", position);
    }

    /**
     * 调用接口删除单条数据，删除成功后修改本地内存数据，并刷新
     *
     * @param node
     * @param position
     */
    @Override
    public void deleteNode(final RefDetailEntity node, int position) {
        if ("Y".equals(node.lineInspectFlag)) {
            showMessage("该行已经过账,不允许删除");
            return;
        }
        if (TextUtils.isEmpty(node.totalQuantity) || "0".equals(node.totalQuantity)) {
            showMessage("该行还未进行数据采集!");
            return;
        }
        mPresenter.deleteNode("N", mRefData.recordNum, node.lineNum, node.refLineId,
                mRefData.refType, mRefData.bizType, Global.USER_ID, position, mCompanyCode);
    }

    @Override
    public void deleteNodeSuccess(int position) {
        showMessage("删除成功");
        if (mAdapter != null) {
            mAdapter.removeNodeByPosition(position);
        }
    }

    @Override
    public boolean checkDataBeforeOperationOnDetail() {
        if (mRefData == null) {
            showMessage("请先获取验收清单");
            return false;
        }
        if (TextUtils.isEmpty(mRefData.refCodeId)) {
            showMessage("请先在抬头界面获取单据数据");
            return false;
        }

        if (TextUtils.isEmpty(mBizType)) {
            showMessage("未获取到业务类型");
            return false;
        }

        if (TextUtils.isEmpty(mRefType)) {
            showMessage("未获取到单据类型");
            return false;
        }

        if (mRefData.billDetailList == null || mRefData.billDetailList.size() == 0) {
            showMessage("该验收清单没有明细数据,不需要过账");
            return false;
        }
        return true;
    }

    /**
     * 显示过账，数据上传等菜单对话框。
     *
     * @param companyCode
     */
    @Override
    public void showOperationMenuOnDetail(final String companyCode) {
        android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(mActivity);
        dialog.setTitle("提示");
        dialog.setMessage("您真的需要过账该张验收单据吗?");
        dialog.setPositiveButton("确定", (dialogInterface, i) -> {
            submit2BarcodeSystem("");
            dialogInterface.dismiss();
        });
        dialog.setNegativeButton("取消", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });
        dialog.show();
    }

    public void submitBarcodeSystemSuccess() {
        setRefreshing(false, "数据上传成功");
        showSuccessDialog(mTransNum);
        if (mAdapter != null) {
            mAdapter.removeAllVisibleNodes();
        }
        //注意这里必须清除单据数据
        mRefData = null;
        mTransId = "";
        mTransNum = "";
        mPresenter.showHeadFragmentByPosition(BaseFragment.HEADER_FRAGMENT_INDEX);
    }

    @Override
    public void submitBarcodeSystemFail(String message) {
        mTransNum = "";
        showErrorDialog(TextUtils.isEmpty(message) ? "过账失败" : message);
    }


    @Override
    protected boolean checkTransStateBeforeRefresh() {
        return true;
    }


    @Override
    protected void submit2BarcodeSystem(String tranToSapFlag) {
        mPresenter.uploadCollectionData(mRefData.recordNum, mRefData.refCodeId,
                mBizType, mRefType, mRefData.inspectionType, mRefData.voucherDate, Global.USER_ID, false);
    }

    @Override
    protected void submit2SAP(String tranToSapFlag) {

    }

    @Override
    protected void sapUpAndDownLocation(String transToSapFlag) {

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

}
