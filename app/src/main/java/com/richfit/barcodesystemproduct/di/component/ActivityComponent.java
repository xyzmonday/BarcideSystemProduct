package com.richfit.barcodesystemproduct.di.component;

import android.app.Activity;
import android.content.Context;

import com.richfit.barcodesystemproduct.di.module.ActivityModule;
import com.richfit.barcodesystemproduct.module.edit.EditActivity;
import com.richfit.barcodesystemproduct.module.home.HomeActivity;
import com.richfit.barcodesystemproduct.module.login.LoginActivity;
import com.richfit.barcodesystemproduct.module.main.MainActivity;
import com.richfit.barcodesystemproduct.module.setting.SettingActivity;
import com.richfit.barcodesystemproduct.module.splash.SplashActivity;
import com.richfit.barcodesystemproduct.module.welcome.WelcomeActivity;
import com.richfit.barcodesystemproduct.module_local.loaddown.LoadLocalRefDataActivity;
import com.richfit.barcodesystemproduct.module_local.upload.UploadActivity;
import com.richfit.common_lib.scope.ActivityScope;
import com.richfit.common_lib.scope.ContextLife;

import dagger.Component;

/**
 * Created by monday on 2016/10/18.
 */
@ActivityScope
@Component(modules = ActivityModule.class,dependencies = AppComponent.class)
public interface ActivityComponent {

    @ContextLife("Activity")
    Context getActivityContext();

    @ContextLife("Application")
    Context getApplicationContext();

    Activity getActivity();
    void inject(SplashActivity activity);
    void inject(LoginActivity activity);
    void inject(WelcomeActivity activity);
    void inject(HomeActivity activity);


    void inject(SettingActivity activity);
    void inject(EditActivity activity);
    void inject(LoadLocalRefDataActivity activity);
    void inject(MainActivity activity);

    void inject(UploadActivity activity);
}
