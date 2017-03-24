package com.richfit.barcidesystemproduct.module.splash;

import com.richfit.barcodesystemproduct.base.BaseView;

/**
 * Created by monday on 2016/12/2.
 */

public interface ISplashView extends BaseView{

    void toLogin();

    void syncDateSuccess(String date);
    void syncDateFail(String message);
    void syncDateComplete();

    void unRegister(String message);
    void registered();
}
