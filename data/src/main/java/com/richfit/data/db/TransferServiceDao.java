package com.richfit.data.db;

import android.content.Context;

import com.richfit.common_lib.scope.ContextLife;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.repository.ITransferServiceDao;

import javax.inject.Inject;

/**
 * Created by monday on 2017/3/29.
 */

public class TransferServiceDao extends BaseDao implements ITransferServiceDao {

    @Inject
    public TransferServiceDao(@ContextLife("Application") Context context) {
        super(context);
    }

    @Override
    public ReferenceEntity getInspectTransferInfoSingle(String refCodeId, String refType, String bizType, String refLineId, String workId, String invId, String recWorkId, String recInvId, String materialNum, String batchFlag, String location, String refDoc, int refDocItem, String userId) {
        return null;
    }

    @Override
    public ReferenceEntity getBusinessTransferInfo(String refCodeId, String refType, String bizType, String refLineId, String workId, String invId, String recWorkId, String recInvId, String materialNum, String batchFlag, String location, String refDoc, int refDocItem, String userId) {
        return null;
    }

    @Override
    public ReferenceEntity getBusinessTransferInfoRef(String refCodeId, String refType, String bizType, String refLineId, String workId, String invId, String recWorkId, String recInvId, String materialNum, String batchFlag, String location, String refDoc, int refDocItem, String userId) {
        return null;
    }

    @Override
    public ReferenceEntity getBusinessTransferInfoSingle(String refCodeId, String refType, String bizType, String refLineId, String workId, String invId, String recWorkId, String recInvId, String materialNum, String batchFlag, String location, String refDoc, int refDocItem, String userId) {
        return null;
    }

    @Override
    public ReferenceEntity getBusinessTransferInfoSingleRef(String refCodeId, String refType, String bizType, String refLineId, String workId, String invId, String recWorkId, String recInvId, String materialNum, String batchFlag, String location, String refDoc, int refDocItem, String userId) {
        return null;
    }
}
