package com.richfit.barcodesystemproduct.module.login;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.BarcodeSystemApplication;
import com.richfit.barcodesystemproduct.base.BasePresenter;
import com.richfit.barcodesystemproduct.crash.CrashLogUtil;
import com.richfit.barcodesystemproduct.di.component.AppComponent;
import com.richfit.barcodesystemproduct.di.component.DaggerAppComponent;
import com.richfit.barcodesystemproduct.di.module.AppModule;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.L;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.data.net.api.IRequestApi;
import com.richfit.data.repository.Repository;
import com.richfit.domain.bean.ResultEntity;
import com.richfit.domain.bean.UserEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by monday on 2016/10/27.
 */

public class LoginPresenterImp extends BasePresenter<LoginContract.View>
        implements LoginContract.Presenter {

    LoginContract.View mView;

    @Override
    protected void onStart() {
        mView = getView();
    }

    @Inject
    public LoginPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void login(final String userName, final String password) {
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
            mView.loginFail("用户名或者密码为空");
            return;
        }
        ResourceSubscriber<UserEntity> subscriber =
                mRepository.login(userName, password)
                        .doOnNext(userEntity -> mRepository.saveUserInfo(userEntity))
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new RxSubscriber<UserEntity>(mContext, "正在登陆...") {
                            @Override
                            public void _onNext(UserEntity userInfo) {
                                Global.LOGIN_ID = userInfo.loginId;
                                Global.USER_ID = userInfo.userId;
                                Global.USER_NAME = userInfo.userName;
                                Global.COMPANY_ID = userInfo.companyId;
                                Global.COMPANY_CODE = userInfo.companyCode;
                                Global.AUTH_ORG = userInfo.authOrgs;
                                Global.BATCHMANAGERSTATUS = userInfo.batchFlag;
                                Global.WMFLAG = userInfo.wmFlag;
                            }

                            @Override
                            public void _onNetWorkConnectError(String message) {
                                if (mView != null) {
                                    mView.networkConnectError(Global.RETRY_LOGIN_ACTION);
                                }
                            }

                            @Override
                            public void _onCommonError(String message) {
                                if (mView != null) {
                                    mView.loginFail(message);
                                }
                            }

                            @Override
                            public void _onServerError(String code, String msg) {
                                if (mView != null) {
                                    mView.loginFail(msg);
                                }
                            }

                            @Override
                            public void _onComplete() {
                                if (mView != null) {
                                    mView.toHome();
                                }
                            }
                        });

        addSubscriber(subscriber);
    }

    @Override
    public void readUserInfos() {
        mView = getView();
        mRepository.readUserInfo("", "")
                .filter(list -> list != null && list.size() > 0)
                .compose(TransformerHelper.io2main())
                .subscribeWith(new ResourceSubscriber<ArrayList<String>>() {
                    @Override
                    public void onNext(ArrayList<String> list) {
                        if (mView != null) {
                            mView.showUserInfos(list);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (mView != null) {
                            mView.loadUserInfosFail(t.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void uploadCrashLogFiles() {
        mView = getView();
        final File crashLogFileDir = CrashLogUtil.getCrashLogFileDir(mContext.getApplicationContext());
        ResourceSubscriber<String> subscriber = Flowable.just(crashLogFileDir)
                .map((Function<File, List<ResultEntity>>) dir -> {
                    final ArrayList<ResultEntity> results = new ArrayList<>();
                    if (dir == null) {
                        return results;
                    }

                    File[] files = dir.listFiles(crashFile -> crashFile != null && crashFile.getName().endsWith(".txt"));

                    if (files == null || files.length == 0) {
                        return results;
                    }
                    for (File file : files) {
                        if (file == null || file.length() == 0)
                            continue;
                        ResultEntity result = new ResultEntity();
                        result.imagePath = file.getAbsolutePath();
                        result.transFileToServer = "DEBUG";
                        results.add(result);
                    }
                    return results;
                })
                .filter(res -> res != null && res.size() > 0)
                .flatMap(res -> mRepository.uploadMultiFiles(res))
                .doOnComplete(() -> CrashLogUtil.deleteCashLogDir(crashLogFileDir))
                .compose(TransformerHelper.io2main())
                .subscribeWith(new ResourceSubscriber<String>() {
                    @Override
                    public void onNext(String s) {
                        L.e("s = " + s);
                    }

                    @Override
                    public void onError(Throwable t) {
                        L.e("error = " + t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        L.e("奔溃日志上传完毕");
                    }
                });
        addSubscriber(subscriber);
    }


    @Override
    public void getMappingInfo() {
        mView = getView();
        ResourceSubscriber<String> subscriber =
                mRepository.getMappingInfo()
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new RxSubscriber<String>(mContext, "正在检查是否已经注册...") {
                            @Override
                            public void _onNext(String s) {
//                                if(mView != null) {
//                                    mView.updateDbSource(s);
//                                }
                            }

                            @Override
                            public void _onNetWorkConnectError(String message) {
                                if (mView != null) {
                                    mView.networkConnectError(Global.RETRY_REGISTER_ACTION);
                                }
                            }

                            @Override
                            public void _onCommonError(String message) {
                                if (mView != null) {
                                    mView.unRegister(message);
                                }
                            }

                            @Override
                            public void _onServerError(String code, String message) {
                                if (mView != null) {
                                    mView.unRegister(message);
                                }
                            }

                            @Override
                            public void _onComplete() {
                                if (mView != null) {
                                    mView.registered();
                                }
                            }
                        });
        addSubscriber(subscriber);
    }


    @Override
    public void setupUrl(String url) {
        mView = getView();
        Repository repository = BarcodeSystemApplication.getAppComponent().getRepository();
        IRequestApi requstApi = BarcodeSystemApplication.getAppComponent().getRequestApi();
        requstApi = null;
        repository = null;
        Flowable.just(url)
                .map(baseUrl -> {
                    AppComponent appComponent = DaggerAppComponent.builder()
                            .appModule(new AppModule(BarcodeSystemApplication.getAppContext(), baseUrl))
                            .build();
                    BarcodeSystemApplication.baseUrl = baseUrl;
                    SPrefUtil.saveData("base_url", baseUrl);
                    SPrefUtil.saveData(Global.IS_APP_FIRST_KEY, true);
                    SPrefUtil.saveData(Global.IS_INITED_FRAGMENT_CONFIG_KEY, false);
                    return appComponent;
                })
                .compose(TransformerHelper.io2main())
                .subscribe(new ResourceSubscriber<AppComponent>() {
                    @Override
                    public void onNext(AppComponent appComponent) {
                        BarcodeSystemApplication.setAppComponent(appComponent);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {
                        if (mView != null) {
                            mView.setupUrlComplete();
                        }
                    }
                });
    }

}
