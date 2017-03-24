package com.richfit.barcidesystemproduct.base.base_detail;

import android.content.Context;

import com.richfit.barcodesystemproduct.base.BasePresenter;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.barcodesystemproduct.module.main.MainActivity;
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
    public void submitData2BarcodeSystem(String transId, String bizType, String refType, String userId, String voucherDate, Map<String, Object> flagMap, Map<String, Object> extraHeaderMap) {

    }

    @Override
    public void submitData2SAP(String transId, String bizType, String refType, String userId, String voucherDate, Map<String, Object> flagMap, Map<String, Object> extraHeaderMap) {

    }

    @Override
    public void sapUpAndDownLocation(String transId, String bizType, String refType, String userId, String voucherDate, Map<String, Object> flagMap, Map<String, Object> extraHeaderMap, int submitFlag) {

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
                mSimpleRxBus.post(new BaseDetailFragment.ClearHeaderUIEvent());
            }
        }
    }
}
