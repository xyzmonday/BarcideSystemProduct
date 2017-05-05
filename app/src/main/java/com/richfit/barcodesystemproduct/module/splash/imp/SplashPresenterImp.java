package com.richfit.barcodesystemproduct.module.splash.imp;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.BarcodeSystemApplication;
import com.richfit.barcodesystemproduct.base.BasePresenter;
import com.richfit.barcodesystemproduct.module.splash.ISplashPresenter;
import com.richfit.barcodesystemproduct.module.splash.ISplashView;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.data.db.BCSSQLiteHelper;
import com.richfit.domain.bean.LoadBasicDataWrapper;
import com.richfit.domain.bean.LoadDataTask;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.ResourceSubscriber;
import zlc.season.rxdownload2.RxDownload;
import zlc.season.rxdownload2.entity.DownloadStatus;

/**
 * Created by monday on 2016/12/2.
 */

public class SplashPresenterImp extends BasePresenter<ISplashView>
        implements ISplashPresenter {

    ISplashView mView;
    private int mTaskId = 0;
    private RxDownload mRxDownload;

    @Inject
    public SplashPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void onStart() {
        mTaskId = 0;
        mRxDownload = RxDownload.getInstance(mContext)
                .maxThread(1)
                .maxRetryCount(3);
    }


    @Override
    public void register() {
        mView = getView();
        ResourceSubscriber<String> subscriber =
                mRepository.getMappingInfo()
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new RxSubscriber<String>(mContext, "正在检查是否已经注册...") {
                            @Override
                            public void _onNext(String s) {

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
    public void syncDate() {
//        mView = getView();
//        addSubscriber(mRepository.syncDate()
//                .compose(TransformerHelper.io2main())
//                .subscribeWith(new ResourceSubscriber<String>() {
//                    @Override
//                    public void onNext(String s) {
//                        if (mView != null) {
//                            mView.syncDateSuccess(s);
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable t) {
//                        if (mView != null) {
//                            mView.syncDateFail(t.getMessage());
//                        }
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        if (mView != null) {
//                            mView.syncDateComplete();
//                        }
//                    }
//                }));
    }

    /**
     * 下载基础数据的入口。
     * 注意这里由于系统必须去下载两种类型的ZZ的基础数据，
     * 然而二级单位的ZZ基础数据又比较特别(仅仅针对某些地区公司存在)，
     * 所以需要拦击错误，不管有没有都必须执行完所有的任务。
     *
     * @param requestParams
     */
    @Override
    public void loadAndSaveBasicData(final ArrayList<LoadBasicDataWrapper> requestParams) {
        mView = getView();
        ResourceSubscriber<Integer> subscriber = Flowable.fromIterable(requestParams)
                .concatMap(param ->
                        Flowable.just(param)
                                .zipWith(Flowable.just(mRepository.getLoadBasicDataTaskDate(param.queryType)), (loadBasicDataWrapper, queryDate) -> {
                                    loadBasicDataWrapper.queryDate = queryDate;
                                    return loadBasicDataWrapper;
                                }).flatMap(p -> mRepository.preparePageLoad(p)))
                .concatMap(param -> Flowable.fromIterable(addTask(param.queryType, param.queryDate, param.totalCount, param.isByPage)))
                .concatMapDelayError(task -> mRepository.loadBasicData(task))
                .flatMap(sourceMap -> mRepository.saveBasicData(sourceMap))
                .doOnComplete(() -> {
                    String currentDate = UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE4);
                    ArrayList<String> queryTypes = new ArrayList<>();
                    for (LoadBasicDataWrapper param : requestParams) {
                        queryTypes.add(param.queryType);
                    }
                    mRepository.saveLoadBasicDataTaskDate(currentDate, queryTypes);
                })
                .compose(TransformerHelper.io2main())
                .subscribeWith(new RxSubscriber<Integer>(mContext, "正在同步基础数据...") {
                    @Override
                    public void _onNext(Integer integer) {

                    }

                    @Override
                    public void _onNetWorkConnectError(String message) {
                        //网络超时
                        if (mView != null) {
                            mView.networkConnectError(Global.RETRY_SYNC_BASIC_DATA_ACTION);
                        }
                    }

                    @Override
                    public void _onCommonError(String message) {
                        if (mView != null) {
                            mTaskId = 0;
                            mView.syncDataError(message);
                        }
                    }

                    @Override
                    public void _onServerError(String code, String message) {
                        if (mView != null) {
                            mTaskId = 0;
                            mView.syncDataError(message);
                        }
                    }

                    @Override
                    public void _onComplete() {
                        if (mView != null) {
                            mTaskId = 0;
                            mView.toLogin();
                        }
                    }
                });
        addSubscriber(subscriber);
    }

    @Override
    public void downloadInitialDB() {
        mView = getView();
        if (SPrefUtil.mSPref == null) {
            SPrefUtil.initSharePreference(mContext.getApplicationContext());
        }
        boolean isAppFist = (boolean) SPrefUtil.getData(Global.IS_APP_FIRST_KEY, true);
        if (!isAppFist) {
            //如果不是第一次启动,那么直接同步基础数据
            mView.downDBComplete();
            return;
        }
        //如果是第一次启动那么下载数据库
        if (TextUtils.isEmpty(Global.MAC_ADDRESS)) {
            Global.MAC_ADDRESS = UiUtil.getMacAddress();
        }
        final String dbName = BCSSQLiteHelper.DB_NAME;
        String parent = mContext.getDatabasePath(dbName).getParent();
        File file = new File(parent);
        if (!file.exists())
            file.mkdir();
        final String savePath = file.getAbsolutePath();
        final String url = BarcodeSystemApplication.baseUrl + "/downloadInitialDB?macAddress=" + Global.MAC_ADDRESS;
        ResourceObserver<DownloadStatus> observer = mRxDownload.download(url, dbName, savePath)
                .doOnComplete(() -> SPrefUtil.saveData(Global.IS_APP_FIRST_KEY, false))
                .doOnComplete(() -> saveLoadBasicDataTaskDate())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new ResourceObserver<DownloadStatus>() {
                    @Override
                    public void onNext(DownloadStatus value) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mView != null) {
                            mView.downDBFail(e.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mView != null) {
                            mView.downDBComplete();
                        }
                    }
                });
        addSubscriber(observer);
    }

    private void saveLoadBasicDataTaskDate() {
        final String currentDate = UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE4);
        ArrayList<String> queryTypes = new ArrayList<>();
        queryTypes.add("WL");
        queryTypes.add("CW");
        queryTypes.add("CC");
        queryTypes.add("XM");
        mRepository.saveLoadBasicDataTaskDate(currentDate, queryTypes);
    }

    /**
     * 生成下载任务。按照分页下载，每一页数据下载就是一个任务
     *
     * @param queryType
     * @param totalCount
     */
    private LinkedList<LoadDataTask> addTask(String queryType, String queryDate, int totalCount, boolean isByPage) {

        LinkedList<LoadDataTask> tasks = new LinkedList<>();

        if (!isByPage) {
            tasks.addLast(new LoadDataTask(++mTaskId, queryType, null, 0, 0, false, true));
        }

        if (totalCount == 0)
            return tasks;
        int count = totalCount / Global.MAX_PATCH_LENGTH;
        int residual = totalCount % Global.MAX_PATCH_LENGTH;
        int ptr = 0;
        if (count == 0) {
            // 说明数据长度小于PATCH_MAX_LENGTH，直接写入即可
            tasks.addLast(new LoadDataTask(++mTaskId, queryType, queryDate, "queryPage", 1
                    , totalCount, ptr, Global.MAX_PATCH_LENGTH, true, true));
        } else if (count > 0) {
            for (; ptr < count; ptr++) {
                tasks.addLast(new LoadDataTask(++mTaskId, queryType, queryDate, "queryPage",
                        Global.MAX_PATCH_LENGTH * ptr + 1, Global.MAX_PATCH_LENGTH * (ptr + 1),
                        ptr, Global.MAX_PATCH_LENGTH, true, ptr == 0 ? true : false));
            }
            if (residual > 0) {
                // 说明还有剩余的数据
                tasks.addLast(new LoadDataTask(++mTaskId, queryType, queryDate, "queryPage",
                        Global.MAX_PATCH_LENGTH * ptr + 1, Global.MAX_PATCH_LENGTH * ptr + residual,
                        ptr, Global.MAX_PATCH_LENGTH, true, false));
            }
        }
        return tasks;
    }
}
