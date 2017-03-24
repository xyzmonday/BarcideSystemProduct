package com.richfit.barcidesystemproduct.module.home;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.BottomSheetDialogAdapter;
import com.richfit.barcodesystemproduct.adapter.ModularAdapter;
import com.richfit.barcodesystemproduct.base.BaseActivity;
import com.richfit.barcodesystemproduct.module.main.MainActivity;
import com.richfit.barcodesystemproduct.module.setting.SettingActivity;
import com.richfit.barcodesystemproduct.module_local.loaddown.LoadLocalRefDataActivity;
import com.richfit.common_lib.baseadapterrv.MultiItemTypeAdapter;
import com.richfit.common_lib.decoration.DividerGridItemDecoration;
import com.richfit.common_lib.dialog.BasePopupWindow;
import com.richfit.common_lib.dialog.SelectDialog;
import com.richfit.common_lib.utils.AppCompat;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.MenuTreeHelper;
import com.richfit.domain.bean.MenuNode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 在线模式的功能主页面。该页面作为App的主页面，这里我们设置它的Activity返回栈为
 * SingleTask(栈内复用，只要栈内里面有那么在它之前的所有Activity全部销毁，HomeActivity回调
 * onNewIntent方法)
 *
 * Created by monday on 2016/11/7.
 */
