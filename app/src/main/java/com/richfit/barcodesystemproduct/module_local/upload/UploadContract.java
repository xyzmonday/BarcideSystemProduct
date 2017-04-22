package com.richfit.barcodesystemproduct.module_local.upload;

import com.richfit.barcodesystemproduct.base.base_detail.IBaseDetailPresenter;
import com.richfit.barcodesystemproduct.base.base_detail.IBaseDetailView;
import com.richfit.domain.bean.ResultEntity;

import java.util.ArrayList;

/**
 * Created by monday on 2017/4/17.
 */

public interface UploadContract {
    interface View extends IBaseDetailView<ResultEntity> {
        void startUploadData(int totalUploadDataNum);

        void uploadCollectDataSuccess(int taskNum,int totalNum,String message,String transNum);

        void uploadCollectDataFail(int taskNum,int totalNum,String message);

        void uploadCollectDataComplete();

        void showUploadData(ArrayList<ResultEntity> results);

        void readUploadDataFail(String message);

        void readUploadDataComplete();

    }

    interface Presenter extends IBaseDetailPresenter<View> {
        void uploadCollectedDataOffLine();

        void readUploadData();

        void resetStateAfterUpload();
    }
}
