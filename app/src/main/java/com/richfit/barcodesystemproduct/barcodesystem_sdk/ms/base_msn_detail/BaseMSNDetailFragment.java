package com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_detail;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.MSNDetailAdapter;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.base.base_detail.BaseDetailFragment;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.domain.bean.RefDetailEntity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by monday on 2016/11/20.
 */

public abstract class BaseMSNDetailFragment<P extends INMSDetailPresenter> extends BaseDetailFragment<P, RefDetailEntity>
        implements INMSDetailView<RefDetailEntity> {

    /*移库无参考的公共组件*/

    /*发出库位*/
    @BindView(R.id.sendInv)
    protected TextView sendInv;
    /*发出仓位*/
    @BindView(R.id.sendLoc)
    protected TextView sendLoc;
    /*发出批次*/
    @BindView(R.id.sendBatchFlag)
    protected TextView sendBatchFlag;
    /*接收仓位*/
    @BindView(R.id.recLoc)
    protected TextView recLoc;
    /*接收批次*/
    @BindView(R.id.recBatchFlag)
    protected TextView recBatchFlag;

    /*处理寄售转自有业务。主要的逻辑是用户点击过账按钮之后系统自动检查该缓存(子节点)中是否有特殊库存标识是否
    * 等于K而且特殊库存编号不为空。如果满足以上的条件，那么系统自动调用转自有的接口。如果转自有成功修改成员变量
    * isTurnSuccess为true。如果业务在上传数据的时候有第二步，那么需要检查该字段*/
    /*是否需要寄售转自有*/
    protected boolean isNeedTurn = false;
    /*转自有是否成功*/
    protected boolean isTurnSuccess = false;


    @Override
    protected int getContentId() {
        return R.layout.fragment_msn_detail;
    }

    @Override
    public void initDataLazily() {
        isNeedTurn = false;
        isTurnSuccess = false;
        startAutoRefresh();
    }

    /**
     * 显示标准无参考移库明细界面
     *
     * @param allNodes
     */
    @Override
    public void showNodes(List<RefDetailEntity> allNodes) {
        saveTransId(allNodes);
        saveTurnFlag(allNodes);
        if (mAdapter == null) {
            mAdapter = new MSNDetailAdapter(mActivity, R.layout.item_msn_detail_item, allNodes, null, null);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemEditAndDeleteListener(this);
            mAdapter.setAdapterStateListener(this);
        } else {
            mAdapter.addAll(allNodes);
        }
    }

    /**
     * 刷新界面结束。注意如果用户切换界面(修改仓位等),那么系统不再自动过账
     */
    @Override
    public void refreshComplete() {
        if (!isNeedTurn && isTurnSuccess) {
            //如果寄售转自有成功后，系统自动去过账。
            submit2BarcodeSystem(mBottomMenus.get(0).transToSapFlag);
        }
    }

    protected void saveTransId(List<RefDetailEntity> allNodes) {
        for (RefDetailEntity node : allNodes) {
            if (!TextUtils.isEmpty(node.transId)) {
                mTransId = node.transId;
                break;
            }
        }
    }

    /**
     * 获取是否该明细是否需要转自有。
     *
     * @param nodes
     */
    private void saveTurnFlag(final List<RefDetailEntity> nodes) {
        //仅仅检查父节点
        for (RefDetailEntity node : nodes) {
            if (Global.PARENT_NODE_ITEM_TYPE == node.getViewType() &&
                    "K".equalsIgnoreCase(node.specialInvFlag) &&
                    !isEmpty(node.specialInvNum)) {
                isNeedTurn = true;
                break;
            }
        }
    }

    /**
     * 修改明细节点，注意无参考移库不具有父子节点的结构
     *
     * @param node
     * @param position
     */
    @Override
    public void editNode(final RefDetailEntity node, int position) {
        String state = (String) SPrefUtil.getData(mBizType, "0");
        if (!"0".equals(state)) {
            showMessage("已经过账,不允许删除");
            return;
        }
        //获取与该子节点的物料编码和发出库位一致的发出仓位和接收仓位列表
        if (mAdapter != null && MSNDetailAdapter.class.isInstance(mAdapter)) {
            MSNDetailAdapter adapter = (MSNDetailAdapter) mAdapter;
            ArrayList<String> sendLocations = adapter.getLocations(node.materialNum, node.invId, position, 0);
            ArrayList<String> recLocations = adapter.getLocations(node.materialNum, node.invId, position, 1);
            mPresenter.editNode(sendLocations, recLocations, null, node, mCompanyCode,
                    mBizType, mRefType, getSubFunName(), position);
        }
    }

    /**
     * 删除节点
     *
     * @param node
     * @param position
     */
    @Override
    public void deleteNode(final RefDetailEntity node, int position) {
        String state = (String) SPrefUtil.getData(mBizType, "0");
        if (!"0".equals(state)) {
            showMessage("已经过账,不允许删除");
            return;
        }
        if (TextUtils.isEmpty(node.transLineId)) {
            showMessage("该行还未进行数据采集");
            return;
        }
        mPresenter.deleteNode("N", node.transId, node.transLineId, node.locationId,
                mRefData.refType, mRefData.bizType, position, mCompanyCode);
    }

    /**
     * 删除成功后回到该方法，注意这里是无参考明细界面直接将该节点移除。
     *
     * @param position：节点在明细列表的位置
     */
    @Override
    public void deleteNodeSuccess(int position) {
        showMessage("删除成功");
        if (mAdapter != null) {
            mAdapter.removeItemByPosition(position);
            int itemCount = mAdapter.getItemCount();
            if (itemCount == 0) {
                mExtraContainer.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void deleteNodeFail(String message) {
        showMessage(message);
    }

    @Override
    public boolean checkDataBeforeOperationOnDetail() {
        if (mRefData == null) {
            showMessage("请先获取单据数据");
            return false;
        }
        if (TextUtils.isEmpty(mTransId)) {
            showMessage("未获取缓存标识");
            return false;
        }

        if (TextUtils.isEmpty(mRefData.voucherDate)) {
            showMessage("请先选择过账日期");
            return false;
        }
        return true;
    }

    /**
     * 1.过账
     */
    protected void submit2BarcodeSystem(String transToSapFlag) {
        //如果需要寄售转自有但是没有成功过，都需要用户需要再次寄售转自有

        String transferFlag = (String) SPrefUtil.getData(mBizType, "0");
        if ("1".equals(transferFlag)) {
            showMessage("本次采集已经过账,请先进行其他转储操作");
            return;
        }
        mFlagMap.clear();
        mFlagMap.put("transToSapFlag", transToSapFlag);
        mPresenter.submitData2BarcodeSystem(mTransId, mRefData.bizType, mRefType, Global.USER_ID,
                mRefData.voucherDate, mFlagMap, createExtraHeaderMap());
    }

    /**
     * 这里由于311移库需要增加是否寄售转自有的取消功能，所以
     * 将该功能交给子类根据具体的业务重写
     */
    protected void showTurnConfirmDialog() {
        if (isNeedTurn && !isTurnSuccess) {
            new SweetAlertDialog(mActivity).setTitleText("温馨提示")
                    .setContentText("您需要先寄售转自有，请点击确定。").setConfirmText("确定")
                    .setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismiss();
                        startTurnOwnSupplies("07");
                    }).show();
            return;
        }
    }

    /**
     * 第一步过账成功后显示物料凭证
     */
    @Override
    public void submitBarcodeSystemSuccess() {
        showSuccessDialog(mTransNum);
    }

    /**
     * 第一步过账失败后清除物料凭证，显示错误信息
     *
     * @param message
     */
    @Override
    public void submitBarcodeSystemFail(String message) {
        mTransNum = "";
        showErrorDialog(TextUtils.isEmpty(message) ? "过账失败" : message);
    }

    /**
     * 2.数据上传
     */
    protected void submit2SAP(String transToSapFlag) {
        if (TextUtils.isEmpty(mTransNum)) {
            showMessage("请先过账");
            return;
        }
        mFlagMap.clear();
        mFlagMap.put("transToSapFlag", transToSapFlag);
        mPresenter.submitData2SAP(mTransId, mRefData.bizType, mRefType, Global.USER_ID,
                mRefData.voucherDate, mFlagMap, createExtraHeaderMap());
    }


    /**
     * 第二步转储成功后跳转到抬头界面
     */
    @Override
    public void submitSAPSuccess() {
        setRefreshing(false, "转储成功");
        showSuccessDialog(mInspectionNum);
        if (mAdapter != null) {
            mAdapter.removeAllVisibleNodes();
        }
        mRefData = null;
        mTransNum = "";
        mTransId = "";
        mPresenter.showHeadFragmentByPosition(BaseFragment.HEADER_FRAGMENT_INDEX);
    }

    @Override
    public void submitSAPFail(String[] messages) {
        mInspectionNum = "";
        showErrorDialog(messages);
    }

    @Override
    protected void sapUpAndDownLocation(String transToSapFlag) {

    }

    @Override
    public void upAndDownLocationFail(String[] messages) {

    }

    @Override
    public void upAndDownLocationSuccess() {

    }

    /**
     * 开始寄售转自有
     *
     * @param transToSapFlag
     */
    protected void startTurnOwnSupplies(String transToSapFlag) {
        if (isEmpty(mTransId)) {
            showMessage("未获取到缓存,请先获取采集数据");
            return;
        }

        mFlagMap.clear();
        mFlagMap.put("transToSapFlag", transToSapFlag);
        mInspectionNum = "";
        mPresenter.turnOwnSupplies(mTransId, mRefData.bizType, mRefType, Global.USER_ID,
                mRefData.voucherDate, mFlagMap, createExtraHeaderMap(), -1);
    }

    /**
     * 寄售转自有成功
     */
    @Override
    public void turnOwnSuppliesSuccess() {
        isTurnSuccess = true;
        isNeedTurn = false;
        startAutoRefresh();
    }

    /**
     * 寄售转自有失败
     */
    @Override
    public void turnOwnSuppliesFail(String message) {
        showErrorDialog(TextUtils.isEmpty(message) ? "寄售转自有失败" : message);
        isTurnSuccess = false;
        isNeedTurn = true;
    }

    /*子类返回修改模块的名称*/
    protected abstract String getSubFunName();
}
