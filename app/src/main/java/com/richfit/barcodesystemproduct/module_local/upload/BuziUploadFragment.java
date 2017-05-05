package com.richfit.barcodesystemproduct.module_local.upload;

import com.richfit.barcodesystemproduct.module_local.upload.imp.UploadPresenterImp;

/**
 * 出入库上传页面
 * Created by monday on 2017/4/21.
 */

public class BuziUploadFragment extends BaseUploadFragment<UploadPresenterImp> {


    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    public void initData() {
        startAutoRefresh();
    }

    /**
     * 响应自动下拉刷新动作，并且开始请求接口获取整单缓存
     */
    @Override
    public void onRefresh() {
        mPresenter.readUploadData(0);
    }

    /**
     * 开始上传数据
     */
    @Override
    public void saveCollectedData() {
        mPresenter.uploadCollectedDataOffLine();
    }


}
