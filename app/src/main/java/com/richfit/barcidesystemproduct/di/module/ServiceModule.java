package com.richfit.barcidesystemproduct.di.module;

import android.app.Service;
import android.content.Context;

import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.scope.ServiceScope;

import dagger.Module;
import dagger.Provides;

@Module
public class ServiceModule {
    private Service mService;

    public ServiceModule(Service service) {
        mService = service;
    }

    @Provides
    @ServiceScope
    @ContextLife("Service")
    public Context provideContext() {
        return mService;
    }

}