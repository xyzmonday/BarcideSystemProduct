package com.richfit.barcodesystemproduct.module_local.upload;

/**
 * 出入库上传页面
 * Created by monday on 2017/4/21.
 */

public class BuziUploadFragment extends BaseUploadFragment {


    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    public void initData() {
        startAutoRefresh();
    }

    /**
     * 开始上传数据
     */
    @Override
    public void saveCollectedData() {
        mPresenter.uploadCollectedDataOffLine();
    }


}
