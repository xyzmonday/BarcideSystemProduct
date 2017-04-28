package com.richfit.barcodesystemproduct.base.base_detail;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.base.BasePresenter;
import com.richfit.barcodesystemproduct.module.main.MainActivity;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by monday on 2017/3/18.
 */

public class BaseDetailPresenterImp<V extends IBaseDetailView> extends BasePresenter<V> implements IBaseDetailPresenter<V> {

    protected V mView;

    @Inject
    public BaseDetailPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void getTransferInfo(ReferenceEntity refData, String refCodeId, String bizType, String refType,
                                String userId, String workId, String invId, String recWorkId, String recInvId) {

    }

    @Override
    public void deleteNode(String lineDeleteFlag, String transId, String transLineId, String locationId, String refType, String bizType, int position, String companyCode) {

    }

    @Override
    public void editNode(ArrayList<String> sendLocations, ArrayList<String> recLocations,
                         ReferenceEntity refData, RefDetailEntity node, String companyCode, String bizType, String refType, String subFunName, int position) {

    }

    @Override
    public void submitData2BarcodeSystem(String transId, String bizType, String refType, String userId, String voucherDate, String transToSapFlag, Map<String, Object> extraHeaderMap) {

    }

    @Override
    public void submitData2SAP(String transId, String bizType, String refType, String userId, String voucherDate, String transToSapFlag, Map<String, Object> extraHeaderMap) {

    }

    @Override
    public void sapUpAndDownLocation(String transId, String bizType, String refType, String userId, String voucherDate,String transToSapFlag, Map<String, Object> extraHeaderMap, int submitFlag) {

    }

    /**
     * 子类不可以重写该方法，注意这里不让重写的目的是统一发送清除抬头UI控件的信号
     * @param position
     */
    @Override
    final public void showHeadFragmentByPosition(int position) {
        if (MainActivity.class.isInstance(mContext)) {
            MainActivity activity = (MainActivity) mContext;
            activity.showFragmentByPosition(position);
            if (mSimpleRxBus.hasSubscribers()) {
                mSimpleRxBus.post(true);
            }
        }
    }

    @Override
    public void setTransFlag(String bizType,String transFlag) {
        mView = getView();
        if(TextUtils.isEmpty(bizType) || TextUtils.isEmpty(transFlag)) {
            return;
        }
        RxSubscriber<String> subscriber = mRepository.setTransFlag(bizType, transFlag)
                .compose(TransformerHelper.io2main())
                .subscribeWith(new RxSubscriber<String>(mContext, "正在结束本次操作") {
                    @Override
                    public void _onNext(String s) {

                    }

                    @Override
                    public void _onNetWorkConnectError(String message) {
                        if (mView != null) {
                            mView.networkConnectError(Global.RETRY_SET_TRANS_FLAG_ACTION);
                        }
                    }

                    @Override
                    public void _onCommonError(String message) {
                        if(mView != null) {
                            mView.setTransFlagFail(message);
                        }
                    }

                    @Override
                    public void _onServerError(String code, String message) {
                        if(mView != null) {
                            mView.setTransFlagFail(message);
                        }
                    }

                    @Override
                    public void _onComplete() {
                        if(mView != null) {
                            mView.setTransFlagsComplete();
                        }
                    }
                });
        addSubscriber(subscriber);

    }
}
