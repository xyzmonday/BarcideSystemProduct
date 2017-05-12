package com.richfit.barcodesystemproduct.module.edit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.base.BaseScannerActivity;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.BizFragmentConfig;

import butterknife.OnClick;

/**
 * 修改页面基类
 * Created by monday on 2016/11/19.
 */

public class EditActivity extends BaseScannerActivity<EditPresenterImp> implements IEditContract.View {

    protected FragmentManager mFragmentManager;
    protected BaseFragment mFragment = null;
    protected String mTitle;
    protected String mCompanyCode;
    protected String mBizType;
    protected String mRefType;

    @Override
    protected int getContentId() {
        return R.layout.activity_edit;
    }

    @Override
    public void initInjector() {
        mActivityComponent.inject(this);
    }

    @Override
    public void initVariables() {
        mFragment = null;
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                mTitle = bundle.getString(Global.EXTRA_TITLE_KEY);
                mCompanyCode = bundle.getString(Global.EXTRA_COMPANY_CODE_KEY);
                mBizType = bundle.getString(Global.EXTRA_BIZ_TYPE_KEY);
                mRefType = bundle.getString(Global.EXTRA_REF_TYPE_KEY);
            }
        }
    }

    @Override
    public void initViews() {
        setupToolBar(R.id.toolbar, R.id.toolbar_title, mTitle);
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        mPresenter.setupEditFragment(mBizType, mRefType, getResources().getInteger(R.integer.edit_fragment_type));
    }

    @OnClick(R.id.floating_button)
    public void onClick(View v) {
        if (mFragment != null) {
            mFragment.showOperationMenuOnCollection(mCompanyCode);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showEditFragment(BizFragmentConfig fragmentConfig) {
        final String fragmentTag = fragmentConfig.fragmentTag;
        final String className = fragmentConfig.className;
        if (TextUtils.isEmpty(fragmentTag) || TextUtils.isEmpty(className)) {
            showMessage("不能初始化修改界面");
            return;
        }
        mFragment = BaseFragment.findFragment(mFragmentManager, fragmentTag, getIntent().getExtras(), className);
        if (mFragment != null)
            mFragmentManager.beginTransaction().replace(R.id.edit_content, mFragment, fragmentTag).commit();
    }

    @Override
    public void initEditFragmentFail(String message) {
        showMessage("初始化修改界面错误" + message);
    }

    @Override
    public void onDestroy() {
        mFragment = null;
        super.onDestroy();
    }
}
