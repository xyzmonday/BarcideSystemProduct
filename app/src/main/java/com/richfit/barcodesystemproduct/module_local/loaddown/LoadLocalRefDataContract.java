package com.richfit.barcodesystemproduct.module_local.loaddown;

import com.richfit.barcodesystemproduct.base.BaseView;
import com.richfit.common_lib.IInterface.IPresenter;
import com.richfit.domain.bean.MenuNode;
import com.richfit.domain.bean.ReferenceEntity;

import java.util.ArrayList;

/**
 * Created by monday on 2016/11/1.
 */

public interface LoadLocalRefDataContract {

    interface View extends BaseView {
        /**
         * 获取单据数据成功，提示用户在明细列表中查看
         */
        void getReferenceInfoSuccess(ReferenceEntity data);

        /**
         * 获取单据数据完成那么刷新界面
         */
        void getReferenceInfoComplete();
        /**
         * 获取单据数据失败，提示用户错误信息
         *
         * @param message
         */
        void getReferenceInfoFail(String message);

        void readMenuInfoSuccess(ArrayList<MenuNode> list);
        void readMenuInfoFail(String message);
    }

    interface Presenter extends IPresenter<View> {
        /**
         * 用户扫描获取某一张单据的数据，获取成功后保存到本地，并且刷新历史单据数据列表，显示
         * 在界面。
         *
         * @param refNum
         * @param refType
         * @param bizType
         */
        void getReferenceInfo(String refNum, String refType, String bizType,
                              String moveType, String refLineId, String userId);

        /**
         * 读取菜单信息
         * @param loginId
         * @param mode
         */
        void readMenuInfo(String loginId, int mode);
    }


}
