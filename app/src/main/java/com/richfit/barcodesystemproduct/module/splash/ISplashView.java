package com.richfit.barcodesystemproduct.module.splash;

import com.richfit.barcodesystemproduct.base.BaseView;

/**
 * Created by monday on 2016/12/2.
 */

public interface ISplashView extends BaseView{

    void toLogin();
    void syncDataError(String message);

    void unRegister(String message);
    void registered();

    void downDBComplete();
    void downDBFail(String message);
}
