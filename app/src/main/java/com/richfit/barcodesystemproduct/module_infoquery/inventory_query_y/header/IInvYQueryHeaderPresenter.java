package com.richfit.barcodesystemproduct.module_infoquery.inventory_query_y.header;

import android.support.annotation.NonNull;

import com.richfit.common_lib.IInterface.IPresenter;

/**
 * Created by monday on 2017/5/25.
 */

public interface IInvYQueryHeaderPresenter extends IPresenter<IInvYQueryHeaderView> {
    /**
     * 获取单据数据
     *
     * @param refNum：单号
     * @param refType:单据类型
     * @param bizType：业务类型
     * @param moveType:移动类型
     * @param userId：用户loginId
     */
    void getReference(@NonNull String refNum, @NonNull String refType,
                      @NonNull String bizType, @NonNull String moveType,
                      @NonNull String refLineId, @NonNull String userId);
}
