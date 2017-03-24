package com.richfit.common_lib.IInterface;

import com.richfit.common_lib.baseadapterrv.base.ViewHolder;

/**
 * Created by monday on 2017/3/17.
 */

public interface IAdapterState {
    /**
     * 具体业务修改父节点抬头和子节点抬头的字段
     *
     * @param holder
     * @param viewType
     */
    void onBindViewHolder(ViewHolder holder, int viewType);
}
