package com.richfit.barcodesystemproduct.module_acceptstore.qingyang_asn.edit.imp;

import android.content.Context;

import com.richfit.barcodesystemproduct.base.base_edit.BaseEditPresenterImp;
import com.richfit.barcodesystemproduct.module_acceptstore.qingyang_asn.edit.IASNEditPresenter;
import com.richfit.barcodesystemproduct.module_acceptstore.qingyang_asn.edit.IASNEditView;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.ReferenceEntity;

import javax.inject.Inject;

/**
 * Created by monday on 2016/12/1.
 */

public class ASNEditPresenterImp extends BaseEditPresenterImp<IASNEditView>
        implements IASNEditPresenter {

    @Inject
    public ASNEditPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void getTransferInfoSingle(String bizType, String materialNum, String userId,
                                      String workId, String invId, String recWorkId, String recInvId, String batchFlag,
                                      String refDoc,int refDocItem) {
        mView = getView();
        RxSubscriber<ReferenceEntity> subscriber = mRepository.getTransferInfoSingle("","",bizType, "",
                workId,invId,recWorkId,recInvId,materialNum,batchFlag,"",refDoc,refDocItem,userId)
                .compose(TransformerHelper.io2main())
                .subscribeWith(new RxSubscriber<ReferenceEntity>(mContext) {
                    @Override
                    public void _onNext(ReferenceEntity refData) {
                        if(mView != null) {
                            mView.onBindCommonUI(refData,batchFlag);
                        }
                    }

                    @Override
                    public void _onNetWorkConnectError(String message) {
                        if(mView != null) {
                            mView.networkConnectError(Global.RETRY_LOAD_SINGLE_CACHE_ACTION);
                        }
                    }

                    @Override
                    public void _onCommonError(String message) {
                        if(mView != null) {
                            mView.loadTransferSingleInfoFail(message);
                        }
                    }

                    @Override
                    public void _onServerError(String code, String message) {
                        if(mView != null) {
                            mView.loadTransferSingleInfoFail(message);
                        }
                    }

                    @Override
                    public void _onComplete() {
                        if(mView != null) {
                            mView.loadTransferSingeInfoComplete();
                        }
                    }
                });
        addSubscriber(subscriber);
    }
}
