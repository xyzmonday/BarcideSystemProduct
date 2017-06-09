package com.richfit.barcodesystemproduct.module.setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.jakewharton.rxbinding2.view.RxView;
import com.richfit.barcodesystemproduct.BarcodeSystemApplication;
import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.base.BaseActivity;
import com.richfit.barcodesystemproduct.module.setting.imp.SettingPresenterImp;
import com.richfit.barcodesystemproduct.module.splash.SplashActivity;
import com.richfit.common_lib.dialog.ProgressDialogFragment;
import com.richfit.common_lib.utils.FileUtil;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.common_lib.widget.ButtonCircleProgressBar;
import com.richfit.domain.bean.LoadBasicDataWrapper;
import com.richfit.domain.bean.UpdateEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import ch.ielse.view.SwitchView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import zlc.season.rxdownload2.entity.DownloadStatus;

/**
 * 基础数据:
 * base_material_code wl  增量 分页
 * base_location      cw  增量 分页
 * base_unit_code     dw  全部 不分页
 * base_cost_center   cc  增量 分页
 * base_project_num   xm  增量 分页
 * base_supplier      gys 全部 分页
 * Created by monday on 2016/11/29.
 */

public class SettingActivity extends BaseActivity<SettingPresenterImp>
        implements ISettingView, OnItemClickListener {

    private static final String PROGRESS_DIALOG_TAG = "progress_dialog";

    private static final int LOAD_STATUS_START = 0;
    private static final int LOAD_STATUS_LADING = 1;
    private static final int LOAD_STATUS_END = 2;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;
    @BindView(R.id.sb_material)
    SwitchView sbMaterial;
    @BindView(R.id.sb_supplier)
    SwitchView sbSupplier;
    @BindView(R.id.sb_cost_center)
    SwitchView sbCostCenter;
    @BindView(R.id.sb_location)
    SwitchView sbLocation;
    @BindView(R.id.sb_project_num)
    SwitchView sbProjectNum;
    @BindView(R.id.check_update_apk)
    TextView mCheckUpdateApk;
    @BindView(R.id.floating_button)
    FloatingActionButton fabButton;
    @BindView(R.id.btn_circle_progress)
    ButtonCircleProgressBar mProgressBar;
    @BindView(R.id.set_ip)
    LinearLayout llSetIP;
    @BindView(R.id.sb_batch_flag)
    SwitchView sbBatchFlag;

    private UpdateEntity mUpdateInfo;
    private int mCurrentLoadStatus = LOAD_STATUS_START;
    private ProgressDialogFragment mProgressDialog;
    private String mMessage;
    private AlertView mAlertView;
    private EditText mIP1;
    private EditText mIP2;
    private EditText mIP3;
    private EditText mIP4;
    private EditText mEtPort;

    @Override
    protected int getContentId() {
        return R.layout.activity_setting;
    }

    @Override
    public void initInjector() {
        mActivityComponent.inject(this);
    }

    @Override
    protected void initViews() {
        setupToolBar();
    }


    @Override
    public void initEvent() {
        /*获取版本信息*/
        RxView.clicks(mCheckUpdateApk)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .filter(a -> mCurrentLoadStatus != LOAD_STATUS_LADING)
                .subscribe(a -> mPresenter.getAppVersion());

        /*下载基础数据*/
        RxView.clicks(fabButton)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .filter(a -> mCurrentLoadStatus != LOAD_STATUS_LADING)
                .subscribe(a -> startLoadBasicData());
        /*IP设置*/
        RxView.clicks(llSetIP)
                .subscribe(a -> startSetupIP());
    }


    @Override
    public void initData(Bundle savedInstanceState) {
        mCheckUpdateApk.setText(mCheckUpdateApk.getText() + "V(" + String.valueOf(UiUtil.getCurrentVersionName(this)) + ")");
    }

    /**
     * 启动IP设置界面
     */
    private void startSetupIP() {
        if (mAlertView == null) {
            mAlertView = new AlertView("提示", "请输入服务器地址", "取消", null, new String[]{"完成"}, this, AlertView.Style.Alert, this);
            ViewGroup extView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.ip_manager_dialog, null);
            mIP1 = (EditText) extView.findViewById(R.id.ip1);
            mIP2 = (EditText) extView.findViewById(R.id.ip2);
            mIP3 = (EditText) extView.findViewById(R.id.ip3);
            mIP4 = (EditText) extView.findViewById(R.id.ip4);
            mEtPort = (EditText) extView.findViewById(R.id.et_port);
            mAlertView.addExtView(extView);
        }
        mAlertView.show();
    }

    private void startLoadBasicData() {
        ArrayList<LoadBasicDataWrapper> requestParams = new ArrayList<>();
        LoadBasicDataWrapper task = new LoadBasicDataWrapper();
        mMessage = "";
        if (sbMaterial.isOpened()) {
            task.isByPage = true;
            task.queryType = "WL";
            requestParams.add(task);
            mMessage += "物料;";
        }

        if (sbSupplier.isOpened()) {
            task.isByPage = true;
            task.queryType = "GYS";
            requestParams.add(task);
            mMessage += "供应商;";
        }

        if (sbCostCenter.isOpened()) {
            task = new LoadBasicDataWrapper();
            task.isByPage = true;
            task.queryType = "CZ";
            requestParams.add(task);
            mMessage += "成本中心;";
        }

        if (sbProjectNum.isOpened()) {
            task = new LoadBasicDataWrapper();
            task.isByPage = true;
            task.queryType = "XM";
            requestParams.add(task);
            mMessage += "项目编号;";
        }

        if (sbLocation.isOpened()) {
            task = new LoadBasicDataWrapper();
            task.isByPage = true;
            task.queryType = "CW";
            requestParams.add(task);
            mMessage += "仓位主数据";
        }

        if(sbBatchFlag.isOpened()) {
            task = new LoadBasicDataWrapper();
            task.isByPage = true;
            task.queryType = "PC";
            requestParams.add(task);
            mMessage += "批次启用主数据";
        }

        if (TextUtils.isEmpty(mMessage)) {
            showMessage("请您先选择要下载的数据");
            return;
        }


        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("提示");
        dialog.setMessage("您选择将要下载的基础数据包括:\n" + mMessage + "\n按下确定将开始下载数据!");
        dialog.setPositiveButton("确定", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            mPresenter.loadAndSaveBasicData(requestParams);
        });
        dialog.setNegativeButton("取消", (dialogInterface, i) -> dialogInterface.dismiss());
        dialog.show();
    }

    private void setupToolBar() {
        //设置标题必须在setSupportActionBar之前才有效
        mToolbarTitle.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        mToolbarTitle.setText("用户设置");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void checkAppVersion(UpdateEntity info) {
        //获取当前的版本号
        mUpdateInfo = info;
        int currentVersion = UiUtil.getCurrentVersionCode(this.getApplicationContext());
        if (info.appNum > currentVersion) {
            //提示用户需要更新
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("检测到最新的版本:" + info.appVersion);
            dialog.setMessage(info.appUpdateDesc);
            dialog.setPositiveButton("现在下载", (dialogInterface, i) -> start(info.appDownloadUrl, info.appName));
            dialog.setNegativeButton("以后再说", (dialogInterface, i) -> dialogInterface.dismiss());
            dialog.show();
        } else {
            showMessage("当前为最新版本!!!");
            mProgressBar.setVisibility(View.INVISIBLE);
            mProgressBar.setStatus(ButtonCircleProgressBar.Status.End);
        }
    }

    @Override
    public void getUpdateInfoFail(String message) {
        showMessage(message);
    }

    @Override
    public void prepareLoadApp() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setStatus(ButtonCircleProgressBar.Status.Starting);
    }

    @Override
    public void loadLatestAppFail(String message) {
        showMessage(message);
        mCurrentLoadStatus = LOAD_STATUS_START;
        mProgressBar.setStatus(ButtonCircleProgressBar.Status.End);
    }

    @Override
    public void showLoadAppProgress(DownloadStatus status) {
        mCurrentLoadStatus = LOAD_STATUS_LADING;
        mProgressBar.setMax((int) status.getTotalSize());
        mProgressBar.setProgress((int) status.getDownloadSize());
    }

    @Override
    public void loadAppComplete() {
        mCurrentLoadStatus = LOAD_STATUS_END;
        mProgressBar.setStatus(ButtonCircleProgressBar.Status.End);
        mProgressBar.setVisibility(View.INVISIBLE);
        //自动安装
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("温馨提示")
                .setMessage("下载成功,是否现在安装?在安装新版本之前请确认你是否有离线的业务数据未上传!")
                .setPositiveButton("现在安装", (dialog1, which) -> {
                    dialog1.dismiss();
                    autoInstall();
                }).setNegativeButton("取消安装", (dialog12, which) -> {
            dialog12.dismiss();
            deleteApp();
        }).show();
    }

    private void autoInstall() {
        //如果新版本不能覆盖旧版本的数据，那么必须让它从新下载
        SPrefUtil.saveData(Global.IS_APP_FIRST_KEY, true);
        SPrefUtil.saveData(Global.IS_INITED_FRAGMENT_CONFIG_KEY, false);
        mPresenter.setLocal(false);
        String apkCacheDir = FileUtil.getApkCacheDir(this.getApplicationContext());
        String appName = mUpdateInfo.appName;
        File file = new File(apkCacheDir, appName);
        if (file == null || !file.exists()) {
            showMessage("文件不存在");
            return;
        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }

    private void deleteApp() {
        String apkCacheDir = FileUtil.getApkCacheDir(this.getApplicationContext());
        String appName = mUpdateInfo.appName;
        File file = new File(apkCacheDir, appName);
        if (file == null || !file.exists()) {
            showMessage("文件不存在");
            return;
        }
        file.delete();
    }

    /**
     * 开始下载app
     *
     * @param url
     * @param saveName
     */
    private void start(String url, String saveName) {
        mCurrentLoadStatus = LOAD_STATUS_START;
        String apkCacheDir = FileUtil.getApkCacheDir(this.getApplicationContext());
        mPresenter.loadLatestApp(url, saveName, apkCacheDir);
    }


    @Override
    public void onStartLoadBasicData(int maxProgress) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mProgressDialog = (ProgressDialogFragment) fragmentManager.findFragmentByTag(PROGRESS_DIALOG_TAG);
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialogFragment();
        }
        mProgressDialog.setMaxProgress(maxProgress);
        if (!mProgressDialog.isAdded())
            mProgressDialog.show(fragmentManager, PROGRESS_DIALOG_TAG);
    }

    @Override
    public void loadBasicDataProgress(float progress) {
        mProgressDialog.setProgress(progress);
    }

    @Override
    public void loadBasicDataFail(String message) {
        showMessage(message);
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void loadBasicDataComplete() {
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("下载成功")
                .setContentText(mMessage + "基础数据下载成功,请进行其他的操作")
                .show();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onItemClick(Object o, int position) {
        hideKeyboard(mIP1);
        //判断是否是拓展窗口View，而且点击的是非取消按钮
        if (o == mAlertView && position != AlertView.CANCELPOSITION) {
            if (!checkIP(mIP1, mIP2, mIP3, mIP4)) {
                showMessage("您输入的IP不合理");
                return;
            }

            final String port = mEtPort.getText().toString();
            if (TextUtils.isEmpty(port)) {
                showMessage("请输入端口号");
                return;
            }
            //1. 先拿到当前服务器地址的资源名
            final String url = BarcodeSystemApplication.baseUrl;
            final StringBuffer sb = new StringBuffer();
            if (!TextUtils.isEmpty(url)) {
                int indexOf = url.indexOf("/", 7);//这里去除http://
                if (indexOf > 0) {
                    String webURI = url.substring(indexOf);
                    sb.append("http://")
                            .append(mIP1.getText())
                            .append(".")
                            .append(mIP2.getText())
                            .append(".")
                            .append(mIP3.getText())
                            .append(".")
                            .append(mIP4.getText())
                            .append(":")
                            .append(port)
                            .append(webURI);
                }
            }
            final String newUrl = sb.toString();
            if (TextUtils.isEmpty(newUrl)) {
                showMessage("服务为空");
                return;
            }
            mPresenter.setupUrl(newUrl);
            return;
        }
    }


    @Override
    public void setupUrlComplete() {
        //启动SplashActivity
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean checkIP(EditText... ets) {
        for (EditText et : ets) {
            int ip = UiUtil.convertToInt(et.getText(), 0);
            if (ip <= 0 || ip > 255) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAlertView != null) {
            mAlertView.dismiss();
            mAlertView = null;
        }
    }
}


