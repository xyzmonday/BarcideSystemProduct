package com.richfit.barcodesystemproduct.module.splash;

import com.richfit.barcodesystemproduct.base.BaseView;

/**
 * Created by monday on 2016/12/2.
 */

public interface ISplashView extends BaseView{

    void networkAvailable();
    void networkNotAvailable(String message);

    void toLogin();
    void syncDataError(String message);

    void downDBComplete();
    void downDBFail(String message);

}