public class HomeActivity extends BaseActivity<HomePresenterImp> implements HomeContract.View,
        OnItemClickListener{

    @BindView(R.id.modular_list)
    RecyclerView mRecycleView;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    ModularAdapter mAdapter;

    ArrayList<MenuNode> mMenuNodes;

    Dialog mBottomSheetDialog;
    AlertView mAlertView;

    private static final int[] MENUS_IMAGES = {
            R.mipmap.icon_submenu1,
            R.mipmap.icon_submenu2,
            R.mipmap.icon_submenu3,
            R.mipmap.icon_submenu4,
            R.mipmap.icon_submenu5,
            R.mipmap.icon_submenu6,
            R.mipmap.icon_submenu7,
            R.mipmap.icon_submenu8,
            R.mipmap.icon_submenu9,
            R.mipmap.icon_submenu10,
            R.mipmap.icon_submenu11
    };

    @Override
    protected int getContentId() {
        return R.layout.activity_home;
    }

    @Override
    public void initInjector() {
        mActivityComponent.inject(this);
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        setUpToolBar();
        mMenuNodes = new ArrayList<>();
        //注意这里没有给出mode因为系统到Home页面的时候已经确定了该字段的值
        mPresenter.setupModule(Global.LOGIN_ID);
    }

    /**
     * 设置toolbar
     */
    private void setUpToolBar() {
        mToolbarTitle.setGravity(Gravity.CENTER);
        mToolbarTitle.setText("条码系统");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(false); //设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    }

    @Override
    protected void initViews() {
        GridLayoutManager lm = new GridLayoutManager(this, 3);
        mRecycleView.setLayoutManager(lm);
        mRecycleView.addItemDecoration(new DividerGridItemDecoration(this));
        mAlertView = new AlertView("温馨提示", "您真的退出App吗?", "取消", new String[]{"确定"}, null,
                this, AlertView.Style.Alert, this);

    }

    /**
     * 开始初始化九宫格的主功能展示界面
     *
     * @param menuNodes
     */
    @Override
    public void initModulesSuccess(List<MenuNode> menuNodes) {
        mMenuNodes.clear();
        mMenuNodes.addAll(menuNodes);
        if (mAdapter == null) {
            final String rootId = menuNodes.get(0).getId();

            mAdapter = new ModularAdapter(this, R.layout.item_module, MenuTreeHelper.getNodesByLevel(menuNodes, rootId));
            mRecycleView.setAdapter(mAdapter);

            mAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                    MenuNode item = mAdapter.getItem(position);
                    final List<MenuNode> bizMenuNodes = item.getChildren();
                    if (bizMenuNodes != null && bizMenuNodes.size() == 1) {
                        //如果第二级菜单自有一个（第二级菜单必须的个数必须大于等于1）
                        final List<MenuNode> refMenuNodes = bizMenuNodes.get(0).getChildren();
                        if (refMenuNodes != null && refMenuNodes.size() == 1) {
                            //如果单据也只有一个，那么直接进入业务MainActivity
                            toMain(Global.companyCode, item.getFunctionCode(),
                                    bizMenuNodes.get(0).getBusinessType(),
                                    refMenuNodes.get(0).getRefType(), bizMenuNodes.get(0).getCaption());
                        } else if (refMenuNodes == null || refMenuNodes.size() == 0) {
                            //如果没有第三级菜单，那么直接跳转
                            toMain(Global.companyCode, item.getFunctionCode(),
                                    bizMenuNodes.get(0).getBusinessType(),
                                    "", bizMenuNodes.get(0).getCaption());
                        } else {
                            //如果有多个单据，那么直接操作第三级
                            setupRefTypeDialog(bizMenuNodes.get(0), item.getFunctionCode());
                        }
                    } else if (bizMenuNodes == null || bizMenuNodes.size() == 0) {
                        //没有第二级菜单.第三级菜单
                        toMain(Global.companyCode, item.getFunctionCode(),
                                item.getBusinessType(),
                                "", item.getCaption());

                    } else {
                        //如果子菜单有多个，那么用户需要先操作子菜单
                        setupBizTypeDialog(item);
                    }
                }

                @Override
                public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                    return false;
                }
            });
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void initModelsFail(String message) {
        showMessage(message);
    }


    /**
     * 创建底部自子菜单功能列表对话框（PopUpWindow）
     *
     * @param menuNode:第一级节点菜单
     */
    public void setupBizTypeDialog(final MenuNode menuNode) {
        final String moduleCode = menuNode.getFunctionCode();

        View view = getLayoutInflater().inflate(R.layout.menu_bottom, null);
        GridView subFunGridList = (GridView) view.findViewById(R.id.gridview);
        final BottomSheetDialogAdapter adapter = new BottomSheetDialogAdapter(this, R.layout.item_bottom_bizmenu,
                menuNode.getChildren(), MENUS_IMAGES);
        subFunGridList.setAdapter(adapter);

        if (mBottomSheetDialog == null) {
            mBottomSheetDialog = new Dialog(HomeActivity.this, R.style.MaterialDialogSheet);
        }

        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.setCancelable(true);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
        mBottomSheetDialog.show();

        //子菜单的点击事件监听
        subFunGridList.setOnItemClickListener((parent, view1, position, id) -> {
            //第二级菜单节点
            final MenuNode item = adapter.getItem(position);
            final List<MenuNode> refMenuNodes = item.getChildren();
            if (refMenuNodes != null && refMenuNodes.size() == 1) {
                //如果第三级只有一个节点，那么直接进入业务界面
                toMain(Global.companyCode, moduleCode, item.getBusinessType(),
                        refMenuNodes.get(0).getRefType(), item.getCaption());
            } else if (refMenuNodes == null || refMenuNodes.size() == 0) {
                toMain(Global.companyCode, moduleCode, item.getBusinessType(),
                        item.getRefType(), item.getCaption());
            } else {
                setupRefTypeDialog(item, moduleCode);
            }
            mBottomSheetDialog.dismiss();
        });
    }

    /**
     * 用户选择单据类型
     *
     * @param menuNode：第二级节点
     * @param moduleCode：第一级节点编码
     */
    private void setupRefTypeDialog(final MenuNode menuNode, final String moduleCode) {
        final List<MenuNode> children = menuNode.getChildren();
        if (children != null && children.size() > 0) {
            final List<String> items = new ArrayList<>(children.size());
            for (MenuNode child : children) {
                items.add(child.getCaption());
            }
            //构造实例化选择弹窗
            SelectDialog chooseDialog = new SelectDialog.Builder(HomeActivity.this)
                    .setDataList(items)
                    .setButtonColor(AppCompat.getColor(R.color.black, HomeActivity.this))
                    .setButtonSize(14)
                    .setLastButtonSize(14)
                    .setTitleText("单据类型选择")
                    .build();
            //对选择弹窗item点击事件监听
            chooseDialog.setButtonListener(new BasePopupWindow.OnButtonListener() {
                @Override
                public void onSureListener(View v) {
                    int position = ((Integer) v.getTag());
                    toMain(Global.companyCode, moduleCode, menuNode.getBusinessType(),
                            children.get(position).getRefType(), children.get(position).getCaption());
                }

                @Override
                public void onDiscardListener(View v) {

                }

                @Override
                public void onDismissListener(View v, int nType) {

                }
            });
            chooseDialog.show(mView);
        }
    }

    /**
     * 调整到mainActivity,用户进行业务操作
     *
     * @param companyCode:公司编码
     * @param moduleCode:主模块编码
     * @param bizType:业务类型
     * @param refType:单据类型
     * @param caption:模块名称
     */
    private void toMain(String companyCode, String moduleCode, String bizType, String refType, String caption) {
        if (TextUtils.isEmpty(companyCode)) {
            showMessage("未获取到需要操作的公司编码");
            return;
        }

        if (TextUtils.isEmpty(moduleCode)) {
            showMessage("未获取到需要操作的主模块编码");
            return;
        }

        if (TextUtils.isEmpty(bizType)) {
            showMessage("未获取到需要操作的业务类型");
            return;
        }
        Class<?> clazz;
        switch (moduleCode) {
            case Global.SETTING:
                clazz = SettingActivity.class;
                break;
            case Global.L_LOADDATA:
                clazz = LoadLocalRefDataActivity.class;
                break;
            default:
                clazz = MainActivity.class;
                break;
        }
        Intent intent = new Intent(HomeActivity.this, clazz);
        Bundle bundle = new Bundle();
        bundle.putString(Global.EXTRA_COMPANY_CODE_KEY, companyCode);
        bundle.putString(Global.EXTRA_MODULE_CODE_KEY, moduleCode);
        bundle.putString(Global.EXTRA_BIZ_TYPE_KEY, bizType);
        bundle.putString(Global.EXTRA_REF_TYPE_KEY, refType);
        bundle.putString(Global.EXTRA_CAPTION_KEY, caption);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void networkConnectError(String retryAction) {

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mAlertView == null) {
                mAlertView = new AlertView("温馨提示", "您真的退出App吗?", "取消", new String[]{"确定"}, null,
                        this, AlertView.Style.Alert, this);
            }
            mAlertView.show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onItemClick(Object o, int position) {
        switch (position) {
            case 0:
                finish();
                break;
            case -1:
                if (mAlertView != null && mAlertView.isShowing()) {
                    mAlertView.dismiss();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (mAlertView != null && mAlertView.isShowing()) {
            mAlertView.dismiss();
        }
        super.onDestroy();
    }
}
