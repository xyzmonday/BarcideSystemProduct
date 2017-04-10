package com.richfit.barcodesystemproduct.crash;

/**
 * Created by monday on 2016/4/8.
 * 异常处理基类
 */
public abstract class BaseCrashReport implements CrashListener {

    /**
     * 系统默认的异常处理（默认情况下，系统会终止当前的异常程序）
     */
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;

    public BaseCrashReport() {
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    /**
     * 关闭app
     * @param thread
     * @param ex
     */
    @Override
    public void closeApp(Thread thread, Throwable ex) {
        mDefaultCrashHandler.uncaughtException(thread, ex);
    }
}
