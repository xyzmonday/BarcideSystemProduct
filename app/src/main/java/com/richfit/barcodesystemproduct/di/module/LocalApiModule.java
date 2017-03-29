package com.richfit.barcodesystemproduct.di.module;

import android.content.Context;

import com.richfit.common_lib.scope.ContextLife;
import com.richfit.data.db.BasicServiceDao;
import com.richfit.data.db.BusinessServiceDao;
import com.richfit.data.db.CheckServiceDao;
import com.richfit.data.db.InspectionServiceDao;
import com.richfit.data.db.ReferenceServiceDao;
import com.richfit.data.db.TransferServiceDao;
import com.richfit.data.repository.local.LocalRepositoryImp;
import com.richfit.domain.repository.IBasicServiceDao;
import com.richfit.domain.repository.IBusinessService;
import com.richfit.domain.repository.ICheckServiceDao;
import com.richfit.domain.repository.IInspectionServiceDao;
import com.richfit.domain.repository.ILocalRepository;
import com.richfit.domain.repository.IReferenceServiceDao;
import com.richfit.domain.repository.ITransferServiceDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by monday on 2016/11/9.
 */
@Module
public class LocalApiModule {

    @Provides
    @Singleton
    public IBasicServiceDao provideBasicServiceDao(@ContextLife("Application") Context context) {
        return new BasicServiceDao(context);
    }

    @Provides
    @Singleton
    public IInspectionServiceDao provideInspectionServiceDao(@ContextLife("Application") Context context) {
        return new InspectionServiceDao(context);
    }

    @Provides
    @Singleton
    public IBusinessService provideBusinessServiceDao(@ContextLife("Application") Context context) {
        return new BusinessServiceDao(context);
    }

    @Provides
    @Singleton
    public IReferenceServiceDao provideReferenceServiceDao(@ContextLife("Application") Context context) {
        return new ReferenceServiceDao(context);
    }

    @Provides
    @Singleton
    public ICheckServiceDao provideCheckServiceDao(@ContextLife("Application") Context context) {
        return new CheckServiceDao(context);
    }

    @Provides
    @Singleton
    public ITransferServiceDao provideTransferServiceDao(@ContextLife("Application") Context context) {
        return new TransferServiceDao(context);
    }


    @Provides
    @Singleton
    public ILocalRepository provideLocalDataApi(IBasicServiceDao basicServiceDao,
                                                IInspectionServiceDao inspectionServiceDao,
                                                IBusinessService businessServiceDao,
                                                IReferenceServiceDao referenceServiceDao,
                                                ITransferServiceDao transferServiceDao,
                                                ICheckServiceDao checkServiceDao) {
        return new LocalRepositoryImp(basicServiceDao, inspectionServiceDao, businessServiceDao,
                referenceServiceDao, transferServiceDao, checkServiceDao);
    }
}
