package com.richfit.barcodesystemproduct;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.richfit.common_lib.multisp.SPHelper;

import java.util.List;

/**
 * Created by monday on 2017/5/2.
 */

public class PreLoadDexActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);//取消掉系统默认的动画。
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    MultiDex.install(getApplication());
                    SPHelper.init(PreLoadDexActivity.this.getApplicationContext());
                    SPHelper.save("dex_opt",true);
                    killCurrentProcess(PreLoadDexActivity.this);
                } catch (Exception e) {
                    killCurrentProcess(PreLoadDexActivity.this);
                }
            }
        }.start();
    }


    public void killCurrentProcess(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager
                .getRunningAppProcesses();
        String currentProcess = context.getApplicationInfo().processName;
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
            String processName = appProcessInfo.processName;
            if (!processName.equals(currentProcess)) {
                activityManager.killBackgroundProcesses(processName);
            }
        }
    }
}

