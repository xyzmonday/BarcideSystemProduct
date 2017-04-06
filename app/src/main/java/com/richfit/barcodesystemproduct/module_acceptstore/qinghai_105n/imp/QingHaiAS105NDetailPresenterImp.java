package com.richfit.barcodesystemproduct.module_acceptstore.qinghai_105n.imp;

import android.content.Context;
import android.text.TextUtils;

import com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_detail.imp.ASDetailPresenterImp;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;

import java.util.List;

import javax.inject.Inject;

public class QingHaiAS105NDetailPresenterImp extends ASDetailPresenterImp {

    @Inject
    public QingHaiAS105NDetailPresenterImp(@ContextLife("Activity") Context context) {
        super(context);
    }

    /**
     * 通过lineNum105将缓存和原始单据行关联起来
     */
    @Override
    protected RefDetailEntity getLineDataByRefLineId(RefDetailEntity refLineData, ReferenceEntity cachedRefData) {
        if (refLineData == null) {
            return null;
        }

        final String lineNum105 = refLineData.lineNum105;
        if (TextUtils.isEmpty(lineNum105))
            return null;
        //通过refLineId匹配出缓存中的明细行
        List<RefDetailEntity> detail = cachedRefData.billDetailList;
        for (RefDetailEntity entity : detail) {
            if (lineNum105.equals(entity.lineNum105)) {
                return entity;
            }
        }
        return null;
    }

}
