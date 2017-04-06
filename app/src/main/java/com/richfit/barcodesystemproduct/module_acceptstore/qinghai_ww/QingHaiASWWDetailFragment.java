package com.richfit.barcodesystemproduct.module_acceptstore.qinghai_ww;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_detail.BaseASDetailFragment;
import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_detail.imp.ASDetailPresenterImp;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.domain.bean.BottomMenuEntity;

import java.util.List;

/**
 * 物资委外入库
 * Created by monday on 2017/2/20.
 */

public class QingHaiASWWDetailFragment extends BaseASDetailFragment<ASDetailPresenterImp> {

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    public List<BottomMenuEntity> provideDefaultBottomMenu() {
        List<BottomMenuEntity> menus = super.provideDefaultBottomMenu();
        menus.get(0).transToSapFlag = "01";
        //如果全部是必检物资那么不需要上架
        if (!mRefData.qmFlag)
            menus.get(1).transToSapFlag = "05";
        return menus.subList(0, 2);
    }

    /**
     * 第一步过账成功显示物料凭证
     */
    @Override
    public void submitBarcodeSystemSuccess() {
        showSuccessDialog(mTransNum);
        if (mRefData.qmFlag) {
            setRefreshing(false, "过账成功");
            if (mAdapter != null) {
                mAdapter.removeAllVisibleNodes();
            }
            //注意这里必须清除单据数据
            mRefData = null;
            mTransNum = "";
            mTransId = "";
            SPrefUtil.saveData(mBizType + mRefType, "0");
            mPresenter.showHeadFragmentByPosition(BaseFragment.HEADER_FRAGMENT_INDEX);
        }
    }

    @Override
    protected String getSubFunName() {
        return "委外入库";
    }
}
