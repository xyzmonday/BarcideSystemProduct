package com.richfit.barcodesystemproduct.module_acceptstore.qinghai_105.imp;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_detail.imp.ASDetailPresenterImp;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;

import java.util.List;

import javax.inject.Inject;

public class QingHaiAS105DetailPresenterImp extends ASDetailPresenterImp {

    @Inject
    public QingHaiAS105DetailPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    /**
     * 通过insLot将缓存和原始单据行关联起来
     */
    @Override
    protected RefDetailEntity getLineDataByRefLineId(RefDetailEntity refLineData, ReferenceEntity cachedRefData) {
        if (refLineData == null) {
            return null;
        }
        final String insLot = refLineData.insLot;
        if (TextUtils.isEmpty(insLot))
            return null;
        //通过refLineId匹配出缓存中的明细行
        List<RefDetailEntity> detail = cachedRefData.billDetailList;
        for (RefDetailEntity entity : detail) {
            if (insLot.equals(entity.insLot)) {
                return entity;
            }
        }
        return null;
    }

}