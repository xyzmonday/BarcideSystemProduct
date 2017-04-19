package com.richfit.barcodesystemproduct.module_check.qinghai_cn.header;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxAdapterView;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;
import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.BottomMenuAdapter;
import com.richfit.barcodesystemproduct.adapter.InvAdapter;
import com.richfit.barcodesystemproduct.adapter.WorkAdapter;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.module_check.qinghai_cn.header.imp.CNHeaderPresenterImp;
import com.richfit.common_lib.utils.DateChooseHelper;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.common_lib.widget.RichEditText;
import com.richfit.domain.bean.BottomMenuEntity;
import com.richfit.domain.bean.InvEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.RowConfig;
import com.richfit.domain.bean.WorkEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;

import static com.richfit.common_lib.utils.Global.USER_ID;

/**
 * 青海无参考明盘抬头界面。仓库级选择工厂和库存地点;
 * 仓位级选择库存号。checkLevel表示盘点的级别，01表示仓位级;02表示库存级
 * <p>
 * Created by monday on 2017/3/3.
 */
@SuppressLint("NewApi")
public class QingHaiCNHeaderFragment extends BaseFragment<CNHeaderPresenterImp>
        implements ICNHeaderView {


    @BindView(R.id.ll_warehouse_level)
    LinearLayout llWarehouseLevel;
    @BindView(R.id.ll_storage_num_level)
    LinearLayout llStorageNumLevel;
    @BindView(R.id.btn_storage_num_level)
    RadioButton rbStorageNumLevel;
    @BindView(R.id.btn_warehouse_level)
    RadioButton rbWarehouseLevel;
    @BindView(R.id.sp_work)
    Spinner spWork;
    @BindView(R.id.sp_inv)
    Spinner spInv;
    @BindView(R.id.sp_storage_num)
    Spinner spStorageNum;
    @BindView(R.id.tv_checker)
    TextView tvChecker;
    @BindView(R.id.et_check_date)
    RichEditText etTransferDate;
    @BindView(R.id.cb_special_flag)
    CheckBox cbSpecialFlag;

    /*工厂列表*/
    List<WorkEntity> mWorkDatas;
    /*库存地点列表以及适配器*/
    InvAdapter mInvAdapter;
    List<InvEntity> mInvDatas;
    /*库存号列表*/
    List<String> mStorageNums;
    String mSpecialFlag;


    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected int getContentId() {
        return R.layout.fragment_cn_header;
    }

    @Override
    public void initVariable(Bundle savedInstanceState) {
        mInvDatas = new ArrayList<>();
        mWorkDatas = new ArrayList<>();
        mStorageNums = new ArrayList<>();
        mRefData = null;
        mSubFunEntity.headerConfigs = null;
        mSubFunEntity.parentNodeConfigs = null;
        mSubFunEntity.childNodeConfigs = null;
        mSubFunEntity.collectionConfigs = null;
        mSubFunEntity.locationConfigs = null;
    }

    @Override
    protected void initView() {
        mPresenter.readExtraConfigs(mCompanyCode, mBizType, mRefType, Global.HEADER_CONFIG_TYPE);
    }

    /**
     * 注册点击事件
     */
    @Override
    public void initEvent() {
        /*选择盘点日期*/
        etTransferDate.setOnRichEditTouchListener((view, text) ->
                DateChooseHelper.chooseDateForEditText(mActivity, etTransferDate, Global.GLOBAL_DATE_PATTERN_TYPE1));


        /*监听工厂，获取库存地点*/
        RxAdapterView.itemSelections(spWork)
                .filter(position -> position.intValue() > 0)
                .subscribe(position -> mPresenter.getInvsByWorkId(mWorkDatas.get(position).workId, 0));

         /*如果选择仓库级，那么初始化工厂和库存地点*/
        RxView.clicks(rbWarehouseLevel)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(a -> {
                    resetUI();
                    mRefData = null;
                    rbWarehouseLevel.setChecked(true);
                    llWarehouseLevel.setVisibility(View.VISIBLE);
                });


        /*如果选择仓位级，那么初始化仓库号*/
        RxView.clicks(rbStorageNumLevel)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(a -> {
                    resetUI();
                    mRefData = null;
                    rbStorageNumLevel.setChecked(true);
                    llStorageNumLevel.setVisibility(View.VISIBLE);
                    if (spStorageNum.getAdapter() == null) {
                        mPresenter.getStorageNums(0);
                    }
                });

        RxCompoundButton.checkedChanges(cbSpecialFlag)
                .subscribe(a -> mSpecialFlag = a.booleanValue() ? "Y" : "N");

    }

    @Override
    public void initData() {
        etTransferDate.setText(UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1));
        tvChecker.setText(Global.LOGIN_ID);
        mSpecialFlag = cbSpecialFlag.isChecked() ? "Y" : "N";
    }

    /**
     * 重置所有的Button背景
     */
    private void resetUI() {
        rbStorageNumLevel.setChecked(false);
        rbWarehouseLevel.setChecked(false);
        llWarehouseLevel.setVisibility(View.INVISIBLE);
        llStorageNumLevel.setVisibility(View.INVISIBLE);
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

    @Override
    public void readConfigsComplete() {
        //因为默认是选择仓库级的，所以在先初始化工厂
        if (spWork.getAdapter() == null) {
            //如果工厂还未初始化
            mPresenter.getWorks(0);
        }
    }

    /**
     * 显示工厂
     *
     * @param works
     */
    @Override
    public void showWorks(List<WorkEntity> works) {
        mWorkDatas.clear();
        mWorkDatas.addAll(works);
        WorkAdapter adapter = new WorkAdapter(mActivity, android.R.layout.simple_list_item_1, mWorkDatas);
        spWork.setAdapter(adapter);
    }

    @Override
    public void loadWorksFail(String message) {
        showMessage(message);
        SpinnerAdapter adapter = spWork.getAdapter();
        if (adapter != null && WorkAdapter.class.isInstance(adapter)) {
            mWorkDatas.clear();
            WorkAdapter workAdapter = (WorkAdapter) adapter;
            workAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 显示库存地点
     *
     * @param invs
     */
    @Override
    public void showInvs(List<InvEntity> invs) {
        mInvDatas.clear();
        mInvDatas.addAll(invs);
        if (mInvAdapter == null) {
            mInvAdapter = new InvAdapter(mActivity, android.R.layout.simple_list_item_1,
                    mInvDatas);
            spInv.setAdapter(mInvAdapter);
        } else {
            mInvAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void loadInvsFail(String message) {
        showMessage(message);
        if (mInvAdapter != null) {
            mInvDatas.clear();
            mInvAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showStorageNums(List<String> storageNums) {
        mStorageNums.clear();
        mStorageNums.addAll(storageNums);
        ArrayAdapter<String> adapter = new ArrayAdapter(mActivity, android.R.layout.simple_list_item_1,
                mStorageNums);
        spStorageNum.setAdapter(adapter);
    }

    @Override
    public void loadStorageNumFail(String message) {
        showMessage(message);
        SpinnerAdapter adapter = spWork.getAdapter();
        if (adapter != null && ArrayAdapter.class.isInstance(adapter)) {
            mWorkDatas.clear();
            ArrayAdapter workAdapter = (ArrayAdapter) adapter;
            workAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 用户初始化盘点之前进行必要的检查
     *
     * @return
     */
    @Override
    public boolean checkDataBeforeOperationOnHeader() {
        if (rbWarehouseLevel.isChecked()) {
            //如果仓库级选中，那么检查工厂和库存地点
            if (spWork.getSelectedItemPosition() == 0) {
                showMessage("请检查工厂");
                return false;
            }

            if (spInv.getSelectedItemPosition() == 0) {
                showMessage("请选择库存地点");
                return false;
            }
            return true;
        } else if (rbStorageNumLevel.isChecked()) {
            //如果仓位级选中，那么检查仓库号
            if (spStorageNum.getSelectedItemPosition() == 0) {
                showMessage("请选择仓库号");
                return false;
            }
            return true;
        }
        return true;
    }

    /**
     * 显示开始盘点和重新盘点两个菜单
     *
     * @param companyCode
     */
    @Override
    public void operationOnHeader(String companyCode) {

        View rootView = LayoutInflater.from(mActivity).inflate(R.layout.menu_bottom, null);
        GridView menu = (GridView) rootView.findViewById(R.id.gridview);
        BottomMenuAdapter adapter = new BottomMenuAdapter(mActivity, R.layout.item_bottom_menu, provideDefaultBottomMenu());
        menu.setAdapter(adapter);

        final Dialog dialog = new Dialog(mActivity, R.style.MaterialDialogSheet);
        dialog.setContentView(rootView);
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();

        menu.setOnItemClickListener((adapterView, view, position, id) -> {
            switch (position) {
                case 0:
                    //1.开始盘点
                    startCheck();
                    break;
                case 1:
                    //2.重新盘点
                    restartCheck();
                    break;
            }
            dialog.dismiss();
        });

    }

    /**
     * 开始盘点
     */
    private void startCheck() {
        //请求抬头信息
        mRefData = null;
        if (rbStorageNumLevel.isChecked()) {
            //库位级盘点
            mPresenter.getCheckInfo(USER_ID, mBizType, "01",
                    mSpecialFlag, mStorageNums.get(spStorageNum.getSelectedItemPosition()),
                    "", "");
        } else if (rbWarehouseLevel.isChecked()) {
            //库存级
            mPresenter.getCheckInfo(USER_ID, mBizType, "02",
                    mSpecialFlag, "",
                    mWorkDatas.get(spWork.getSelectedItemPosition()).workId,
                    mInvDatas.get(spInv.getSelectedItemPosition()).invId);
        }
    }

    /**
     * 重新开始盘点。先调用删除整单的接口删除历史的盘点数据，然后调用getCheckInfo获取新的盘点数据
     */
    private void restartCheck() {

        if (mRefData == null) {
            showMessage("请先点击开始盘点");
            return;
        }
        if (TextUtils.isEmpty(mRefData.checkId)) {
            showMessage("请先点击开始盘点");
            return;
        }

        if (rbStorageNumLevel.isChecked()) {
            //仓位级别
            mPresenter.deleteCheckData(mStorageNums.get(spStorageNum.getSelectedItemPosition()),
                    "", "", mRefData.checkId, Global.USER_ID, mBizType);
        } else if (rbWarehouseLevel.isChecked()) {
            mPresenter.deleteCheckData("", mWorkDatas.get(spWork.getSelectedItemPosition()).workId,
                    mInvDatas.get(spInv.getSelectedItemPosition()).invId,
                    mRefData.checkId, Global.USER_ID, mBizType);
        }
    }

    @Override
    public List<BottomMenuEntity> provideDefaultBottomMenu() {
        ArrayList<BottomMenuEntity> menus = new ArrayList<>();
        BottomMenuEntity menu = new BottomMenuEntity();
        menu.menuName = "开始盘点";
        menu.menuImageRes = R.mipmap.icon_start_check;
        menus.add(menu);

        menu = new BottomMenuEntity();
        menu.menuName = "重新盘点";
        menu.menuImageRes = R.mipmap.icon_restart_check;
        menus.add(menu);
        return menus;
    }

    @Override
    public void getCheckInfoSuccess(ReferenceEntity refData) {
        SPrefUtil.saveData(mBizType, "0");
        refData.bizType = mBizType;
        refData.refType = mRefType;
        mRefData = refData;
        showMessage("成功获取到盘点列表");

    }

    @Override
    public void getCheckInfoFail(String message) {
        mRefData = null;
        showMessage(message);
    }

    @Override
    public void bindCommonHeaderUI() {
        if (mRefData != null) {
            //还未有任何字段需要绑定
            bindExtraUI(mSubFunEntity.headerConfigs, mRefData.mapExt);
        }
    }

    @Override
    public void deleteCacheSuccess() {
        showMessage("删除盘点历史成功");
        startCheck();
    }

    @Override
    public void deleteCacheFail(String message) {
        showMessage(message);
    }

    @Override
    public void _onPause() {
        //再次检查用户是否输入的额外字段而且必须输入的字段（情景是用户请求单据之前没有输入该字段，回来填上后，但是没有请求单据而是直接）
        //切换了页面
        if (mRefData != null) {
            mRefData.voucherDate = getString(etTransferDate);
            if (rbStorageNumLevel.isChecked()) {
                mRefData.storageNum = mStorageNums.get(spStorageNum.getSelectedItemPosition());
                mRefData.checkLevel = "01";
            } else if (rbWarehouseLevel.isChecked()) {
                final int selectedWorkPosition = spWork.getSelectedItemPosition();
                if (selectedWorkPosition > 0) {
                    mRefData.workId = mWorkDatas.get(selectedWorkPosition).workId;
                    mRefData.workCode = mWorkDatas.get(selectedWorkPosition).workCode;
                }
                final int selectedInvPosition = spInv.getSelectedItemPosition();
                if (selectedInvPosition > 0) {
                    mRefData.invId = mInvDatas.get(selectedInvPosition).invId;
                    mRefData.invCode = mInvDatas.get(selectedInvPosition).invCode;
                }
                mRefData.checkLevel = "02";
            }
            mRefData.specialFlag = mSpecialFlag;
            Map<String, Object> extraHeaderMap = saveExtraUIData(mSubFunEntity.headerConfigs);
            mRefData.mapExt = UiUtil.copyMap(extraHeaderMap, mRefData.mapExt);
        }
    }

    @Override
    public void clearAllUIAfterSubmitSuccess() {

    }


    @Override
    public void retry(String action) {
        switch (action) {
            case Global.RETRY_LOAD_REFERENCE_ACTION:
                startCheck();
                break;
            case Global.RETRY_DELETE_TRANSFERED_CACHE_ACTION:
                restartCheck();
                break;
        }
        super.retry(action);
    }
}
