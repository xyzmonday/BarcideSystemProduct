package com.richfit.barcodesystemproduct.di.component;

import android.content.Context;

import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.scope.ServiceScope;
import com.richfit.barcodesystemproduct.di.module.ServiceModule;

import dagger.Component;

@ServiceScope
@Component(dependencies = AppComponent.class, modules = {ServiceModule.class})
public interface ServiceComponent {

    @ContextLife("Service")
    Context getServiceContext();

    @ContextLife("Application")
    Context getApplicationContext();

}