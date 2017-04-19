package com.richfit.barcodesystemproduct.module_local.upload;

import com.richfit.barcodesystemproduct.base.BaseView;
import com.richfit.common_lib.IInterface.IPresenter;
import com.richfit.domain.bean.ResultEntity;

import java.util.ArrayList;

/**
 * Created by monday on 2017/4/17.
 */

public interface UploadContract {
    interface View extends BaseView {


        void uploadCollectDataSuccess(int taskNum, String message);

        void uploadCollectDataComplete();

        void uploadCollectDataFail(String message);

        void showUploadData(ArrayList<ResultEntity> results);

        void readUploadDataFail(String message);


    }

    interface Presenter extends IPresenter<View> {
        void readUploadData();

        void uploadCollectedDataOffLine();

    }
}
