package com.richfit.barcodesystemproduct;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.github.moduth.blockcanary.BlockCanary;
import com.richfit.barcodesystemproduct.di.component.AppComponent;
import com.richfit.barcodesystemproduct.di.component.DaggerAppComponent;
import com.richfit.barcodesystemproduct.di.module.AppModule;
import com.richfit.barcodesystemproduct.service.InitializeService;
import com.richfit.common_lib.blockcanary.AppBlockCanaryContext;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import net.grandcentrix.tray.AppPreferences;

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
//    public final static String baseUrl = BuildConfig.SERVER_URL;
//    public final static String baseUrl = "http://10.88.53.9:8080/lhbk_middleware/MobileProcess/";
    //庆阳测试地址
//    public final static String baseUrl = "http://11.11.177.100:8092/lhbk_middleware/MobileProcess/";
    //青海培训地址
    public final static String baseUrl = "http://11.11.177.98:8088/ktbk_middleware/MobileProcess/";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //注意该app的方法数已经超过了64k
//        MultiDex.install(this) ;
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
        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this, baseUrl))
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

    /**
     * 当前版本是否进行过DexOpt操作。
     * @param context
     * @return
     */
    private boolean dexOptDone(Context context) {
        final AppPreferences sp = new AppPreferences(getAppContext());
        return sp.getBoolean("dex_opt",false);
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


    public String getCurProcessName(Context context) {
        String strRet = null;

        try {
            Class pid = Class.forName("android.ddm.DdmHandleAppName");
            Method activityManager = pid.getDeclaredMethod("getAppName", new Class[0]);
            strRet = (String)activityManager.invoke(pid, new Object[0]);
        } catch (Exception var7) {
        }

        if(TextUtils.isEmpty(strRet)) {
            int pid1 = android.os.Process.myPid();
            ActivityManager activityManager1 = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            List runningAppProcesses = activityManager1.getRunningAppProcesses();
            Iterator var5 = runningAppProcesses.iterator();

            while(var5.hasNext()) {
                ActivityManager.RunningAppProcessInfo appProcess = (ActivityManager.RunningAppProcessInfo)var5.next();
                if(appProcess.pid == pid1) {
                    strRet = appProcess.processName;
                    break;
                }
            }
        }

        return strRet;
    }

    public boolean isMainProcess(Context context) {
        String packageName = context.getPackageName();
        String processName = getCurProcessName(context);
        return packageName.equalsIgnoreCase(processName);
    }
}
