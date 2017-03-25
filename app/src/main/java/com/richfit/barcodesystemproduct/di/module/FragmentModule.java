package com.richfit.barcodesystemproduct.di.module;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.scope.FragmentScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by monday on 2016/10/18.
 */
@Module
public class FragmentModule {
    private Fragment mFragment;

    public FragmentModule(Fragment fragment) {
        mFragment = fragment;
    }

    @Provides
    @FragmentScope
    @ContextLife("Activity")
    public Context provideContext() {
        return mFragment.getActivity();
    }

    @Provides
    @FragmentScope
    public Activity provideActivity() {
        return mFragment.getActivity();
    }

    @Provides
    @FragmentScope
    public Fragment provideFragment() {
        return mFragment;
    }
}
