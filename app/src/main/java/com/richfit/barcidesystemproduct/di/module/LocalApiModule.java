package com.richfit.barcidesystemproduct.di.module;

import android.content.Context;

import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.scope.Type;
import com.richfit.data.db.ASDao;
import com.richfit.data.db.ApprovalDao;
import com.richfit.data.db.CommonDao;
import com.richfit.data.repository.local.LocalRepositoryImp;
import com.richfit.domain.repository.ILocalRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by monday on 2016/11/9.
 */
@Module
public class LocalApiModule {

    @Type("CommonDao")
    @Provides
    @Singleton
    public ILocalRepository provideCommonDao(@ContextLife("Application") Context context) {
        return new CommonDao(context);
    }

    @Type("ApprovalDao")
    @Provides
    @Singleton
    public ILocalRepository provideApprovalDao(@ContextLife("Application") Context context) {
        return new ApprovalDao(context);
    }

    @Type("ASDao")
    @Provides
    @Singleton
    public ILocalRepository provideASDao(@ContextLife("Application") Context context) {
        return new ASDao(context);
    }

    @Provides
    @Singleton
    public ILocalRepository provideLocalDataApi(@Type("CommonDao") ILocalRepository commonDao,
                                                @Type("ApprovalDao") ILocalRepository approvalDao,
                                                @Type("ASDao") ILocalRepository asDao) {
        return new LocalRepositoryImp(commonDao, approvalDao, asDao);
    }
}
