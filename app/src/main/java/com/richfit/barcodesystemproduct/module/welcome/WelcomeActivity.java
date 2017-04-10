package com.richfit.barcodesystemproduct.module.welcome;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.view.View;
import android.widget.Button;

import com.jakewharton.rxbinding.view.RxView;
import com.richfit.barcodesystemproduct.BuildConfig;
import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.base.BaseActivity;
import com.richfit.common_lib.utils.AppCompat;
import com.richfit.common_lib.utils.GUIUtils;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.L;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;

/**
 * Created by monday on 2016/11/8.
 */

public class WelcomeActivity extends BaseActivity<WelcomePresenterImp> implements WelcomeContract.View {

    private final static String CONFIG_FILE_NAME = BuildConfig.CONFIG_FILE_NAME;

    @BindView(R.id.btn_online_mode)
    Button btnOnlineMode;
    @BindView(R.id.btn_offline_mode)
    Button btnOfflineMode;
    @BindView(R.id.reveal_view)
    View revealView;

    int mode;

    @Override
    protected int getContentId() {
        return R.layout.activity_welcome;
    }

    @Override
    public void initInjector() {
        mActivityComponent.inject(this);
    }

    @Override
    public void initEvent() {
        L.e("CONFIG_FILE_NAME " + CONFIG_FILE_NAME);
        RxView.clicks(btnOnlineMode)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(a -> {
                    mode = Global.ONLINE_MODE;
                    mPresenter.loadFragmentConfig(Global.companyId, CONFIG_FILE_NAME);
                });

        RxView.clicks(btnOfflineMode)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(a -> {
                    mode = Global.OFFLINE_MODE;
                    mPresenter.loadFragmentConfig(Global.companyId, CONFIG_FILE_NAME);
                });
    }

    @Override
    public void loadFragmentConfigSuccess() {
        //如果fragment的配置信加载成功，那么直接下载扩展字段的配置信息
        mPresenter.loadExtraConfig(Global.companyId);
    }

    @Override
    public void loadFragmentConfigFail(String message) {
        showMessage(message);
    }

    /**
     * 下载配置文件成功。注意不管是否下载扩展字段的配置信息都必须去下载
     */
    @Override
    public void loadExtraConfigSuccess() {
        toHome();
    }

    @Override
    public void loadExtraConfigFail(String message) {
        toHome();
    }

    /**
     * 显示动画，在动画结束后直接跳转到home页面
     */
    private void toHome() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //如果当前的版本大于5.1那么执行动画
            final View view = Global.ONLINE_MODE == mode ? btnOnlineMode : btnOfflineMode;
            int primaryColor = AppCompat.getColor(R.color.colorPrimary, this);
            int[] location = new int[2];
            revealView.setBackgroundColor(primaryColor);
            view.getLocationOnScreen(location);
            int cx = (location[0] + (view.getWidth() / 2));
            int cy = location[1] + (GUIUtils.getStatusBarHeight(this) / 2);
            hideNavigationStatus();
            GUIUtils.showRevealEffect(revealView, cx, cy, new RevealAnimationListener());
        } else {
            mPresenter.toHome(mode);
        }
    }

    private void hideNavigationStatus() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
    }

    protected class RevealAnimationListener extends AnimatorListenerAdapter {
        @Override
        public void onAnimationEnd(Animator animation) {
            mPresenter.toHome(mode);
        }
    }


}
