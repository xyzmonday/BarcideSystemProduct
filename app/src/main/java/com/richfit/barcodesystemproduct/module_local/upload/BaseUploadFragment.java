package com.richfit.barcodesystemproduct.module_local.upload;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.ShowUploadDataAdapter;
import com.richfit.barcodesystemproduct.base.base_detail.BaseDetailFragment;
import com.richfit.common_lib.adapter.animation.Animation.animators.FadeInDownAnimator;
import com.richfit.common_lib.adapter.animation.StickyDividerDecoration;
import com.richfit.common_lib.dialog.UploadFragmentDialog;
import com.richfit.common_lib.utils.L;
import com.richfit.domain.bean.ResultEntity;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据上传页面基类
 * Created by monday on 2017/4/21.
 */

public abstract class BaseUploadFragment extends BaseDetailFragment<UploadPresenterImp, ResultEntity>
        implements UploadContract.View {

    private static final String UPLOAD_INFO_FRAGMENT_DIALOG = "upload_info_fragment_dialog";
    /*公共数据*/
    protected List<ResultEntity> mDatas;
    protected ShowUploadDataAdapter mShowUploadDataAdapter;
    protected UploadFragmentDialog mUploadDialog;

    @Override
    public int getContentId() {
        return R.layout.fragment_upload;
    }

    protected void initVariable(@Nullable Bundle savedInstanceState) {
        mDatas = new ArrayList<>();
    }

    /**
     * 注意这里重写该方法的目的是跳过读取配置信息
     */
    @Override
    protected void initView() {
        // 刷新时，指示器旋转后变化的颜色
        mSwipeRefreshLayout.setColorSchemeResources(R.color.blue_a700, R.color.red_a400,
                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        //设置RecyclerView
        LinearLayoutManager lm = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(lm);
        mRecyclerView.setItemAnimator(new FadeInDownAnimator());
        //初始化是适配器
        mShowUploadDataAdapter = new ShowUploadDataAdapter(mActivity, R.layout.item_local_data, mDatas);
        mRecyclerView.setAdapter(mShowUploadDataAdapter);
        StickyRecyclerHeadersDecoration stickHeader = new StickyRecyclerHeadersDecoration(mShowUploadDataAdapter);
        mRecyclerView.addItemDecoration(stickHeader);
        mRecyclerView.addItemDecoration(new StickyDividerDecoration(mActivity));
    }


    /**
     * 判断有参考情况下，抬头需要采集的基本字段信息。
     * 子类如果需要检测更多的字段应该重写该方法。
     */
    @Override
    public void initDataLazily() {
        //开始刷新
        startAutoRefresh();
    }

    /**
     * 响应自动下拉刷新动作，并且开始请求接口获取整单缓存
     */
    @Override
    public void onRefresh() {
        mPresenter.readUploadData();
    }

    /**
     * 显示需要上传的明细数据
     *
     * @param results
     */
    @Override
    public void showUploadData(ArrayList<ResultEntity> results) {
        L.e("showUploadData");
        if (results == null || results.size() == 0)
            return;
        mDatas.addAll(results);
        mShowUploadDataAdapter.notifyDataSetChanged();
    }

    /**
     * 读取需要上传的数据失败
     *
     * @param message
     */
    @Override
    public void readUploadDataFail(String message) {
        //不论成功或者失败都应该关闭下拉加载动画
        showMessage(message);
        mSwipeRefreshLayout.setRefreshing(false);
        mExtraContainer.setVisibility(View.GONE);
        if (mShowUploadDataAdapter != null) {
            mDatas.clear();
            mShowUploadDataAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 读取数据成功，而且刷新UI界面完毕
     */
    @Override
    public void readUploadDataComplete() {
        L.e("readUploadDataComplete");
        showMessage("读取离线数据完成!");
        mExtraContainer.setVisibility(View.VISIBLE);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * 开始数据上传回到
     *
     * @param totalUploadDataNum
     */
    @Override
    public void startUploadData(int totalUploadDataNum) {
        AppCompatActivity appCompatActivity = (AppCompatActivity) mActivity;
        FragmentManager fragmentManager = appCompatActivity.getSupportFragmentManager();
        mUploadDialog = (UploadFragmentDialog) fragmentManager.findFragmentByTag(UPLOAD_INFO_FRAGMENT_DIALOG);
        if (mUploadDialog == null) {
            mUploadDialog = UploadFragmentDialog.newInstance("您有" + String.valueOf(totalUploadDataNum) + "笔单数据正在上传...");
        }
        if (!mUploadDialog.isAdded())
            mUploadDialog.show(fragmentManager, UPLOAD_INFO_FRAGMENT_DIALOG);
    }

    /**
     * 每一单数据上传成功
     *
     * @param taskNum
     * @param message
     * @param transNum
     */
    @Override
    public void uploadCollectDataSuccess(int taskNum, int totalNum, String message, String transNum) {
        if (mUploadDialog != null && mUploadDialog.isVisible()) {
            mUploadDialog.addMessage(String.valueOf(taskNum + 1) + "/" + String.valueOf(totalNum) + ":"
                    + message + "\n" + transNum);
        }
    }

    /**
     * 数据上传失败
     *
     * @param taskNum
     * @param message
     */
    @Override
    public void uploadCollectDataFail(int taskNum, int totalNum, String message) {
        if (mUploadDialog != null && mUploadDialog.isVisible()) {
            mUploadDialog.addMessage(String.valueOf(taskNum + 1) + "/" + String.valueOf(totalNum) + ":" + message);
        }
    }

    /**
     * 数据上传完成
     */
    @Override
    public void uploadCollectDataComplete() {
        showMessage("数据上传成功!!!");
        mExtraContainer.setVisibility(View.GONE);
        if (mShowUploadDataAdapter != null) {
            mDatas.clear();
            mShowUploadDataAdapter.notifyDataSetChanged();
        }
        if (mUploadDialog != null) {
            mUploadDialog.dismiss();
            mUploadDialog = null;
        }
        mPresenter.resetStateAfterUpload();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDatas != null) {
            mDatas.clear();
            mDatas = null;
        }
        if (mUploadDialog != null) {
            mUploadDialog.dismiss();
            mUploadDialog = null;
        }
    }

    @Override
    public void deleteNode(ResultEntity node, int position) {

    }

    @Override
    public void editNode(ResultEntity node, int position) {

    }

    @Override
    public void showNodes(List<ResultEntity> allNodes) {

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
