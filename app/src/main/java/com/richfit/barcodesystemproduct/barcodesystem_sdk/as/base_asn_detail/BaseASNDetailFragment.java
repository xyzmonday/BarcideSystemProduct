package com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_asn_detail;

import android.text.TextUtils;
import android.view.View;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.ASNDetailAdapter;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_asn_detail.imp.ASNDetailPresenterImp;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.base.base_detail.BaseDetailFragment;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.BottomMenuEntity;
import com.richfit.domain.bean.RefDetailEntity;

import java.util.ArrayList;
import java.util.List;

import static com.richfit.common_lib.utils.SPrefUtil.getData;

/**
 * Created by monday on 2016/11/27.
 */

public abstract class BaseASNDetailFragment extends BaseDetailFragment<ASNDetailPresenterImp, RefDetailEntity>
        implements IASNDetailView<RefDetailEntity> {

    @Override
    protected int getContentId() {
        return R.layout.fragment_asn_detail;
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
            mAdapter = new ASNDetailAdapter(mActivity, R.layout.item_asn_parent_item, nodes);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemEditAndDeleteListener(this);
            mAdapter.setAdapterStateListener(this);
        } else {
            mAdapter.addAll(nodes);
        }
    }

    @Override
    public void refreshComplete() {

    }

    /**
     * 修改明细里面的子节点
     *
     * @param node
     * @param position
     */
    @Override
    public void editNode(final RefDetailEntity node, int position) {
        String state = (String) getData(mBizType, "0");
        if (!"0".equals(state)) {
            showMessage("已经过账,不允许修改");
            return;
        }
        //获取与该子节点的物料编码和发出库位一致的发出仓位和接收仓位列表
        if (mAdapter != null && ASNDetailAdapter.class.isInstance(mAdapter)) {
            ASNDetailAdapter adapter = (ASNDetailAdapter) mAdapter;
            ArrayList<String> Locations = adapter.getLocations(position, 0);
            mPresenter.editNode(Locations, null, null,node, mCompanyCode, mBizType, mRefType,
                    "其他入库-无参考", position);
        }
    }


    @Override
    public void deleteNode(final RefDetailEntity node, int position) {
        String state = (String) getData(mBizType, "0");
        if (!"0".equals(state)) {
            showMessage("已经过账,不允许删除");
            return;
        }
        if (TextUtils.isEmpty(node.transLineId)) {
            showMessage("该行还未进行数据采集");
            return;
        }
        mPresenter.deleteNode("N", node.transId, node.transLineId, node.locationId, mRefData.refType,
                mRefData.bizType, position, mCompanyCode);
    }

    /**
     * 重写删除成功的回调，因为父类实现实现的有参考的逻辑
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
    @Override
    protected void submit2BarcodeSystem(String transToSapFlag) {
        String transferFlag = (String) getData(mBizType, "0");
        if ("1".equals(transferFlag)) {
            showMessage(getString(R.string.detail_on_location));
            return;
        }
        mPresenter.submitData2BarcodeSystem(mTransId, mBizType, mRefType, mRefData.voucherDate,
                mRefData.voucherDate,transToSapFlag,null);
    }

    /**
     * 过账成功显示物料凭证
     */
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
     * 2.上架处理
     */
    @Override
    protected void submit2SAP(String tranToSapFlag) {
        if (TextUtils.isEmpty(mTransNum)) {
            showMessage("请先过账");
            return;
        }
        mPresenter.submitData2SAP(mTransId, mBizType, mRefType, Global.USER_ID,
                mRefData.voucherDate, "", null);
    }

    /**
     * 上架成功后跳转到抬头屏幕
     */
    @Override
    public void submitSAPSuccess() {
        setRefreshing(false, "数据上传成功");
        if (mAdapter != null) {
            mAdapter.removeAllVisibleNodes();
        }
        mRefData = null;
        mTransNum = "";
        mTransId = "";
        mPresenter.showHeadFragmentByPosition(BaseFragment.HEADER_FRAGMENT_INDEX);
    }

    /**
     * 上架失败
     *
     * @param messages
     */
    @Override
    public void submitSAPFail(String[] messages) {
        showErrorDialog(messages);
    }

    /**
     * 这里重写的目的是只显示两个按钮
     *
     * @return
     */
    @Override
    public List<BottomMenuEntity> provideDefaultBottomMenu() {
        List<BottomMenuEntity> menus = super.provideDefaultBottomMenu();
        return menus.subList(0, 2);
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
        String state = (String) getData(mBizType, "0");
        if (!"0".equals(state)) {
            showMessage(getString(R.string.detail_on_location));
            return false;
        }
        return true;
    }
}
