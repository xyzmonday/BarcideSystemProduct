package com.richfit.barcodesystemproduct;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;

import com.github.moduth.blockcanary.BlockCanary;
import com.richfit.barcodesystemproduct.di.component.AppComponent;
import com.richfit.barcodesystemproduct.di.component.DaggerAppComponent;
import com.richfit.barcodesystemproduct.di.module.AppModule;
import com.richfit.barcodesystemproduct.service.InitializeService;
import com.richfit.common_lib.blockcanary.AppBlockCanaryContext;
import com.richfit.common_lib.multisp.SPHelper;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.SPrefUtil;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;


/**
 * Created by monday on 2017/3/17.
 */

public class BarcodeSystemApplication extends Application {
    private static BarcodeSystemApplication app;
    private static AppComponent mAppComponent;
    private static RefWatcher mRefWatcher;
    public static String baseUrl;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //只有主进程以及SDK版本5.0以下才走。
        if (isMainProcess(BarcodeSystemApplication.this) && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (!dexOptDone(base)) {
                preLoadDex(base);
            }
            MultiDex.install(this);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        //初始化自己的全局配置
        app = this;
        //初始化Http的BaseUrl
        baseUrl = generateBaseUrl();
        //初始化全局模块
        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this, baseUrl))
                .build();
        //初始化相关全局参数，注意由于是异步的需要考虑线程同步的问题
        InitializeService.start(this);
        //开启性能检测
        if (BuildConfig.DEBUG) {
            Log.e("yff","App启动baseUrl = " + baseUrl);
            BlockCanary.install(this, new AppBlockCanaryContext()).start();
            mRefWatcher = LeakCanary.install(this);
        }
    }

    private String generateBaseUrl() {
        String baseUrl = null;
        SPrefUtil.initSharePreference(this);
        baseUrl = (String) SPrefUtil.getData("base_url", "");
        if (!TextUtils.isEmpty(baseUrl)) {
            //说明用户手动设置过Url
            return baseUrl;
        }
        //如果没有手动设置过Url
        if (BuildConfig.DEBUG) {
            switch (BuildConfig.APP_NAME) {
                case Global.QINGYANG:
                    //庆阳测试地址
                    baseUrl = "http://11.11.177.100:8092/lhbk_middleware/MobileProcess/";
                    break;
                case Global.QINGHAI:
                    //青海测试地址(D)
                    baseUrl = "http://11.11.47.29:8087/ktbk_middleware/MobileProcess/";
//                    baseUrl = "http://10.82.60.100:8080/ktbk_middleware/MobileProcess/";
                    break;
            }
        } else {
            //正式地址
            baseUrl = BuildConfig.SERVER_URL;
        }
        return baseUrl;
    }

    public static BarcodeSystemApplication getAppContext() {
        return app;
    }

    public static AppComponent getAppComponent() {
        return mAppComponent;
    }

    public static void setAppComponent(AppComponent appComponent) {
        mAppComponent = appComponent;
    }

    public static RefWatcher getRefWatcher() {
        return mRefWatcher;
    }

    /**
     * 当前版本是否进行过DexOpt操作。
     *
     * @param context
     * @return
     */
    private boolean dexOptDone(Context context) {
        SPHelper.init(context);
        return SPHelper.getBoolean("dex_opt", false);
    }

    /**
     * 在单独进程中提前进行DexOpt的优化操作；主进程进入等待状态。
     *
     * @param base
     */
    public void preLoadDex(Context base) {
        Intent intent = new Intent(BarcodeSystemApplication.this, PreLoadDexActivity.class);
        //注意这里使用context启动Activity,需要给出Activity的栈标识
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        base.startActivity(intent);
        while (!dexOptDone(base)) {
            try {
                //主线程开始等待；直到优化进程完成了DexOpt操作。
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取当前应用所在的进程名称
     *
     * @param context
     * @return
     */
    public String getCurProcessName(Context context) {
        String strRet = null;

        try {
            Class pid = Class.forName("android.ddm.DdmHandleAppName");
            Method activityManager = pid.getDeclaredMethod("getAppName", new Class[0]);
            strRet = (String) activityManager.invoke(pid, new Object[0]);
        } catch (Exception var7) {
        }

        if (TextUtils.isEmpty(strRet)) {
            int pid1 = android.os.Process.myPid();
            ActivityManager activityManager1 = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List runningAppProcesses = activityManager1.getRunningAppProcesses();
            Iterator var5 = runningAppProcesses.iterator();
            while (var5.hasNext()) {
                ActivityManager.RunningAppProcessInfo appProcess = (ActivityManager.RunningAppProcessInfo) var5.next();
                if (appProcess.pid == pid1) {
                    strRet = appProcess.processName;
                    break;
                }
            }
        }

        return strRet;
    }

    /**
     * 判断当前应用运行的进程是否在主进程
     *
     * @param context
     * @return
     */
    public boolean isMainProcess(Context context) {
        String packageName = context.getPackageName();
        String processName = getCurProcessName(context);
        return packageName.equalsIgnoreCase(processName);
    }
}
