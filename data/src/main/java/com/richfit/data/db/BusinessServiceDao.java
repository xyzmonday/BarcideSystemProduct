package com.richfit.data.db;

import android.content.Context;

import com.richfit.common_lib.scope.ContextLife;
import com.richfit.domain.bean.ResultEntity;
import com.richfit.domain.repository.IBusinessService;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by monday on 2017/3/29.
 */

public class BusinessServiceDao extends BaseDao implements IBusinessService {

    @Inject
    public BusinessServiceDao(@ContextLife("Application") Context context) {
        super(context);
    }

    @Override
    public String uploadBusinessDataSingle(ResultEntity param) {
        return null;
    }

    @Override
    public String deleteBusinessData(String refNum, String transId, String refCodeId, String refType, String bizType, String userId, String companyCode) {
        return null;
    }

    @Override
    public String deleteBusinessDataByLineId(String businessType, String transLineId, String transId) {
        return null;
    }

    @Override
    public String deleteBusinessDataByLocationId(String locationId, String transLineId, String transId) {
        return null;
    }

    @Override
    public String uploadBusinessDataOffline(List<ResultEntity> params) {
        return null;
    }
}
