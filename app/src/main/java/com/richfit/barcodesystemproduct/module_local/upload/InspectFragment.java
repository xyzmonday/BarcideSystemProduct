package com.richfit.barcodesystemproduct.module_local.upload;

/**
 * 离线验收数据上传
 * Created by monday on 2017/4/28.
 */

public class InspectFragment extends BaseUploadFragment {
    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    /**
     * 响应自动下拉刷新动作，并且开始请求接口获取整单缓存
     */
    @Override
    public void onRefresh() {
        mPresenter.readUploadData(1);
    }

    /**
     * 开始上传数据
     */
    @Override
    public void saveCollectedData() {
        mPresenter.uploadInspectionDataOffLine();
    }
}
