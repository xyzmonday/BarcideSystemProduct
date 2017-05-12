package com.richfit.barcodesystemproduct.module_local.upload;

import com.richfit.barcodesystemproduct.module_local.upload.imp.CheckUploadPresenterImp;

/**
 * 盘点数据上传页面
 * Created by monday on 2017/4/21.
 */

public class CheckUploadFragment extends BaseUploadFragment<CheckUploadPresenterImp> {

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

}
