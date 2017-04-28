package com.richfit.barcodesystemproduct.base.base_header;

import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.ResultEntity;

/**
 * Created by monday on 2017/4/28.
 */

public abstract class BaseHeaderFragment<P extends IBaseHeaderPresenter> extends
        BaseFragment<P> implements IBaseHeaderView {

    /*离线保存的抬头数据*/
    protected ResultEntity mLocalHeaderResult;


    @Override
    public boolean checkDataBeforeOperationOnHeader() {
        if (mRefData == null) {
            showMessage("请先获取单据数据");
            return false;
        }
        if (TextUtils.isEmpty(mBizType)) {
            showMessage("未获取到业务类型");
            return false;
        }
        if (TextUtils.isEmpty(mLocalTransId)) {
            showMessage("缓存标识为空");
            return false;
        }
        return true;
    }


    @Override
    public void operationOnHeader(final String companyCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("温馨提示")
                .setMessage("您是否要保存修改的抬头数据")
                .setPositiveButton("修改", (dialog, which) -> {
                    dialog.dismiss();
                    mPresenter.uploadEditedHeadData(mLocalHeaderResult);

                }).setNegativeButton("取消", (dialog, which) -> dialog.dismiss()).show();
    }

    @Override
    public void uploadEditedHeadDataFail(String message) {
        showMessage(message);
    }

    @Override
    public void uploadEditedHeadComplete() {
        showMessage("保存成功!");
    }

    /**
     * 网络错误重试
     *
     * @param action
     */
    @Override
    public void retry(String action) {
        switch (action) {
            case Global.RETRY_SAVE_COLLECTION_DATA_ACTION:
                mPresenter.uploadEditedHeadData(mLocalHeaderResult);
                break;
        }
        super.retry(action);
    }

    @Override
    public void onDestroyView() {
        //如果用户是从数据上传过来的，退出时需要将isLocalFlag设置回去
        mPresenter.setLocal(!TextUtils.isEmpty(mLocalTransId));
        super.onDestroyView();
    }
}
