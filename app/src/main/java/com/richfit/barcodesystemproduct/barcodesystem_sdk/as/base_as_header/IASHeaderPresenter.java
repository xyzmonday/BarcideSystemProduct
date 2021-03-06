package com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_header;

import com.richfit.barcodesystemproduct.base.base_header.IBaseHeaderPresenter;
import com.richfit.domain.bean.ReferenceEntity;


/**
 * 物资入库抬头标准接口
 * Created by monday on 2016/11/11.
 */

public interface IASHeaderPresenter extends IBaseHeaderPresenter<IASHeaderView> {

    void getReference(String refNum, String refType, String bizType,
                      String moveType, String refLineId, String userId);

    /**
     * 获取整单缓存
     *
     * @param refData：抬头界面获取的单据数据
     * @param refCodeId：单据id
     * @param bizType:业务类型
     * @param refType：单据类型
     */
    void getTransferInfo(ReferenceEntity refData, String refCodeId, String bizType, String refType);

    /**
     * 删除整单缓存
     *
     * @param refNum:单据号
     * @param transId:缓存抬头id
     * @param bizType:业务类型
     */
    void deleteCollectionData(String refNum, String transId, String refCodeId, String refType, String bizType,
                              String userId, String companyCode);

}
