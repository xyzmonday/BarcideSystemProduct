package com.richfit.barcodesystemproduct.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.richfit.barcodesystemproduct.crash.CrashManager;
import com.richfit.barcodesystemproduct.crash.HttpCrashReport;
import com.richfit.common_lib.utils.SPrefUtil;

/**
 * 初始化app的服务。
 */

public class InitializeService extends IntentService {

    private static final String ACTION_INIT_WHEN_APP_CREATE = "com.richfit.barcodesystemproduc.service.action.init";

    public InitializeService() {
        super("InitializeService");
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, InitializeService.class);
        intent.setAction(ACTION_INIT_WHEN_APP_CREATE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_INIT_WHEN_APP_CREATE.equals(action)) {
                performInit();
            }
        }
    }

    private void performInit() {
        initSPrefUtils();
        initCrashManage();
    }

    private void initSPrefUtils() {
        SPrefUtil.initSharePreference(getApplication());
    }

    private void initCrashManage() {
        CrashManager crashManager = CrashManager.getInstance();
        HttpCrashReport httpCrashReport = new HttpCrashReport();
        crashManager.init(getApplication(),httpCrashReport);
    }
}