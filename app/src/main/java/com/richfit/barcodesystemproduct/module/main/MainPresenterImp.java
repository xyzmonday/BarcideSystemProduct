package com.richfit.barcodesystemproduct.module.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.adapter.MainPagerViewAdapter;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.base.BasePresenter;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.UploadMsgEntity;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by monday on 2016/11/10.
 */

public class MainPresenterImp extends BasePresenter<MainContract.View>
        implements MainContract.Presenter {

    protected MainContract.View mView;

    @Inject
    public MainPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void setupMainContent(FragmentManager fragmentManager, final Bundle bundle,
                                 int currentPageIndex,int mode) {

        mView = getView();
        final String bizType = bundle.getString(Global.EXTRA_BIZ_TYPE_KEY);
        if (TextUtils.isEmpty(bizType)) {
            mView.setupMainContentFail("业务类型为空!");
            return;
        }
        final String refType = bundle.getString(Global.EXTRA_REF_TYPE_KEY);
        final String companyCode = bundle.getString(Global.EXTRA_COMPANY_CODE_KEY);
        final String moduleCode = bundle.getString(Global.EXTRA_MODULE_CODE_KEY);
        final String refLineNum = bundle.getString(Global.EXTRA_REF_LINE_NUM_KEY);
        final UploadMsgEntity uploadMsgEntity = bundle.getParcelable(Global.EXTRA_UPLOAD_MSG_KEY);

        ResourceSubscriber<MainPagerViewAdapter> subscriber =
                mRepository.readBizFragmentConfig(bizType, refType, 1, mode)
                        .filter(bizFragmentConfigs -> bizFragmentConfigs != null && bizFragmentConfigs.size() > 0)
                        .flatMap(bizFragmentConfigs -> Flowable.fromIterable(bizFragmentConfigs))
                        .map(config -> {
                            Bundle argument  = new Bundle();
                            argument.putString(Global.EXTRA_COMPANY_CODE_KEY,companyCode);
                            argument.putString(Global.EXTRA_MODULE_CODE_KEY,moduleCode);
                            argument.putString(Global.EXTRA_BIZ_TYPE_KEY,config.bizType);
                            argument.putString(Global.EXTRA_REF_TYPE_KEY,config.refType);
                            argument.putString(Global.EXTRA_TITLE_KEY, config.tabTitle);
                            argument.putInt(Global.EXTRA_FRAGMENT_TYPE_KEY, config.fragmentType);
                            //外委入库组件使用该字段
                            argument.putString(Global.EXTRA_REF_LINE_NUM_KEY,refLineNum);
                            if(uploadMsgEntity != null)
                            argument.putParcelable(Global.EXTRA_UPLOAD_MSG_KEY,uploadMsgEntity);
                            return BaseFragment.findFragment(fragmentManager, config.fragmentTag, argument, config.className);
                        })
                        .buffer(3)
                        .map(fragments -> {
                            MainPagerViewAdapter adapter = new MainPagerViewAdapter(fragmentManager, fragments);
                            return adapter;
                        })
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new ResourceSubscriber<MainPagerViewAdapter>() {
                            @Override
                            public void onNext(MainPagerViewAdapter adapter) {
                                if (mView != null) {
                                    mView.showMainContent(adapter, currentPageIndex);
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                if(mView != null) {
                                    mView.setupMainContentFail(t.getMessage());
                                }
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
        addSubscriber(subscriber);
    }


}
