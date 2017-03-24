package com.richfit.barcidesystemproduct.module_acceptstore.ww_component.detail;

import com.richfit.barcodesystemproduct.base.base_detail.IBaseDetailPresenter;
import com.richfit.barcodesystemproduct.base.base_detail.IBaseDetailView;

/**
 * Created by monday on 2017/3/10.
 */

public interface QingHaiWWCDetailContract {

    interface IQingHaiWWCDetailView<T> extends IBaseDetailView<T> {

    }

    interface IQingHaiWWCDetailPresenter extends IBaseDetailPresenter<IQingHaiWWCDetailView> {
        /**
         * 获取整单缓存
         *
         * @param refNum：单据号
         * @param refCodeId：单据id
         * @param bizType:业务类型
         * @param refType：单据类型
         */
        void getTransferInfo(String refNum, String refCodeId, String bizType, String refType,
                             String moveType, String refLineId, String userId);
    }
}
