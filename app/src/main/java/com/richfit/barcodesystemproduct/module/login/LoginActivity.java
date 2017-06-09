package com.richfit.barcodesystemproduct.module.login;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.jakewharton.rxbinding2.view.RxView;
import com.richfit.barcodesystemproduct.BarcodeSystemApplication;
import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.BottomMenuAdapter;
import com.richfit.barcodesystemproduct.base.BaseActivity;
import com.richfit.barcodesystemproduct.module.splash.SplashActivity;
import com.richfit.barcodesystemproduct.module.welcome.WelcomeActivity;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.common_lib.widget.RichAutoEditText;
import com.richfit.domain.bean.BottomMenuEntity;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by monday on 2016/10/27.
 */

public class LoginActivity extends BaseActivity<LoginPresenterImp> implements LoginContract.View, OnItemClickListener {

    @BindView(R.id.et_username)
    RichAutoEditText etUsername;
    @BindView(R.id.et_password)
    RichAutoEditText etPassword;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.floating_button)
    FloatingActionButton btnShowInfo;


    private AlertView mAlertView;
    private TextView mOldIP;
    private EditText mIP1;
    private EditText mIP2;
    private EditText mIP3;
    private EditText mIP4;
    private EditText mEtPort;

    @Override
    protected int getContentId() {
        return R.layout.activity_login;
    }

    @Override
    public void initInjector() {
        mActivityComponent.inject(this);
    }

    @Override
    public void initEvent() {
        etUsername.setOnRichAutoEditTouchListener((view, text) -> etUsername.setText(""));
        etPassword.setOnRichAutoEditTouchListener((view, text) -> etPassword.setText(""));

        RxView.clicks(btnLogin)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(a -> prepareLogin());

        RxView.clicks(etUsername)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(a -> {
                    if (etUsername.getAdapter() != null) {
                        etUsername.setThreshold(0);
                        etUsername.showDropDown();
                    }
                });

        RxView.clicks(btnShowInfo)
                .subscribe(a -> showChooseDialog());
    }


    @Override
    public void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mPresenter.readUserInfos();
        //如果没有网络，不需要上传奔溃日志
        if (mPresenter != null && !mPresenter.isLocal())
            mPresenter.uploadCrashLogFiles();
    }

    /**
     * 准备登陆，如果是离线的情况那么直接登陆，如果是在线那么先检查是否注册过了
     */
    private void prepareLogin() {
        if (mPresenter.isLocal()) {
            mPresenter.login(etUsername.getText().toString(),
                    etPassword.getText().toString());
        } else {
            mPresenter.getMappingInfo();
        }
    }

    @Override
    public void registered() {
        mPresenter.login(etUsername.getText().toString(),
                etPassword.getText().toString());
    }

    @Override
    public void unRegister(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("提示")
                .setMessage(message + ".该手持MAC地址是" + Global.MAC_ADDRESS + ".注意注册完成后系统将自动重启APP!")
                .setPositiveButton("已经注册", (dialog1, which) -> {
                    dialog1.dismiss();
                    Intent intent = new Intent(LoginActivity.this, SplashActivity.class);
                    startActivity(intent);
                    finish();
                }).show();
    }


    @Override
    public void toHome() {
        showMessage("登陆成功");
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void showChooseDialog() {
        View rootView = LayoutInflater.from(this).inflate(R.layout.menu_bottom, null);
        GridView menu = (GridView) rootView.findViewById(R.id.gridview);
        ArrayList<BottomMenuEntity> items = new ArrayList<>();
        BottomMenuEntity item = new BottomMenuEntity();
        item.menuName = "App基本信息";
        item.menuImageRes = R.mipmap.icon_data_submit;
        items.add(item);

        item = new BottomMenuEntity();
        item.menuName = "修改IP";
        item.menuImageRes = R.mipmap.icon_transfer;
        items.add(item);


        BottomMenuAdapter adapter = new BottomMenuAdapter(this, R.layout.item_bottom_menu, items);
        menu.setAdapter(adapter);

        final Dialog dialog = new Dialog(this, R.style.MaterialDialogSheet);
        dialog.setContentView(rootView);
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();

        menu.setOnItemClickListener((adapterView, view, position, id) -> {
            switch (position) {
                case 0:
                    showAppBasicInfo();
                    break;
                case 1:
                    showEditIPDialog();
                    break;

            }
            dialog.dismiss();
        });
    }

    private void showAppBasicInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        StringBuffer sb = new StringBuffer();
        sb.append("当前使用的系统")
                .append(mPresenter.isLocal() ? "离线系统" : "在线系统")
                .append("\n")
                .append("当前版本号:")
                .append("V(")
                .append(UiUtil.getCurrentVersionName(this))
                .append(")")
                .append("\n")
                .append("服务器地址:")
                .append(BarcodeSystemApplication.baseUrl);
        builder.setTitle("App信息查看")
                .setMessage(sb.toString())
                .show();
    }


    /**
     * 修改ip
     */
    private void showEditIPDialog() {
        if (mPresenter != null) {
            if (mAlertView == null) {
                mAlertView = new AlertView("提示", "请输入新的服务器地址", "取消", null, new String[]{"完成"}, this, AlertView.Style.Alert, this);
                ViewGroup extView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.ip_manager_dialog, null);
                mOldIP = (TextView) extView.findViewById(R.id.tv_old_ip);
                mIP1 = (EditText) extView.findViewById(R.id.ip1);
                mIP2 = (EditText) extView.findViewById(R.id.ip2);
                mIP3 = (EditText) extView.findViewById(R.id.ip3);
                mIP4 = (EditText) extView.findViewById(R.id.ip4);
                mEtPort = (EditText) extView.findViewById(R.id.et_port);
                int indexOf = BarcodeSystemApplication.baseUrl.indexOf("/", 7);
                if (indexOf < 7) {
                    mOldIP.setVisibility(ViewGroup.GONE);
                } else {
                    String ip = BarcodeSystemApplication.baseUrl.substring(7, indexOf);
                    mOldIP.setText("当前的ip是:" + ip);
                }
                mAlertView.addExtView(extView);
            }
            mAlertView.show();
        }
    }

    @Override
    public void loginFail(String message) {
        showMessage(message);
        etUsername.setAdapter(null);
    }

    @Override
    public void showUserInfos(ArrayList<String> list) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, list);
        etUsername.setAdapter(adapter);
    }

    @Override
    public void loadUserInfosFail(String message) {
        showMessage(message);
    }


    @Override
    public void networkConnectError(String retryAction) {
        showNetConnectErrorDialog(Global.RETRY_LOGIN_ACTION);
    }

    @Override
    public void retry(String action) {
        switch (action) {
            case Global.RETRY_LOGIN_ACTION:
                mPresenter.login(etUsername.getText().toString(),
                        etPassword.getText().toString());
                break;
            case Global.RETRY_REGISTER_ACTION:
                mPresenter.getMappingInfo();
                break;
        }
        super.retry(action);
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
