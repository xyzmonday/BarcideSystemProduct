package com.richfit.barcodesystemproduct.module_local.upload;

import com.richfit.barcodesystemproduct.base.base_detail.IBaseDetailPresenter;
import com.richfit.barcodesystemproduct.base.base_detail.IBaseDetailView;
import com.richfit.domain.bean.ResultEntity;
import com.richfit.domain.bean.UploadMsgEntity;

import java.util.ArrayList;

/**
 * Created by monday on 2017/4/17.
 */

public interface UploadContract {
    interface View extends IBaseDetailView<ResultEntity> {
        void startUploadData(int totalUploadDataNum);

        void uploadCollectDataSuccess(UploadMsgEntity uploadInfo);

        void uploadCollectDataFail(UploadMsgEntity uploadInfo);

        void uploadCollectDataComplete();

        void showUploadData(ArrayList<ResultEntity> results);

        void readUploadDataFail(String message);

        void readUploadDataComplete();

    }

    interface Presenter extends IBaseDetailPresenter<View> {
        void uploadCollectedDataOffLine();

        void uploadInspectionDataOffLine();

        void uploadCheckDataOffline();

        void readUploadData(int bizType);

        void resetStateAfterUpload();
    }
}
