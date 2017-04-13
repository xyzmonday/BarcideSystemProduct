package com.richfit.barcodesystemproduct.di.module;

import android.app.Application;
import android.content.Context;

import com.richfit.common_lib.rxutils.SimpleRxBus;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.data.repository.Repository;
import com.richfit.domain.repository.ILocalRepository;
import com.richfit.domain.repository.IServerRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by monday on 2016/10/18.
 */

@Module(includes = {LocalApiModule.class, ServerApiModule.class})
public class AppModule {

    private final Application mApp;
    private final String mBaseUrl;

    public AppModule(Application app,String baseUrl) {
        this.mApp = app;
        this.mBaseUrl = baseUrl;
    }

    @Provides
    @Singleton
    @ContextLife("Application")
    public Context provideContext() {
        return mApp.getApplicationContext();
    }

    @Provides
    public String provideBaseUrl() {
        return mBaseUrl;
    }

    @Provides
    @Singleton
    public SimpleRxBus provideSimpleRxBus() {
        return SimpleRxBus.getInstance();
    }

    @Provides
    @Singleton
    public Repository provideRepository(IServerRepository serverDataApi, ILocalRepository localDataApi) {
        return new Repository(serverDataApi, localDataApi);
    }
}
