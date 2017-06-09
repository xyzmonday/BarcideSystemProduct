package com.richfit.barcodesystemproduct.module.setting.imp;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.BarcodeSystemApplication;
import com.richfit.barcodesystemproduct.base.BasePresenter;
import com.richfit.barcodesystemproduct.di.component.AppComponent;
import com.richfit.barcodesystemproduct.di.component.DaggerAppComponent;
import com.richfit.barcodesystemproduct.di.module.AppModule;
import com.richfit.barcodesystemproduct.module.setting.ISettingPresenter;
import com.richfit.barcodesystemproduct.module.setting.ISettingView;
import com.richfit.common_lib.rxutils.RetryWhenNetworkException;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.data.net.api.IRequestApi;
import com.richfit.data.repository.Repository;
import com.richfit.domain.bean.LoadBasicDataWrapper;
import com.richfit.domain.bean.LoadDataTask;
import com.richfit.domain.bean.UpdateEntity;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.ResourceSubscriber;
import zlc.season.rxdownload2.RxDownload;
import zlc.season.rxdownload2.entity.DownloadStatus;

/**
 * Created by monday on 2016/11/29.
 */

public class SettingPresenterImp extends BasePresenter<ISettingView>
        implements ISettingPresenter {

    private ISettingView mView;
    private RxDownload mRxDownload;
    private int mTaskId = 0;
    private Disposable mUpdateDisposable;

    @Inject
    public SettingPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRxDownload = RxDownload.getInstance(mContext)
                .maxThread(1)
                .maxRetryCount(3);

        mTaskId = 0;
    }

    /**
     * 下载基础数据。
     * 具体的控制逻辑是:用户传递过来需要下载的基础数据的类型，
     * 调用preparePageLoad方法请求到分页数据的基本信息(如果不需要分页那么系统自动返回合适的下载信息)，
     * 根据返回的信息生成下载任务(如果分页下载的数据为0,那么下载任务为0)，
     * 下载完成后，保存到本地数据库
     *
     * @param requestParams
     */
    @Override
    public void loadAndSaveBasicData(final ArrayList<LoadBasicDataWrapper> requestParams) {
        mView = getView();
        ResourceSubscriber<Integer> subscriber =
                Flowable.fromIterable(requestParams)
                        .concatMap(param ->
                                Flowable.just(param)
                                        .zipWith(Flowable.just(mRepository.getLoadBasicDataTaskDate(param.queryType)), (loadBasicDataWrapper, queryDate) -> {
                                            loadBasicDataWrapper.queryDate = queryDate;
                                            return loadBasicDataWrapper;
                                        }).flatMap(p -> mRepository.preparePageLoad(p)))
                        .concatMap(param -> Flowable.fromIterable(addTask(param.queryType, param.queryDate, param.totalCount, param.isByPage)))
                        .concatMap(task -> mRepository.loadBasicData(task))
                        .flatMap(sourceMap -> mRepository.saveBasicData(sourceMap))
                        .doOnComplete(() -> {
                            String currentDate = UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE4);
                            ArrayList<String> queryTypes = new ArrayList<>();
                            for (LoadBasicDataWrapper param : requestParams) {
                                queryTypes.add(param.queryType);
                            }
                            mRepository.saveLoadBasicDataTaskDate(currentDate, queryTypes);
                        })
                        .retryWhen(new RetryWhenNetworkException(3, 2000))
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new ResourceSubscriber<Integer>() {

                            @Override
                            protected void onStart() {
                                super.onStart();
                                if (mView != null) {
                                    mView.onStartLoadBasicData(mTaskId);
                                }
                            }

                            @Override
                            public void onNext(Integer integer) {
                                if (mView != null && mTaskId != 0) {
                                    float percent = (integer.intValue() * 100.0F) / (mTaskId * 1.0F);
                                    mView.loadBasicDataProgress(percent);
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                mTaskId = 0;
                                if (mView != null) {
                                    mView.loadBasicDataFail(t.getMessage());
                                }
                            }

                            @Override
                            public void onComplete() {
                                mTaskId = 0;
                                if (mView != null) {
                                    mView.loadBasicDataComplete();
                                }
                            }
                        });


        addSubscriber(subscriber);
    }

    @Override
    public void getAppVersion() {
        mView = getView();
        mRepository.getAppVersion()
                .compose(TransformerHelper.io2main())
                .subscribeWith(new RxSubscriber<UpdateEntity>(mContext, "正在检查更新...") {
                    @Override
                    public void _onNext(UpdateEntity updateEntity) {
                        if (mView != null) {
                            mView.checkAppVersion(updateEntity);
                        }
                    }

                    @Override
                    public void _onNetWorkConnectError(String message) {

                    }

                    @Override
                    public void _onCommonError(String message) {
                        if (mView != null) {
                            mView.getUpdateInfoFail(message);
                        }
                    }

                    @Override
                    public void _onServerError(String code, String message) {
                        if (mView != null) {
                            mView.getUpdateInfoFail(message);
                        }
                    }

                    @Override
                    public void _onComplete() {

                    }
                });
    }

    @Override
    public void loadLatestApp(String url, String saveName, String savePath) {
        mView = getView();
        if (TextUtils.isEmpty(url) && mView != null) {
            mView.loadLatestAppFail("下载地址为空");
            return;
        }

        if (TextUtils.isEmpty(saveName) && mView != null) {
            mView.loadLatestAppFail("未获取到保存文件的名称");
            return;
        }

        //2017年06月修改下载的url，获取到服务器的下载url后需要动态的将其ip修改系统设置的url
        //获取http://后面第一次出现/字符的位置
        int indexOf = url.indexOf("/", 7);
        String currentUrl = BarcodeSystemApplication.baseUrl;
        int pos = currentUrl.indexOf("/",7);
        if(indexOf < 0 || pos < 0) {
            mView.loadLatestAppFail("服务器地址有误!");
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(currentUrl.substring(0,pos))
                .append( url.substring(indexOf));

        mUpdateDisposable = mRxDownload.download(sb.toString(), saveName, savePath)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new ResourceObserver<DownloadStatus>() {

                    @Override
                    protected void onStart() {
                        super.onStart();
                        if (mView != null) {
                            mView.prepareLoadApp();
                        }
                    }

                    @Override
                    public void onNext(DownloadStatus status) {
                        if (mView != null) {
                            mView.showLoadAppProgress(status);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mView != null) {
                            mView.loadLatestAppFail(e.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mView != null) {
                            mView.loadAppComplete();
                        }
                    }
                });
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

    @Override
    public void detachView() {
        super.detachView();
        if (mUpdateDisposable != null && !mUpdateDisposable.isDisposed()) {
            mUpdateDisposable.dispose();
        }
    }

    /**
     * 生成下载任务。按照分页下载，每一页数据下载就是一个任务。
     *
     * @param queryType
     * @param totalCount
     */
    private LinkedList<LoadDataTask> addTask(String queryType, String queryDate, int totalCount, boolean isByPage) {

        LinkedList<LoadDataTask> tasks = new LinkedList<>();
        if (!isByPage) {
            tasks.addLast(new LoadDataTask(++mTaskId, queryType, null, 0, 0, false, true));
        }
        //如果该基础数据没有获取到基础数据的条数，那么不进行下载
        if (totalCount == 0)
            return tasks;
        int count = totalCount / Global.MAX_PATCH_LENGTH;
        int residual = totalCount % Global.MAX_PATCH_LENGTH;
        int ptr = 0;
        if (count == 0) {
            // 说明数据长度小于PATCH_MAX_LENGTH，直接写入即可
            tasks.addLast(new LoadDataTask(++mTaskId, queryType, queryDate, "getPage", 1
                    , totalCount, totalCount, 1, true, true));
        } else if (count > 0) {
            for (; ptr < count; ptr++) {
                tasks.addLast(new LoadDataTask(++mTaskId, queryType, queryDate, "getPage",
                        Global.MAX_PATCH_LENGTH * ptr + 1, Global.MAX_PATCH_LENGTH * (ptr + 1)
                        , Global.MAX_PATCH_LENGTH, ptr + 1, true, ptr == 0));
            }
            if (residual > 0) {
                // 说明还有剩余的数据
                tasks.addLast(new LoadDataTask(++mTaskId, queryType, queryDate, "getPage",
                        Global.MAX_PATCH_LENGTH * ptr + 1, Global.MAX_PATCH_LENGTH * ptr + residual
                        , Global.MAX_PATCH_LENGTH, ptr + 1, true, false));
            }
        }
        return tasks;
    }
}
