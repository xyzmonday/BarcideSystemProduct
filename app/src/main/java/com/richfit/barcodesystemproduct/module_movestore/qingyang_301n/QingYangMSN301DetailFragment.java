package com.richfit.barcodesystemproduct.module_movestore.qingyang_301n;

import android.text.TextUtils;

import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_detail.BaseMSNDetailFragment;
import com.richfit.barcodesystemproduct.module_movestore.qingyang_301n.imp.QingYangNMS301DetailPresenterImp;
import com.richfit.domain.bean.BottomMenuEntity;

import java.util.List;

/**
 * 庆阳301数据过账只有一步
 * Created by monday on 2017/2/8.
 */

public class QingYangMSN301DetailFragment extends BaseMSNDetailFragment<QingYangNMS301DetailPresenterImp> {


    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    public void initDataLazily() {
        if (mRefData == null) {
            setRefreshing(false, "获取明细失败,请现在抬头界面选择相应的参数");
            return;
        }

        if (TextUtils.isEmpty(mRefData.workId) ||
                TextUtils.isEmpty(mRefData.recWorkId)) {
            setRefreshing(false, "获取明细失败,请先选择发出工厂和接收工厂");
            return;
        }

        if (TextUtils.isEmpty(mRefData.recInvId)) {
            setRefreshing(false, "获取明细失败,请先选择接收库位");
            return;
        }

        if (!checkExtraData(mSubFunEntity.headerConfigs, mRefData.mapExt)) {
            showMessage("请先在抬头界面输入必要的信息");
            return;
        }
        startAutoRefresh();
    }

    @Override
    public void submitBarcodeSystemSuccess() {
        setRefreshing(false, "过账成功");
        showSuccessDialog(mTransNum);
        if (mAdapter != null) {
            mAdapter.removeAllVisibleNodes();
        }
        mRefData = null;
        mTransNum = "";
        mTransId = "";
        mPresenter.showHeadFragmentByPosition(BaseFragment.HEADER_FRAGMENT_INDEX);
    }

    @Override
    public void submitBarcodeSystemFail(String message) {
        showErrorDialog(message);
    }

    @Override
    public void submitSAPSuccess() {

    }

    @Override
    public void showInspectionNum(String message) {
    }

    @Override
    protected boolean checkTransStateBeforeRefresh() {
        return true;
    }

    @Override
    public List<BottomMenuEntity> provideDefaultBottomMenu() {
        List<BottomMenuEntity> menus = super.provideDefaultBottomMenu();
        return menus.subList(3, 4);
    }

    @Override
    protected String getSubFunName() {
        return "301无参考移库";
    }
}
