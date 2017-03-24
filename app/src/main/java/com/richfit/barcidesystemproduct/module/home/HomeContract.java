package com.richfit.barcidesystemproduct.module.home;

import com.richfit.barcodesystemproduct.base.BaseView;
import com.richfit.common_lib.IInterface.IPresenter;
import com.richfit.domain.bean.MenuNode;

import java.util.List;

/**
 * Created by monday on 2016/11/7.
 */

public interface HomeContract {

    interface View extends BaseView {
        void initModulesSuccess(List<MenuNode> modules);
        void initModelsFail(String message);
    }

    interface Presenter extends IPresenter<View> {
        /*初始化每一个模块的基本配置*/
        void setupModule(String loginId);
    }
}
