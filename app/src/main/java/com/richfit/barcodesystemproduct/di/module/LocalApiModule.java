package com.richfit.barcodesystemproduct.di.module;

import android.content.Context;

import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.scope.Type;
import com.richfit.data.db.ASDao;
import com.richfit.data.db.ApprovalDao;
import com.richfit.data.db.CommonDao;
import com.richfit.data.repository.local.LocalRepositoryImp;
import com.richfit.domain.repository.ILocalDataDao;
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
    public ILocalDataDao provideCommonDao(@ContextLife("Application") Context context) {
        return new CommonDao(context);
    }

    @Type("ApprovalDao")
    @Provides
    @Singleton
    public ILocalDataDao provideApprovalDao(@ContextLife("Application") Context context) {
        return new ApprovalDao(context);
    }

    @Type("ASDao")
    @Provides
    @Singleton
    public ILocalDataDao provideASDao(@ContextLife("Application") Context context) {
        return new ASDao(context);
    }

    @Provides
    @Singleton
    public ILocalRepository provideLocalDataApi(@Type("CommonDao") ILocalDataDao commonDao,
                                                @Type("ApprovalDao") ILocalDataDao approvalDao,
                                                @Type("ASDao") ILocalDataDao asDao) {
        return new LocalRepositoryImp(commonDao, approvalDao, asDao);
    }
}
