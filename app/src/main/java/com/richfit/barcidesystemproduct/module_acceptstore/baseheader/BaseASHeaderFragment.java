package com.richfit.barcidesystemproduct.module_acceptstore.baseheader;

import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.baseheader.imp.ASHeaderPresenterImp;
import com.richfit.common_lib.utils.DateChooseHelper;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.common_lib.widget.RichEditText;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.RowConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

/**
 * 物资入库抬头界面基类
 * Created by monday on 2016/11/11.
 */

public abstract class BaseASHeaderFragment extends BaseFragment<ASHeaderPresenterImp>
        implements IASHeaderView {

    @BindView(R.id.et_ref_num)
    RichEditText etRefNum;
    @BindView(R.id.tv_ref_num)
    TextView tvRefNum;
    @BindView(R.id.et_transfer_date)
    RichEditText etTransferDate;
    @BindView(R.id.tv_supplier)
    TextView tvSupplier;
    @BindView(R.id.tv_send_work)
    TextView tvSendWork;
    @BindView(R.id.ll_supplier)
    protected LinearLayout llSupplier;
    @BindView(R.id.ll_send_work)
    protected LinearLayout llSendWork;
    @BindView(R.id.tv_send_work_name)
    protected TextView tvSendWorkName;
    @BindView(R.id.ll_creator)
    protected LinearLayout llCreator;
    @BindView(R.id.tv_creator)
    TextView tvCreator;

    @Override
    public void handleBarCodeScanResult(String type, String[] list) {
        if (list != null && list.length == 1) {
            getRefData(list[0]);
        }
    }

    @Override
    protected int getContentId() {
        return R.layout.fragment_base_asy_header;
    }

    @Override
    public void initVariable(Bundle savedInstanceState) {
        mRefData = null;
        mSubFunEntity.headerConfigs = null;
        mSubFunEntity.parentNodeConfigs = null;
        mSubFunEntity.childNodeConfigs = null;
        mSubFunEntity.collectionConfigs = null;
        mSubFunEntity.locationConfigs = null;
    }

    @Override
    protected void initView() {
        etTransferDate.setText(UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1));
        mPresenter.readExtraConfigs(mCompanyCode, mBizType, mRefType, Global.HEADER_CONFIG_TYPE);
    }

    /**
     * 注册点击事件
     */
    @Override
    public void initEvent() {
        /*点击单号加载单据数据*/
        etRefNum.setOnRichEditTouchListener((view, refNum) -> {
            hideKeyboard(view);
            getRefData(refNum);
        });

        /*选择日期*/
        etTransferDate.setOnRichEditTouchListener((view, text) ->
                DateChooseHelper.chooseDateForEditText(mActivity, etTransferDate, Global.GLOBAL_DATE_PATTERN_TYPE1));
    }

    protected void getRefData(String refNum) {
        mRefData = null;
        clearAllUI();
        mPresenter.getReference(refNum, mRefType, mBizType, getMoveType(),"", Global.USER_ID);
    }

    /**
     * 读取抬头配置文件成功
     *
     * @param configs
     */
    @Override
    public void readConfigsSuccess(List<ArrayList<RowConfig>> configs) {
        mSubFunEntity.headerConfigs = configs.get(0);
        createExtraUI(mSubFunEntity.headerConfigs, EXTRA_VERTICAL_ORIENTATION_TYPE);
    }

    /**
     * 读取抬头配置文件失败
     *
     * @param message
     */
    @Override
    public void readConfigsFail(String message) {
        showMessage(message);
        mSubFunEntity.headerConfigs = null;
    }

    /**
     * 获取单据数据成功
     *
     * @param refData
     */
    @Override
    public void getReferenceSuccess(ReferenceEntity refData) {
        SPrefUtil.saveData(mBizType + mRefType, "0");
        refData.bizType = mBizType;
        refData.moveType = getMoveType();
        refData.refType = mRefType;
        mRefData = refData;
        cacheProcessor(mRefData.transId, mRefData.transId, mRefData.recordNum,
                mRefData.refCodeId, mRefData.refType, mRefData.bizType);
    }

    /**
     * 获取单据数据失败
     *
     * @param message
     */
    @Override
    public void getReferenceFail(String message) {
        showMessage(message);
        mRefData = null;
        //清除所有控件绑定的数据
        clearAllUI();
    }

    /**
     * 检查数据库是否存在该历史数据.
     * 如果有缓存提示用户是否删除缓存。
     * 如果用户点击确定删除那么删除缓存，并刷新界面；
     * 如果用户点击取消删除那么直接刷新界面，在采集界面和明细界面会重新获取缓存
     *
     * @param cacheFlag：缓存标志。有可能是Y,N或者TransId标识
     * @param transId：缓存id,用于删除缓存
     * @param refNum：单据号
     * @param bizType：业务类型
     */
    @Override
    public void cacheProcessor(String cacheFlag, String transId, String refNum,
                               String refCodeId, String refType, String bizType) {
        if (!TextUtils.isEmpty(cacheFlag)) {
            android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(mActivity);
            dialog.setTitle("提示");
            dialog.setIcon(R.mipmap.icon_tips);
            dialog.setMessage(getString(R.string.has_history));
            dialog.setPositiveButton("确定", (dia, which) -> {
                dia.dismiss();
                mPresenter.deleteCollectionData(refNum, transId, refCodeId, refType, bizType, Global.USER_ID,
                        mCompanyCode);
            });
            dialog.setNegativeButton("取消", (dia, which) -> {
                dia.dismiss();
                mPresenter.getTransferInfo(mRefData, refCodeId, bizType, refType);
            });
            dialog.show();
        } else {
            bindCommonHeaderUI();
        }
    }

    @Override
    public void getTransferInfoFail(String message) {
        showMessage(message);
    }

    /**
     * 删除整单缓存数据成功
     */
    @Override
    public void deleteCacheSuccess() {
        showMessage("缓存删除成功");
        bindCommonHeaderUI();
    }

    /**
     * 删除整单缓存数据失败
     *
     * @param message
     */
    @Override
    public void deleteCacheFail(String message) {
        showMessage(message);
        bindCommonHeaderUI();
    }

    /**
     * 为公共控件绑定数据
     */
    @Override
    public void bindCommonHeaderUI() {
        if (mRefData != null) {
            //单据号
            tvRefNum.setText(mRefData.recordNum);
            //供应商
            tvSupplier.setText(mRefData.supplierNum + "_" + mRefData.supplierDesc);
            //发出工厂
            tvSendWork.setText(mRefData.workCode);
            tvCreator.setText(Global.LOGIN_ID);
            //绑定额外字段
            bindExtraUI(mSubFunEntity.headerConfigs, mRefData.mapExt);
        }
    }

    @Override
    public void clearAllUI() {
        clearCommonUI(tvRefNum, tvSupplier, tvSendWork);
        clearExtraUI(mSubFunEntity.headerConfigs);
    }

    @Override
    public void clearAllUIAfterSubmitSuccess() {
        clearCommonUI(etRefNum, tvRefNum, tvSendWork, tvSupplier);
        clearExtraUI(mSubFunEntity.headerConfigs);
        mRefData = null;
    }

    @Override
    public void _onPause() {
        //再次检查用户是否输入的额外字段而且必须输入的字段（情景是用户请求单据之前没有输入该字段，回来填上后，但是没有请求单据而是直接）
        //切换了页面
        if (mRefData != null) {
            mRefData.voucherDate = getString(etTransferDate);
            Map<String, Object> extraHeaderMap = saveExtraUIData(mSubFunEntity.headerConfigs);
            mRefData.mapExt = UiUtil.copyMap(extraHeaderMap, mRefData.mapExt);
        }
    }

    /**
     * 网络错误重试
     *
     * @param action
     */
    @Override
    public void retry(String action) {
        switch (action) {
            case Global.RETRY_LOAD_REFERENCE_ACTION:
                mPresenter.getReference(getString(etRefNum), mRefType, mBizType, getMoveType(),"", Global.LOGIN_ID);
                break;
        }
        super.retry(action);
    }


    @Override
    public boolean isNeedShowFloatingButton() {
        return false;
    }

    /*返回移动类型*/
    @CheckResult
    @NonNull
    protected abstract String getMoveType();
}
