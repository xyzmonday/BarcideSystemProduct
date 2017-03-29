package com.richfit.data.db;

import android.content.Context;

import com.richfit.common_lib.scope.ContextLife;
import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.repository.ICheckServiceDao;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by monday on 2017/3/29.
 */

public class CheckServiceDao extends BaseDao implements ICheckServiceDao {

    @Inject
    public CheckServiceDao(@ContextLife("Application") Context context) {
        super(context);
    }

    @Override
    public ReferenceEntity getCheckInfo(String userId, String bizType, String checkLevel, String checkSpecial, String storageNum, String workId, String invId, String checkNum) {
        return null;
    }

    @Override
    public boolean deleteCheckData(String storageNum, String workId, String invId, String checkId, String userId, String bizType) {
        return false;
    }

    @Override
    public List<InventoryEntity> getCheckTransferInfoSingle(String checkId, String materialId, String materialNum, String location, String bizType) {
        return null;
    }

    @Override
    public ReferenceEntity getCheckTransferInfo(String checkId, String materialNum, String location, String isPageQuery, int pageNum, int pageSize, String bizType) {
        return null;
    }

    @Override
    public boolean deleteCheckDataSingle(String checkId, String checkLineId, String userId, String bizType) {
        return false;
    }
}
