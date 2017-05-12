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
}
