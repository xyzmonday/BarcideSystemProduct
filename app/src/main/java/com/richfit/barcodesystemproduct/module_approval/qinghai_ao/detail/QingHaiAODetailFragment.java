package com.richfit.barcodesystemproduct.module_approval.qinghai_ao.detail;

import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.QingHaiAODetailAdapter;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.base.base_detail.BaseDetailFragment;
import com.richfit.barcodesystemproduct.module_approval.qinghai_ao.detail.imp.QingHaiAODetailPresenterImp;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.RefDetailEntity;

import java.util.List;

/**
 * Created by monday on 2017/2/28.
 */

public class QingHaiAODetailFragment extends BaseDetailFragment<QingHaiAODetailPresenterImp, RefDetailEntity>
        implements IQingHaiAODetailView<RefDetailEntity> {


    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected int getContentId() {
        return R.layout.fragment_qinghai_ao_detail;
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

    /**
     * 响应自动下拉刷新动作，并且开始请求接口获取整单缓存
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
        //清除缓存标识
        mTransId = "";
        //清除过账凭证
        mTransNum = "";
        //获取缓存累计数量缓存
        mPresenter.getReference(mRefData, recordNum, refType, bizType, moveType, "", Global.USER_ID);
    }

    @Override
    public void showNodes(List<RefDetailEntity> allNodes) {

    }

    /**
     * 注意这里我们需要获取到抬头的缓存标识。
     *
     * @param nodes
     * @param transId
     */
    @Override
    public void showNodes(List<RefDetailEntity> nodes, String transId) {
        mTransId = transId;
        if (mAdapter == null) {
            mAdapter = new QingHaiAODetailAdapter(mActivity, R.layout.item_qinghai_ao_item, nodes);
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
     * 修改明细节点
     *
     * @param node
     * @param position
     */
    @Override
    public void editNode(final RefDetailEntity node, int position) {
        if (TextUtils.isEmpty(node.transLineId) || TextUtils.isEmpty(node.totalQuantity) ||
                "0".equals(node.totalQuantity)) {
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
        if (TextUtils.isEmpty(node.transLineId) || TextUtils.isEmpty(node.totalQuantity) ||
                "0".equals(node.totalQuantity)) {
            showMessage("该行还未进行数据采集!");
            return;
        }
        mPresenter.deleteNode("N", mRefData.recordNum, node.lineNum, node.refLineId,
                mRefData.refType, mRefData.bizType, Global.USER_ID, position, mCompanyCode,mPresenter.isLocal());
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
     * 显示过账，数据上传等菜单对话框
     *
     * @param companyCode
     */
    @Override
    public void showOperationMenuOnDetail(final String companyCode) {
        if (mPresenter.isLocal()) {
            //如果是离线
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle("温馨提示")
                    .setMessage("您是否要结束本次操作?")
                    .setPositiveButton("结束本次操作", (dialog, which) -> {
                        dialog.dismiss();
                        mPresenter.setTransFlag(mBizType,mTransId, "2");
                    }).setNegativeButton("取消", (dialog, which) -> dialog.dismiss()).show();
        } else {
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
    }


    @Override
    protected void submit2BarcodeSystem(String transToSapFlag) {
        if (TextUtils.isEmpty(mTransId)) {
            showMessage("请先采集数据");
            return;
        }
        mTransNum = "";
        mPresenter.transferCollectionData(mRefData.recordNum, mRefData.refCodeId, mTransId, mBizType,
                mRefType, mRefData.inspectionType, Global.USER_ID, false,
                mRefData.voucherDate, transToSapFlag, null);
    }

    @Override
    public void submitBarcodeSystemSuccess() {
        showMessage("上传图片和数据成功");
        if (mAdapter != null) {
            mAdapter.removeAllVisibleNodes();
        }
        showSuccessDialog(mTransNum);
        mTransId = "";
        mTransNum = "";
        mRefData = null;
        mPresenter.showHeadFragmentByPosition(BaseFragment.HEADER_FRAGMENT_INDEX);
    }

    @Override
    public void submitBarcodeSystemFail(String message) {
        mTransNum = "";
        showErrorDialog(TextUtils.isEmpty(message) ? "过账失败" : message);
    }

    /**
     * 因为物资验收只有一步过账所以可以每次进入明细界面都刷新界面
     *
     * @return
     */
    @Override
    protected boolean checkTransStateBeforeRefresh() {
        return true;
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
