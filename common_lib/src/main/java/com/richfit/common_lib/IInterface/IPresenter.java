package com.richfit.common_lib.IInterface;

import com.richfit.domain.bean.RowConfig;

import java.util.List;

/**
 * mvp的presenter层
 * Created by monday on 2016/9/30.
 */

public interface IPresenter<T extends IView> {
    /**
     * 绑定View
     * @param view
     */
    void attachView(T view);
    void detachView();

    void setLocal(boolean isLocal);
    boolean isLocal();
}
