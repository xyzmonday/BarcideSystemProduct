package com.richfit.barcodesystemproduct;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.github.moduth.blockcanary.BlockCanary;
import com.richfit.barcodesystemproduct.di.component.AppComponent;
import com.richfit.barcodesystemproduct.di.component.DaggerAppComponent;
import com.richfit.barcodesystemproduct.di.module.AppModule;
import com.richfit.barcodesystemproduct.service.InitializeService;
import com.richfit.common_lib.blockcanary.AppBlockCanaryContext;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by monday on 2017/3/17.
 */

public class BarcodeSystemApplication extends Application {
    private  static BarcodeSystemApplication app;
    private static AppComponent mAppComponent;
    private static RefWatcher mRefWatcher;
    private final static String baseUrl = BuildConfig.SERVER_URL;
//    private final static String baseUrl = "http://10.88.53.10:8080/ktbk/MobileProcess/";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //注意该app的方法数已经超过了64k
        MultiDex.install(this) ;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化自己的全局配置
        app = this;
        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this,baseUrl))
                .build();
        BlockCanary.install(this, new AppBlockCanaryContext()).start();
        InitializeService.start(this);
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        mRefWatcher = LeakCanary.install(this);
    }

    public static BarcodeSystemApplication getAppContext() {
        return app;
    }

    public static AppComponent getAppComponent() {
        return mAppComponent;
    }

    public static RefWatcher getRefWatcher() {
        return mRefWatcher;
    }

}
