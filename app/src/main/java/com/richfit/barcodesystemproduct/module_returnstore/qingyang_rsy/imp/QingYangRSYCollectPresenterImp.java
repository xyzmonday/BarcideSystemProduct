package com.richfit.barcodesystemproduct.module_returnstore.qingyang_rsy.imp;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_collect.imp.ASCollectPresenterImp;
import com.richfit.common_lib.scope.ContextLife;

import javax.inject.Inject;

/**
 * Created by monday on 2017/3/29.
 */

public class QingYangRSYCollectPresenterImp extends ASCollectPresenterImp {

    @Inject
    public QingYangRSYCollectPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    /**
     * 庆阳检查仓位
     * @param queryType
     * @param workId
     * @param invId
     * @param batchFlag
     * @param location
     */
    @Override
    public void checkLocation(String queryType, String workId, String invId, String batchFlag, String location) {
        mView = getView();
        if (TextUtils.isEmpty(workId) && mView != null) {
            mView.checkLocationFail("工厂为空");
            return;
        }

        if (TextUtils.isEmpty(invId) && mView != null) {
            mView.checkLocationFail("库存地点为空");
            return;
        }

        mView.checkLocationSuccess(batchFlag, location);
    }
}
