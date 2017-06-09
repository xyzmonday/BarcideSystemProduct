package com.richfit.barcodesystemproduct.module.splash;

import com.richfit.common_lib.IInterface.IPresenter;
import com.richfit.domain.bean.LoadBasicDataWrapper;

import java.util.ArrayList;

/**
 * Created by monday on 2016/12/2.
 */

public interface ISplashPresenter extends IPresenter<ISplashView> {

    /**
     * 下载基础数据
     * @param requestParam
     */
    void loadAndSaveBasicData(ArrayList<LoadBasicDataWrapper> requestParam);

    /**
     * 下载基础数据库,仅仅在app第一次启动的时候同步
     */
    void downloadInitialDB();

    void getConnectionStatus();

}
