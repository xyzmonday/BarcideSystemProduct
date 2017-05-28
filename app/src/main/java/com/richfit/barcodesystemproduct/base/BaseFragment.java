package com.richfit.barcodesystemproduct.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.annotation.CheckResult;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.richfit.barcodesystemproduct.BarcodeSystemApplication;
import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.di.component.DaggerFragmentComponent;
import com.richfit.barcodesystemproduct.di.component.FragmentComponent;
import com.richfit.barcodesystemproduct.di.module.FragmentModule;
import com.richfit.common_lib.IInterface.IFragmentState;
import com.richfit.common_lib.IInterface.IPresenter;
import com.richfit.common_lib.dialog.NetConnectErrorDialogFragment;
import com.richfit.common_lib.dialog.ShowErrorMessageDialog;
import com.richfit.common_lib.utils.CommonUtil;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.L;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.BottomMenuEntity;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.squareup.leakcanary.RefWatcher;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.pedant.SweetAlert.SweetAlertDialog;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by monday on 2016/11/10.
 */

public abstract class BaseFragment<P extends IPresenter> extends Fragment implements BaseView,
        NetConnectErrorDialogFragment.INetworkConnectListener, IFragmentState {

    public static final int HEADER_FRAGMENT_INDEX = 0x0;
    public static final int DETAIL_FRAGMENT_INDEX = 0x1;
    public static final int COLLECT_FRAGMENT_INDEX = 0x2;

    protected FragmentComponent mFragmentComponent;

    private boolean isActivityCreated;

    private Disposable mDisposable;

    protected View mView;

    @Inject
    protected P mPresenter;

    protected Activity mActivity;

    private Unbinder mUnbinder;

    protected NetConnectErrorDialogFragment mNetConnectErrorDialogFragment;

    /*抬头界面，数据明细界面，数据采集界面共享的单据数据，注意我们需要将单据数据和缓存数据隔离*/
    protected static ReferenceEntity mRefData;
    /*对于委外入库，组件界面的明细界面和数据采集界面共享的数据明细*/
    protected static List<RefDetailEntity> mRefDetail;

    /*额外控件缓存*/
    protected Map<String, View> mExtraViews;

    protected String mCompanyCode;
    protected String mModuleCode;
    protected String mBizType;
    protected String mRefType;
    protected int mFragmentType;
    /*该页签名称*/
    protected String mTabTitle;
    protected boolean mIsOpenBatchManager = Global.BATCH_FLAG;


    public static BaseFragment findFragment(FragmentManager fm, String tag, String companyCode, String moduleCode,
                                            String bizType, String refType, int fragmentType, String title, Class clazz) {
        BaseFragment fragment = (BaseFragment) fm.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = newInstance(clazz, companyCode, moduleCode, bizType, refType, fragmentType, title);
        }
        return fragment;
    }

    public static BaseFragment findFragment(FragmentManager fm, String tag, Bundle arguments, String className) {
        BaseFragment fragment = (BaseFragment) fm.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = newInstance(className, arguments);
        }
        return fragment;
    }

    private static void setFieldValue(Class clazz, Object object, String value) {
        try {
            /**
             * getDeclaredMethod*()获取的是类自身声明的所有方法，包含public、protected和private方法。
             * getMethod*()获取的是类的所有共有方法，这就包括自身的所有public方法，和从基类继承的、从接口实现的所有public方法。
             */
            Method method = clazz.getMethod("setTabTitle", String.class);
            method.setAccessible(true);
            method.invoke(object, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 生成BaseFragment的子类的实例对象
     *
     * @param clazz
     * @param moduleCode:组模块编码
     * @param bizType：子模块编码
     * @param refType：业务类型
     * @param <T>
     * @return
     */
    private static <T extends BaseFragment> T newInstance(final Class<T> clazz, String companyCode, String moduleCode,
                                                          String bizType, String refType, int fragmentType, String title) {
        T instance = null;
        try {
            instance = clazz.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString(Global.EXTRA_COMPANY_CODE_KEY, companyCode);
            bundle.putString(Global.EXTRA_MODULE_CODE_KEY, moduleCode);
            bundle.putString(Global.EXTRA_BIZ_TYPE_KEY, bizType);
            bundle.putString(Global.EXTRA_REF_TYPE_KEY, refType);
            bundle.putString(Global.EXTRA_TITLE_KEY, title);
            bundle.putInt(Global.EXTRA_FRAGMENT_TYPE_KEY, fragmentType);
            setFieldValue(clazz, instance, title);
            instance.setArguments(bundle);
            return instance;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T extends BaseFragment> T newInstance(String className, Bundle arguments) {
        T instance = null;
        try {
            final Class clazz = Class.forName(className);
            instance = (T) clazz.newInstance();
            String tabTitle = arguments.getString(Global.EXTRA_TITLE_KEY);
            setFieldValue(clazz, instance, tabTitle);
            instance.setArguments(arguments);
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mExtraViews = new HashMap<>();
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCompanyCode = bundle.getString(Global.EXTRA_COMPANY_CODE_KEY);
            mModuleCode = bundle.getString(Global.EXTRA_MODULE_CODE_KEY);
            mBizType = bundle.getString(Global.EXTRA_BIZ_TYPE_KEY);
            mRefType = bundle.getString(Global.EXTRA_REF_TYPE_KEY);
            mTabTitle = bundle.getString(Global.EXTRA_TITLE_KEY);
            mFragmentType = bundle.getInt(Global.EXTRA_FRAGMENT_TYPE_KEY);
        }
        initVariable(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(getContentId(), container, false);
        mUnbinder = ButterKnife.bind(this, mView);
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //2017年5月优化成异步加载，解决注入慢的卡顿。也就是先渲染页面在延迟加载数据
        Observable.just("start")
                .map(areaName -> {
                    FragmentComponent component = DaggerFragmentComponent.builder()
                            .fragmentModule(new FragmentModule(this))
                            .appComponent(BarcodeSystemApplication.getAppComponent())
                            .build();
                    return component;
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<FragmentComponent>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposable = d;
                    }

                    @Override
                    public void onNext(FragmentComponent value) {
                        mFragmentComponent = value;
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        initInjector();
                        if (mPresenter != null) {
                            mPresenter.attachView(BaseFragment.this);
                        }
                        initView();
                        initEvent();
                        initData();
                        isActivityCreated = true;
                    }
                });
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 此方法目前仅适用于标示ViewPager中的Fragment是否真实可见.
     * 第一次执行时候，该方法可能早与onCreate方法，此时不适合做业务
     * 因为我们需要在onCreate方法初始化相关变量，所以设置isCreated标志
     * 位。如果isVisibleToUser为true，说明此时该页面真正的对用户可见。
     * 那么可以执行相关的业务。
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isActivityCreated) {
            return;
        }
        if (isVisibleToUser) {
            initDataLazily();
        } else {
            _onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mUnbinder != Unbinder.EMPTY) mUnbinder.unbind();

        if (mDisposable != null && !mDisposable.isDisposed())
            mDisposable.dispose();
        if (mPresenter != null) {
            mPresenter.detachView();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = BarcodeSystemApplication.getRefWatcher();
        if (refWatcher != null) {
            refWatcher.watch(this);
        }
    }

    //获取布局文件
    protected abstract int getContentId();

    public abstract void initInjector();

    protected void showMessage(String message) {
        Snackbar.make(mView, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * 获取view上的数据（该view只能是textView或者其子类）
     *
     * @param view
     * @return
     */
    @CheckResult
    protected String getString(View view) {
        if (view != null && TextView.class.isInstance(view)) {
            TextView tv = (TextView) view;
            return CommonUtil.Obj2String(tv.getText());
        }
        return "";
    }

    protected int getInteger(int resId) {
        if (resId != 0) {
            return mActivity.getResources().getInteger(resId);
        }
        return -1;
    }

    /**
     * 通过物资条码信息和批次信息匹配
     * 这里的逻辑是：如果启用了批次管理那么明细里面可能有也可能没有批次，如果没有启用批次管理，那么
     * 明细一定没有批次
     *
     * @return:与该物料匹配的行号集合
     */
    protected Flowable<ArrayList<String>> matchMaterialInfo(final String materialNum, final String batchFlag) {
        if (mRefData == null || mRefData.billDetailList == null ||
                mRefData.billDetailList.size() == 0 || TextUtils.isEmpty(materialNum)) {
            return Flowable.error(new Throwable("请先获取单据信息"));
        }
        ArrayList<String> lineNums = new ArrayList<>();
        List<RefDetailEntity> list = mRefData.billDetailList;
        for (RefDetailEntity entity : list) {
            if (mIsOpenBatchManager) {
                final String lineNum = entity.lineNum;
                //如果打开了批次，那么在看明细中是否有批次
                if (!TextUtils.isEmpty(entity.batchFlag) && !TextUtils.isEmpty(batchFlag)) {
                    if (materialNum.equalsIgnoreCase(entity.materialNum) &&
                            batchFlag.equalsIgnoreCase(entity.batchFlag) &&
                            !TextUtils.isEmpty(lineNum))

                        lineNums.add(lineNum);
                } else {
                    if (materialNum.equalsIgnoreCase(entity.materialNum) &&
                            !TextUtils.isEmpty(lineNum))
                        lineNums.add(lineNum);
                }
            } else {
                final String lineNum = entity.lineNum;
                //如果明细中没有打开了批次管理,那么只匹配物料编码
                if (materialNum.equalsIgnoreCase(entity.materialNum) && !TextUtils.isEmpty(lineNum))
                    lineNums.add(entity.lineNum);

            }
        }
        if (lineNums.size() == 0) {
            return Flowable.error(new Throwable("未获取到匹配的物料"));
        }
        return Flowable.just(lineNums);
    }

    /**
     * 通过单据行的行号得到该行在单据明细列表中的位置
     *
     * @param lineNum:单据行号
     * @return 返回该行号对应的行明细在明细列表的索引
     */
    protected int getIndexByLineNum(String lineNum) {
        int index = -1;
        if (TextUtils.isEmpty(lineNum))
            return index;

        if (mRefData == null || mRefData.billDetailList == null
                || mRefData.billDetailList.size() == 0)
            return index;

        for (RefDetailEntity detailEntity : mRefData.billDetailList) {
            index++;
            if (lineNum.equalsIgnoreCase(detailEntity.lineNum))
                break;

        }
        return index;
    }

    /**
     * 获取行明细
     *
     * @param lineNum:单据行号
     * @return
     */
    protected RefDetailEntity getLineData(String lineNum) {
        int lineIndex = getIndexByLineNum(lineNum);
        if (lineIndex < 0) {
            mRefData.billDetailList.get(0);
        }
        return mRefData.billDetailList.get(lineIndex);
    }

    /**
     * 掩藏输入法
     */
    protected void hideKeyboard(View view) {
        if (view == null)
            view = mActivity.getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 显示网络错误重试对话框
     *
     * @param action：重试的事件类型
     */
    protected void showNetConnectErrorDialog(String action) {
        AppCompatActivity activity = (AppCompatActivity) mActivity;
        FragmentManager supportFragmentManager = activity.getSupportFragmentManager();
        mNetConnectErrorDialogFragment = (NetConnectErrorDialogFragment) supportFragmentManager.findFragmentByTag("NETWORK_CONNECT_ERROR_TAG");
        if (mNetConnectErrorDialogFragment == null) {
            mNetConnectErrorDialogFragment = NetConnectErrorDialogFragment.newInstance(action);
            mNetConnectErrorDialogFragment.setINetworkConnectListener(this);
        }
        mNetConnectErrorDialogFragment.show(supportFragmentManager, Global.NETWORK_CONNECT_ERROR_TAG);
    }

    @Override
    public void retry(String action) {
        if (mNetConnectErrorDialogFragment != null) {
            mNetConnectErrorDialogFragment.dismiss();
        }
    }

    @Override
    public void networkConnectError(String retryAction) {
        showNetConnectErrorDialog(retryAction);
    }

    protected boolean isEmpty(String str) {
        return TextUtils.isEmpty(str);
    }

    protected void showSuccessDialog(String message) {
        new SweetAlertDialog(mActivity, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("温馨提示")
                .setContentText(message)
                .show();
    }

    protected void showFailDialog(String message) {
        new SweetAlertDialog(mActivity, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("温馨提示")
                .setContentText(message)
                .show();
    }

    protected void showErrorDialog(String message) {
        String[] errors = message.split("______");
        //注意这里使用多态性质，父类AppCompatActivity中的FragmentManager
        AppCompatActivity activity = (AppCompatActivity) mActivity;
        FragmentManager fm = activity.getSupportFragmentManager();
        ShowErrorMessageDialog dialog = ShowErrorMessageDialog.newInstance(errors);
        dialog.show(fm, "nms_show_error_messages");
    }


    protected void showErrorDialog(String[] messages) {
        AppCompatActivity activity = (AppCompatActivity) mActivity;
        FragmentManager fm = activity.getSupportFragmentManager();
        ShowErrorMessageDialog dialog = ShowErrorMessageDialog.newInstance(messages);
        dialog.show(fm, "nms_show_error_messages");
    }

    protected void setAutoCompleteConfig(AutoCompleteTextView autoComplete) {
        autoComplete.setThreshold(1);
        autoComplete.setDropDownWidth(autoComplete.getWidth() * 2);
        autoComplete.isPopupShowing();
    }

    /**
     * 初始化供应商下拉列表的配置
     */
    protected void showAutoCompleteConfig(AutoCompleteTextView autoComplete) {
        if (autoComplete.getAdapter() != null) {
            autoComplete.showDropDown();
        }
    }

    public String getTabTitle() {
        return mTabTitle;
    }

    public int getFragmentType() {
        return mFragmentType;
    }

    public void setTabTitle(String title) {
        this.mTabTitle = title;
    }

    protected List<BottomMenuEntity> provideDefaultBottomMenu() {
        ArrayList<BottomMenuEntity> menus = new ArrayList<>();
        BottomMenuEntity menu = new BottomMenuEntity();
        menu.menuName = "过账";
        menu.menuImageRes = R.mipmap.icon_transfer;
        menus.add(menu);

        menu = new BottomMenuEntity();
        menu.menuName = "上架";
        menu.menuImageRes = R.mipmap.icon_data_submit;
        menus.add(menu);

        menu = new BottomMenuEntity();
        menu.menuName = "下架";
        menu.menuImageRes = R.mipmap.icon_down_location;
        menus.add(menu);

        menu = new BottomMenuEntity();
        menu.menuName = "记账更改";
        menu.menuImageRes = R.mipmap.icon_detail_transfer;
        menus.add(menu);
        return menus;
    }

    //初始化相关变量
    protected void initVariable(@Nullable Bundle savedInstanceState) {

    }

    //初始化view
    protected void initView() {
    }

    //注册所有的监听事件,可以不实现
    public void initEvent() {
    }

    /**
     * 初始化数据, 可以不实现.该方法在onResume方法中调用，说明在大部分的时候
     * 该方法只会调用一次，也就是我们需要初始化一下静态的数据。比如单据类型等。
     */
    public void initData() {

    }

    //赖加载数据。该方法在Fragment可见的时候调用。可以动态的加载数据
    public void initDataLazily() {

    }

    //该方法在fragment不可见的时候调用，相当于onPause方法，用户保存抬头界面相关数据。
    public void _onPause() {
        if (mView != null)
            hideKeyboard(mView);
    }

    protected List<String> getStringArray(@ArrayRes int id) {
        return Arrays.asList(getResources().getStringArray(id));
    }

    @Override
    public boolean checkDataBeforeOperationOnHeader() {
        return true;
    }

    @Override
    public void operationOnHeader(final String companyCode) {

    }

    @Override
    public boolean checkDataBeforeOperationOnDetail() {
        return true;
    }

    @Override
    public void showOperationMenuOnDetail(String companyCode) {

    }

    @Override
    public boolean checkCollectedDataBeforeSave() {
        return true;
    }

    @Override
    public void saveCollectedData() {

    }

    @Override
    public void showOperationMenuOnCollection(String companyCode) {

    }

    @Override
    public void handleBarCodeScanResult(String type, String[] list) {

    }

    /**
     * BaseFragment实现isNeedShowFloatingButton该方法，默认显示FloatingButton
     *
     * @return
     */
    @Override
    public boolean isNeedShowFloatingButton() {
        return true;
    }

    /**
     * 设置view的可见性
     */
    protected void setVisibility(int visibility, View... views) {
        if (views == null || views.length == 0)
            return;
        for (View view : views) {
            view.setVisibility(visibility);
        }
    }


    /**
     * 清除公共空控件的数据
     */
    protected void clearCommonUI(View... views) {
        for (View view : views) {
            if (view == null)
                continue;
            if (CheckBox.class.isInstance(view)) {
                CheckBox cb = (CheckBox) view;
                cb.setChecked(false);
            } else if (TextView.class.isInstance(view)) {
                TextView tv = (TextView) view;
                tv.setText("");
            } else if (Spinner.class.isInstance(view)) {
                Spinner sp = (Spinner) view;
                if (sp.getAdapter() != null)
                    sp.setSelection(0);
            }
        }
    }

    /**
     * 通过单据单位计算库存数量
     * @param quantity
     * @param recordUnit
     * @param unitRate
     * @return
     */
    protected String calQuantityByUnitRate(String quantity,String recordUnit,float unitRate) {
        if(TextUtils.isEmpty(quantity)) {
            return "0";
        }
        float q = UiUtil.convertToFloat(quantity,0.0f);
        if(!TextUtils.isEmpty(recordUnit) && unitRate != 0) {
            q  /= unitRate;
        }
        L.e("q = " + q);
        DecimalFormat df = new DecimalFormat("#.###");
        return df.format(q);
    }
}
