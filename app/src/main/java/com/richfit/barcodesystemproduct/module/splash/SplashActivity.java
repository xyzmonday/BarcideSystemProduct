package com.richfit.barcodesystemproduct.module.splash;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.richfit.barcodesystemproduct.BuildConfig;
import com.richfit.barcodesystemproduct.base.BaseActivity;
import com.richfit.barcodesystemproduct.module.login.LoginActivity;
import com.richfit.barcodesystemproduct.module.splash.imp.SplashPresenterImp;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.NetworkStateUtil;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.LoadBasicDataWrapper;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;

/**
 * 在启动app的时候，如果一个Activity所属的Application还没运行，系统会为这个Activity创建一个
 * 进程（每开启一个进程都会有一个Application，所以Application的onCreate()可能会被调用多次），
 * 但是启动一个进程需要时间，所以如果该进程初始化耗时较长，那么屏幕没有任何动静。
 * 所以，系统会自动启动一个startingWindow。StartingWindow一般出现在应用程序进程创建并初始化成功前，所以它是个临时窗口，对应的WindowType是TYPE_APPLICATION_STARTING。
 * 目的是告诉用户，系统已经接受到操作，正在响应，在程序初始化完成后实现目的UI，同时移除这个窗口。
 * 系统给startingWindow的DecorView设置Activity或者Application的Theme。
 * 所以，我们启动一个Splash页面，这里需要注意的是Splash页面不能够有耗时操作。
 * 因为如果Splash有耗时操作，那么startingWindow结束显示后，会显示Splash的
 * Window(注意Splash页面没有setContentView)。所以SplashActivity中不要做太多操作，如果确实要做的话，我想到的解决方式是：
 * 将两个的window的background设置的一样，这样即便startingWindow结束，用户也感觉不到切换了。
 * 设置SplashActivity对应window的background的方法，在SplashActivity的onCreate中调用
 * getWindow().setBackgroundDrawableResource(背景图片);
 * Created by monday on 2016/12/2.
 */

public class SplashActivity extends BaseActivity<SplashPresenterImp>
        implements ISplashView {

    ArrayList<LoadBasicDataWrapper> mRequestParam;

    static {
        System.loadLibrary("IAL");
        System.loadLibrary("SDL");
        System.loadLibrary("barcodereader44");
    }

    @Override
    protected int getContentId() {
        return 0;
    }

    @Override
    public void initInjector() {
        mActivityComponent.inject(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setStatusBar(false);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        //1. 检查是否有网络；2.检查是否是第一次登陆
        if (!NetworkStateUtil.isNetConnected(this.getApplicationContext())) {
            mPresenter.setLocal(true);
            toLogin();
            return;
        }
        toRegister();
    }

    private void toRegister() {
        Global.MAC_ADDRESS = UiUtil.getMacAddress();
        mPresenter.register();
    }

    /**
     * 跳转到登陆页面
     */
    @Override
    public void toLogin() {
        Flowable.timer(1000, TimeUnit.MILLISECONDS)
                .compose(TransformerHelper.io2main())
                .subscribe(a -> {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    overridePendingTransition(0, android.R.anim.fade_out);
                    finish();
                });
    }

    /**
     * 同步基础数据出错
     *
     * @param message
     */
    @Override
    public void syncDataError(String message) {
        if (BuildConfig.LOG_DEBUG && BuildConfig.APP_NAME.equals(Global.QINGYANG)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示")
                    .setMessage("同步基础数据出错:" + message);
            builder.setPositiveButton("继续", (dialog, which) -> {
                dialog.dismiss();
                toLogin();
            });
            builder.show();
        } else {
            toLogin();
        }
    }

    /**
     * 用户未注册，提示用户注册。
     *
     * @param message
     */
    @Override
    public void unRegister(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示")
                .setMessage("该手持未注册，请将提供的手持MAC地址联系条码项目组进行注册。MAC地址为" + Global.MAC_ADDRESS)
                .show();
    }

    /**
     * 注册完毕
     */
    @Override
    public void registered() {
        switch (BuildConfig.APP_NAME) {
            case Global.QINGYANG:
                startSyncBasicData();
                break;
            case Global.QINGHAI:
                mPresenter.downloadInitialDB();
                break;
        }
    }

    @Override
    public void downDBComplete() {
        startSyncBasicData();
    }

    @Override
    public void downDBFail(String message) {
        showMessage(message);
    }

    /**
     * 同步基础数据
     */
    private void startSyncBasicData() {
        if (mRequestParam == null) {
            mRequestParam = new ArrayList<>();
        }
        LoadBasicDataWrapper task = null;
        switch (BuildConfig.APP_NAME) {
            case Global.QINGYANG:
                task = new LoadBasicDataWrapper();
                task.isByPage = false;
                task.queryType = "ZZ2";
                mRequestParam.add(task);
                break;
            default:
                task = new LoadBasicDataWrapper();
                task.isByPage = false;
                task.queryType = "ZZ";
                mRequestParam.add(task);
                break;
        }

        //获取扩展字段的字典
        task = new LoadBasicDataWrapper();
        task.isByPage = false;
        task.queryType = "SD";
        mRequestParam.add(task);

        mPresenter.loadAndSaveBasicData(mRequestParam);
    }

    @Override
    public void networkConnectError(String retryAction) {
        showNetConnectErrorDialog(retryAction);
    }

    @Override
    public void retry(String action) {
        switch (action) {
            case Global.RETRY_REGISTER_ACTION:
                mPresenter.register();
                break;
            case Global.RETRY_SYNC_BASIC_DATA_ACTION:
                mPresenter.loadAndSaveBasicData(mRequestParam);
                break;
        }
        //回调父方法关闭对话框
        super.retry(action);
    }
}
