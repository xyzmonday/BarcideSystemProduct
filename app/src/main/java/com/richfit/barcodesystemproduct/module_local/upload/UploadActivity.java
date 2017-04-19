package com.richfit.barcodesystemproduct.module_local.upload;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jakewharton.rxbinding2.view.RxView;
import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.ShowUploadDataAdapter;
import com.richfit.barcodesystemproduct.base.BaseActivity;
import com.richfit.common_lib.adapter.animation.StickyDividerDecoration;
import com.richfit.common_lib.utils.L;
import com.richfit.domain.bean.ResultEntity;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;

/**
 * 上传离线数据
 * Created by monday on 2017/4/17.
 */

public class UploadActivity extends BaseActivity<UploadPresenterImp>
        implements UploadContract.View {

    @BindView(R.id.btn_upload)
    FloatingActionButton fabUpload;
    @BindView(R.id.rv_show_upload_data)
    RecyclerView recyclerView;

    List<ResultEntity> mDatas;
    ShowUploadDataAdapter mAdapter;
    private StickyRecyclerHeadersDecoration mStickHeader;

    @Override
    public void initVariables() {
        super.initVariables();
        mDatas = new ArrayList<>();
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_upload;
    }

    @Override
    public void initInjector() {
        mActivityComponent.inject(this);
    }

    @Override
    protected void initViews() {
        super.initViews();
        //设置recyclerView
        RecyclerView.LayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(lm);
        mAdapter = new ShowUploadDataAdapter(this, R.layout.item_local_data, mDatas);
        recyclerView.setAdapter(mAdapter);
        mStickHeader = new StickyRecyclerHeadersDecoration(mAdapter);
        recyclerView.addItemDecoration(mStickHeader);
        recyclerView.addItemDecoration(new StickyDividerDecoration(this));
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        mPresenter.readUploadData();
    }

    @Override
    public void initEvent() {
        super.initEvent();
        RxView.clicks(fabUpload)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(a -> mPresenter.uploadCollectedDataOffLine());
    }

    /**
     * 显示需要上传的明细数据
     *
     * @param results
     */
    @Override
    public void showUploadData(ArrayList<ResultEntity> results) {
        mDatas.addAll(results);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 读取需要上传的数据失败
     *
     * @param message
     */
    @Override
    public void readUploadDataFail(String message) {
        mDatas.clear();
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void uploadCollectDataComplete() {
        showMessage("数据上传成功!!!");
        mAdapter.notifyDataSetChanged();
        mPresenter.resetStateAfterUpload();
    }

    @Override
    public void uploadCollectDataSuccess(int taskNum, int offset, String materialDoc, String transNum) {
        L.e("taskNum = " + taskNum + ";offset = " + offset + "; materialDoc = " + materialDoc + "; transNum = " + transNum);
        mAdapter.setStickyHeaderData(taskNum, offset, materialDoc, transNum);
        mStickHeader.invalidateHeaders();
    }

    @Override
    public void uploadCollectDataFail(String message) {
        showMessage(message);
    }

    @Override
    protected void onDestroy() {
        if (mDatas != null) {
            mDatas.clear();
            mDatas = null;
        }
        super.onDestroy();
    }
}
