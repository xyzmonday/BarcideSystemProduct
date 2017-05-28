package com.richfit.barcodesystemproduct.module_location_process.location_qt.header;

import android.os.Bundle;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.base.base_header.BaseHeaderFragment;
import com.richfit.barcodesystemproduct.module_location_process.location_qt.header.imp.LocQTHeaderPresenterImp;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.common_lib.widget.RichEditText;
import com.richfit.domain.bean.ReferenceEntity;

import butterknife.BindView;

/**
 * Created by monday on 2017/5/25.
 */

public class LocQTHeaderFragment extends BaseHeaderFragment<LocQTHeaderPresenterImp>
        implements ILocQTHeaderView {

    private final static String MOVE_TYPE = "";

    @BindView(R.id.et_ref_num)
    RichEditText etRefNum;

    @Override
    public void handleBarCodeScanResult(String type, String[] list) {
        if (list != null && list.length == 1) {
            getRefData(list[0]);
        }
    }

    @Override
    protected int getContentId() {
        return R.layout.fragment_loc_qt_header;
    }

    @Override
    public void initInjector() {

    }

    @Override
    public void initVariable(Bundle savedInstanceState) {
        mRefData = null;
    }

    /**
     * 注册点击事件
     */
    @Override
    public void initEvent() {
        //点击单号加载单据数据
        etRefNum.setOnRichEditTouchListener((view, refNum) -> {
            hideKeyboard(view);
            getRefData(refNum);
        });
    }

    protected void getRefData(String refNum) {
        mRefData = null;
        clearAllUI();
        mPresenter.getReference(refNum, mRefType, mBizType, MOVE_TYPE, "", Global.USER_ID);
    }

    @Override
    public void getReferenceSuccess(ReferenceEntity refData) {
        //需要注意上下架处理是否有单据类型
        SPrefUtil.saveData(mBizType, "0");
        refData.bizType = mBizType;
        refData.moveType = MOVE_TYPE;
        refData.refType = mRefType;
        mRefData = refData;
    }

    @Override
    public void getReferenceFail(String message) {
        showMessage(message);
        mRefData = null;
        clearAllUI();
    }

    @Override
    public void clearAllUI() {
        clearCommonUI(etRefNum);
    }

    @Override
    public void clearAllUIAfterSubmitSuccess() {
        clearCommonUI(etRefNum);
    }

}
