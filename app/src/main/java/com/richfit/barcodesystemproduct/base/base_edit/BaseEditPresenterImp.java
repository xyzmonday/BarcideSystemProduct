package com.richfit.barcodesystemproduct.base.base_edit;

import android.content.Context;

import com.richfit.barcodesystemproduct.base.BasePresenter;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.domain.bean.ResultEntity;

import javax.inject.Inject;

/**
 * Created by monday on 2017/4/6.
 */

public class BaseEditPresenterImp<V extends IBaseEditView> extends BasePresenter<V>
implements IBaseEditPresenter<V>{

    @Inject
    public BaseEditPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    @Override
    public void uploadCollectionDataSingle(ResultEntity result) {

    }
}
