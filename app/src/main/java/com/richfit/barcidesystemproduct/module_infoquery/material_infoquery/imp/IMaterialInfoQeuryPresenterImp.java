package com.richfit.barcidesystemproduct.module_infoquery.material_infoquery.imp;

import android.content.Context;

import com.richfit.barcodesystemproduct.base.BasePresenter;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.barcodesystemproduct.module_infoquery.material_infoquery.IMaterialInfoQeuryPresenter;
import com.richfit.barcodesystemproduct.module_infoquery.material_infoquery.IMaterialInfoQeuryView;
import com.richfit.common_lib.rxutils.RxSubscriber;
import com.richfit.common_lib.rxutils.TransformerHelper;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.MaterialEntity;

import javax.inject.Inject;

/**
 * Created by monday on 2017/3/16.
 */

public class IMaterialInfoQeuryPresenterImp extends BasePresenter<IMaterialInfoQeuryView>
        implements IMaterialInfoQeuryPresenter {

    IMaterialInfoQeuryView mView;

    @Inject
    public IMaterialInfoQeuryPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void getMaterialInfo(String queryType, String materialNum) {
        mView = getView();
        RxSubscriber<MaterialEntity> subscriber = mRepository.getMaterialInfo(queryType, materialNum)
                .compose(TransformerHelper.io2main())
                .subscribeWith(new RxSubscriber<MaterialEntity>(mContext, "正在查询...") {
                    @Override
                    public void _onNext(MaterialEntity materialEntity) {
                        if (mView != null) {
                            mView.querySuccess(materialEntity);
                        }
                    }

                    @Override
                    public void _onNetWorkConnectError(String message) {
                        if (mView != null) {
                            mView.networkConnectError(Global.RETRY_QUERY_MATERIAL_INFO);
                        }
                    }

                    @Override
                    public void _onCommonError(String message) {
                        if (mView != null) {
                            mView.queryFail(message);
                        }
                    }

                    @Override
                    public void _onServerError(String code, String message) {
                        if (mView != null) {
                            mView.queryFail(message);
                        }
                    }

                    @Override
                    public void _onComplete() {

                    }
                });
        addSubscriber(subscriber);
    }
}
