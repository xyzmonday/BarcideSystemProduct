package com.richfit.barcodesystemproduct.barcodesystem_sdk.rs.base_rsn_detail;

import android.text.TextUtils;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.RSNDetailAdapter;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.rs.base_rsn_detail.imp.RSNDetailPresenterImp;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.base.base_detail.BaseDetailFragment;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.domain.bean.BottomMenuEntity;
import com.richfit.domain.bean.RefDetailEntity;

import java.util.ArrayList;
import java.util.List;

import static com.richfit.common_lib.utils.SPrefUtil.getData;

/**
 * 注意这里是标准的无参考退库
 * Created by monday on 2017/3/2.
 */

public abstract class BaseRSNDetailFragment extends BaseDetailFragment<RSNDetailPresenterImp, RefDetailEntity>
        implements IRSNDetailView<RefDetailEntity> {

    @Override
    protected int getContentId() {
        return R.layout.fragment_base_rsn_detail;
    }

    @Override
    public void initDataLazily() {
        if (mRefData == null) {
            setRefreshing(false, "获取明细失败,请现在抬头界面选择相应的参数");
            return;
        }

        if (TextUtils.isEmpty(mRefData.workId)) {
            setRefreshing(false, "获取明细失败,请先选择工厂");
            return;
        }

        if ("46".equals(mBizType) && TextUtils.isEmpty(mRefData.costCenter)) {
            showMessage("请先在抬头界面输入成本中心");
            return;
        }
        if ("47".equals(mBizType) && TextUtils.isEmpty(mRefData.projectNum)) {
            showMessage("请现在抬头界面输入项目编号");
            return;
        }

        startAutoRefresh();
    }


    @Override
    public void showNodes(List<RefDetailEntity> nodes) {
        for (RefDetailEntity node : nodes) {
            if (!TextUtils.isEmpty(node.transId)) {
                mTransId = node.transId;
                break;
            }
        }
        if (mAdapter == null) {
            mAdapter = new RSNDetailAdapter(mActivity, R.layout.item_rsn_detail_item, nodes);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemEditAndDeleteListener(this);
        } else {
            mAdapter.addAll(nodes);
        }
    }

    @Override
    public void refreshComplete() {

    }


    @Override
    public void deleteNode(final RefDetailEntity node, int position) {
        String state = (String) getData(mBizType + mRefType, "0");
        if (!"0".equals(state)) {
            showMessage("已经过账,不允许删除");
            return;
        }
        mPresenter.deleteNode("N", node.transId, node.transLineId, node.locationId, mRefData.refType,
                mRefData.bizType, position, mCompanyCode);
    }

    /**
     * 修改明细里面的子节点
     *
     * @param node
     * @param position
     */
    @Override
    public void editNode(final RefDetailEntity node, int position) {
        String state = (String) getData(mBizType + mRefType, "0");
        if (!"0".equals(state)) {
            showMessage("已经过账,不允许修改");
            return;
        }
        //获取与该子节点的物料编码和发出库位一致的发出仓位和接收仓位列表
        if (mAdapter != null && RSNDetailAdapter.class.isInstance(mAdapter)) {
            RSNDetailAdapter adapter = (RSNDetailAdapter) mAdapter;
            ArrayList<String> Locations = adapter.getLocations(position, 0);
            String subFunName = "46".equals(mBizType) ? "202-退库" : "222-退库";
            mPresenter.editNode(Locations, null, null, node, mCompanyCode, mBizType, mRefType, subFunName, position);
        }
    }

    @Override
    public void deleteNodeSuccess(int position) {
        showMessage("删除成功");
        if (mAdapter != null) {
            mAdapter.removeItemByPosition(position);
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
        String transferFlag = (String) SPrefUtil.getData(mBizType, "0");
        if ("1".equals(transferFlag)) {
            showMessage(getString(R.string.detail_off_location));
            return;
        }
        mExtraTansMap.clear();
        mExtraTansMap.put("centerCost", mRefData.costCenter);
        mExtraTansMap.put("projectNum", mRefData.projectNum);
        mPresenter.submitData2BarcodeSystem(mTransId, mRefData.bizType, mRefType, Global.USER_ID,
                mRefData.voucherDate, transToSapFlag, mExtraTansMap);
    }

    @Override
    public void submitBarcodeSystemSuccess() {
        showSuccessDialog(mTransNum);
    }

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
        mPresenter.submitData2SAP(mTransId, mRefData.bizType, mRefType, Global.USER_ID,
                mRefData.voucherDate, transToSapFlag, null);
    }


    @Override
    public void submitSAPSuccess() {
        setRefreshing(false, "数据上传成功");
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
        showErrorDialog(messages);
        mInspectionNum = "";
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

    @Override
    protected boolean checkTransStateBeforeRefresh() {
        String transferFlag = (String) getData(mBizType + mRefType, "0");
        if ("1".equals(transferFlag)) {
            showMessage(getString(R.string.detail_on_location));
            return false;
        }
        return true;
    }

    @Override
    public List<BottomMenuEntity> provideDefaultBottomMenu() {
        List<BottomMenuEntity> menus = super.provideDefaultBottomMenu();
        menus.get(0).transToSapFlag = "01";
        menus.get(1).transToSapFlag = "05";
        return menus.subList(0, 2);
    }


}


