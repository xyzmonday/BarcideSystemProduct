package com.richfit.barcodesystemproduct.module_local.upload;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.ShowUploadDataAdapter;
import com.richfit.barcodesystemproduct.base.base_detail.BaseDetailFragment;
import com.richfit.barcodesystemproduct.module.main.MainActivity;
import com.richfit.common_lib.adapter.animation.Animation.animators.FadeInDownAnimator;
import com.richfit.common_lib.adapter.animation.StickyDividerDecoration;
import com.richfit.common_lib.dialog.UploadFragmentDialog;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.ResultEntity;
import com.richfit.domain.bean.UploadMsgEntity;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据上传页面基类
 * Created by monday on 2017/4/21.
 */

public abstract class BaseUploadFragment<P extends UploadContract.Presenter> extends BaseDetailFragment<P, ResultEntity>
        implements UploadContract.View, UploadFragmentDialog.OnEditLocalDataListener {

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
     * 读取数据之前，必须情况所有的历史明细数据
     */
    @Override
    public void startReadUploadData() {
        mDatas.clear();
    }

    /**
     * 显示需要上传的明细数据
     *
     * @param results
     */
    @Override
    public void showUploadData(ArrayList<ResultEntity> results) {
        if (results == null || results.size() == 0)
            return;
        //注意這裡是每一單回調一次
        mDatas.addAll(results);
        mShowUploadDataAdapter.notifyItemInserted(mDatas.size());
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
            UploadMsgEntity msgEntity = new UploadMsgEntity();
            msgEntity.totalTaskNum = totalUploadDataNum;
            mUploadDialog = UploadFragmentDialog.newInstance(msgEntity);
        }
        if (!mUploadDialog.isAdded())
            mUploadDialog.show(fragmentManager, UPLOAD_INFO_FRAGMENT_DIALOG);
        mUploadDialog.setOnEditLocalDataListener(this);
    }

    /**
     * 每一单数据上传成功
     */
    @Override
    public void uploadCollectDataSuccess(UploadMsgEntity info) {
        if (mUploadDialog != null && mUploadDialog.isVisible()) {
            mUploadDialog.addMessage(info);
        }
    }

    /**
     * 数据上传失败
     */
    @Override
    public void uploadCollectDataFail(UploadMsgEntity info) {
        if (mUploadDialog != null && mUploadDialog.isVisible()) {
            mUploadDialog.addMessage(info);
        }
        //刷新界面
        startAutoRefresh();
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
        mPresenter.resetStateAfterUpload();
    }


    @Override
    public void retry(String action) {
        super.retry(action);
        if (mUploadDialog != null) {
            mUploadDialog.setOnEditLocalDataListener(null);
            mUploadDialog.dismiss();
            mUploadDialog = null;
        }
        startAutoRefresh();
    }

    @Override
    public void onItemClick(UploadMsgEntity info) {
        if (info == null || TextUtils.isEmpty(info.bizType)) {
            return;
        }
        Intent intent = new Intent(getContext(), MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(Global.EXTRA_COMPANY_CODE_KEY, Global.COMPANY_CODE);
        bundle.putString(Global.EXTRA_MODULE_CODE_KEY, "");
        bundle.putString(Global.EXTRA_BIZ_TYPE_KEY, info.bizType);
        bundle.putString(Global.EXTRA_REF_TYPE_KEY, info.refType);
        bundle.putString(Global.EXTRA_CAPTION_KEY, info.bizTypeDesc);
        bundle.putInt(Global.EXTRA_MODE_KEY, Global.OFFLINE_MODE);
        bundle.putParcelable(Global.EXTRA_UPLOAD_MSG_KEY, info);
        intent.putExtras(bundle);
        if (mDatas != null) {
            mDatas.clear();
            mDatas = null;
        }
        if (mUploadDialog != null) {
            mUploadDialog.setOnEditLocalDataListener(null);
            mUploadDialog.dismiss();
            mUploadDialog = null;
        }
        mActivity.startActivity(intent);
        mActivity.finish();
    }

    /**
     * 开始上传数据
     */
    @Override
    public void saveCollectedData() {
        mPresenter.uploadCollectedDataOffLine();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDatas != null) {
            mDatas.clear();
            mDatas = null;
        }
        if (mUploadDialog != null) {
            mUploadDialog.setOnEditLocalDataListener(null);
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
