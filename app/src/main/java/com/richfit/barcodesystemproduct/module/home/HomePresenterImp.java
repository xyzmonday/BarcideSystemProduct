package com.richfit.barcodesystemproduct.module.home;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.base.BasePresenter;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.MenuNode;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subscribers.ResourceSubscriber;


/**
 * Created by monday on 2016/11/7.
 */

public class HomePresenterImp extends BasePresenter<HomeContract.View>
        implements HomeContract.Presenter {

    HomeContract.View mView;
    //parentId为0对应的Id;
    String mOnLineMenuRootId;
    String mOffLineMenuRootId;

    @Override
    protected void onStart() {
        mView = getView();
    }

    @Inject
    public HomePresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    /**
     * 获取所有的菜单信息，通过mode显示对应的菜单
     *
     * @param loginId
     */
    @Override
    public void setupModule(final String loginId) {
        mView = getView();
        final int mode = isLocal() ? Global.OFFLINE_MODE : Global.ONLINE_MODE;
        Disposable subscriber =
                Flowable.zip(mRepository.getMenuInfo(loginId, Global.ONLINE_MODE)
                                .flatMap(list -> Flowable.just(wrapperMenuNodes(list, Global.ONLINE_MODE))),
                        mRepository.getMenuInfo(loginId, Global.OFFLINE_MODE)
                                .flatMap(list -> Flowable.just(wrapperMenuNodes(list, Global.OFFLINE_MODE))),
                        (onLineMenus, offLineMenus) -> mergeMenuNodes(onLineMenus, offLineMenus))
                        .filter(list -> list != null && list.size() > 0)
                        .map(list -> convertDatas2Nodes(list, mode))
                        .map(list -> mRepository.saveMenuInfo(list, loginId, mode))
                        .map(list -> getSecondNodesByParentId(list, mode))
                        .compose(TransformerHelper.io2main())
                        .subscribeWith(new ResourceSubscriber<ArrayList<MenuNode>>() {

                            @Override
                            public void onNext(ArrayList<MenuNode> menuNodes) {
                                if (mView != null) {
                                    mView.initModulesSuccess(menuNodes);
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                if (mView != null) {
                                    mView.initModelsFail(t.getMessage());
                                }
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
        addSubscriber(subscriber);
    }

    @Override
    public void changeMode(String loginId, int mode) {
        //1. 根据用户选择的模式，修改数据仓库的mode标识
        mRepository.setLocal(Global.ONLINE_MODE == mode ? false : true);
        //2. 调用setupModule，重新初始化Home界面
        setupModule(loginId);
    }



    private ArrayList<MenuNode> wrapperMenuNodes(ArrayList<MenuNode> list, int mode) {
        ArrayList<MenuNode> menus = new ArrayList<>();
        if (list == null)
            return menus;
        for (MenuNode menuNode : list) {
            menuNode.setMode(mode);
            menus.add(menuNode);
        }
        return menus;
    }

    private ArrayList<MenuNode> mergeMenuNodes(ArrayList<MenuNode> onLineMenus, ArrayList<MenuNode> offLineMenus) {
        ArrayList<MenuNode> menus = new ArrayList<>();
        if ((onLineMenus == null || onLineMenus.size() == 0) && (offLineMenus == null || offLineMenus.size() == 0)) {
            //如果两个菜单都不满足条件，那么返回
            return menus;
        }
        if ((onLineMenus != null && onLineMenus.size() > 0)) {
            menus.addAll(onLineMenus);
            mOnLineMenuRootId = onLineMenus.get(0).getId();
        }
        if ((offLineMenus != null || offLineMenus.size() > 0)) {
            menus.addAll(offLineMenus);
            mOffLineMenuRootId = offLineMenus.get(0).getId();
        }
        return menus;
    }

    private int createModuleIcon(String moduleCode) {
        if (TextUtils.isEmpty(moduleCode))
            return 0;
        switch (moduleCode) {
            case Global.WZYS:
                return R.mipmap.icon_module1;
            case Global.WZRK:
                return R.mipmap.icon_module2;
            case Global.WZCK:
                return R.mipmap.icon_module3;
            case Global.WZTK:
                return R.mipmap.icon_module4;
            case Global.WZYK:
                return R.mipmap.icon_module5;
            case Global.WZTH:
                return R.mipmap.icon_module6;
            case Global.WZPD:
                return R.mipmap.icon_module7;
            case Global.CWTZ:
                return R.mipmap.icon_module8;
            case Global.XXCX:
                return R.mipmap.icon_module11;
            case Global.DGRK:
                return R.mipmap.icon_module14;
            case Global.DGCK:
                return R.mipmap.icon_module12;
            case Global.SETTING:
                return R.mipmap.icon_module13;
            case Global.DGYK:
                return R.mipmap.icon_module14;
            case Global.L_LOADDATA:
                return R.mipmap.icon_module15;
        }
        return 0;
    }

    /**
     * 将一般的数据转换为tree的数据结构
     *
     * @param datas
     * @return
     */
    private ArrayList<MenuNode> convertDatas2Nodes(ArrayList<MenuNode> datas, int mode) {

        /**
         * 设置Node间的节点关系
         */
        for (int i = 0; i < datas.size(); i++) {
            MenuNode n = datas.get(i);
            //这里过滤掉了不是本次模式显示的节点
            if (n.getMode() != mode)
                continue;
            for (int j = i + 1; j < datas.size(); j++) {
                MenuNode m = datas.get(j);
                if (m.getParentId().equals(n.getId())) {
                    n.getChildren().add(m);
                    m.setParent(n);
                } else if (m.getId().equals(n.getParentId())) {
                    m.getChildren().add(n);
                    n.setParent(m);
                }
            }
            //为二级节点生成icon
            if ((mode == Global.ONLINE_MODE &&
                    !TextUtils.isEmpty(mOnLineMenuRootId) &&
                    !n.getParentId().equals(mOnLineMenuRootId)))
                continue;
            if ((mode == Global.OFFLINE_MODE &&
                    !TextUtils.isEmpty(mOffLineMenuRootId) &&
                    !n.getParentId().equals(mOffLineMenuRootId)))
                continue;
            n.setIcon(createModuleIcon(n.getFunctionCode()));
        }
        return datas;
    }

    /**
     * 通过根节点id获取二级节点
     *
     * @param nodes
     * @return
     */
    private ArrayList<MenuNode> getSecondNodesByParentId(ArrayList<MenuNode> nodes, int mode) {
        ArrayList<MenuNode> menuNodes = new ArrayList<>();
        final String rootId = mode == Global.ONLINE_MODE ? mOnLineMenuRootId : mOffLineMenuRootId;
        for (MenuNode node : nodes) {
            if (node.getParentId().equals(rootId)) {
                menuNodes.add(node);
            }
        }
        return menuNodes;
    }
}
