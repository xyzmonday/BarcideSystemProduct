package com.richfit.barcodesystemproduct.module_infoquery.inventory_query_y.detail;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.LQDetailAdapter;
import com.richfit.barcodesystemproduct.base.base_detail.BaseDetailFragment;
import com.richfit.barcodesystemproduct.module_infoquery.inventory_query_y.detail.imp.IInvYQueryDetailPresenterImp;
import com.richfit.domain.bean.InventoryEntity;

import java.util.List;

/**
 * 有仓位queryType 04 ；没有仓位 01
 * Created by monday on 2017/3/16.
 */

public class IInvYQueryDetailFragment extends BaseDetailFragment<IInvYQueryDetailPresenterImp,InventoryEntity>
        implements IInvYQueryDetailView {

    private LQDetailAdapter mLQDetailAdapter;

    @Override
    protected int getContentId() {
        return R.layout.fragment_invy_query_detail;
    }

    @Override
    public void initInjector() {

    }

    /**
     * 检查抬头界面的必要的字段是否已经赋值
     */
    @Override
    public void initDataLazily() {
        if (mRefData == null) {
            showMessage("请先在抬头界面获取单据信息");
            return;
        }

        //开始刷新
        startAutoRefresh();
    }


    @Override
    public void onRefresh() {
        String queryType = isEmpty(mRefData.location) ? "01" : "04";
        mPresenter.getInventoryInfo(queryType, "", "", mRefData.workCode,
                mRefData.invCode, "", mRefData.materialNum, "", mRefData.location,
                mRefData.batchFlag, "", "", "1", "");
    }


    @Override
    public void showInventory(List<InventoryEntity> allNodes) {
        if (mLQDetailAdapter == null) {
            mLQDetailAdapter = new LQDetailAdapter(mActivity, R.layout.item_lq_detail, allNodes);
            mRecyclerView.setAdapter(mLQDetailAdapter);
        } else {
            mLQDetailAdapter.addAll(allNodes);
        }
    }

    @Override
    public void loadInventoryFail(String message) {
        showMessage(message);
    }

    @Override
    public void showNodes(List allNodes) {

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


    @Override
    public void deleteNode(InventoryEntity node, int position) {

    }

    @Override
    public void editNode(InventoryEntity node, int position) {

    }
}
