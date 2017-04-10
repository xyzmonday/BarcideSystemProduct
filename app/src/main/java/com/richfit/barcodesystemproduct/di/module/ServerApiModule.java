package com.richfit.barcodesystemproduct.di.module;


import android.content.Context;

import com.richfit.common_lib.scope.ContextLife;
import com.richfit.data.net.api.IRequestApi;
import com.richfit.data.net.http.RetrofitModule;
import com.richfit.data.repository.server.ServerRepositoryImp;
import com.richfit.domain.repository.IServerRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by monday on 2016/11/9.
 */
@Module
public class ServerApiModule {

    @Provides
    @Singleton
    public IRequestApi provideRequestApi(@ContextLife("Application") Context context,String baseUrl) {
        return RetrofitModule.getRequestApi(context,baseUrl);
    }

    @Provides
    @Singleton
    public IServerRepository provideServerDataApi(IRequestApi requestApi) {
        return new ServerRepositoryImp(requestApi);
    }
}
