package com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_dsn_header;

import com.richfit.barcodesystemproduct.base.base_header.IBaseHeaderPresenter;

/**
 * Created by monday on 2017/2/23.
 */

public interface IDSNHeaderPresenter extends IBaseHeaderPresenter<IDSNHeaderView> {

    void getWorks(int flag);
    void getAutoCompleteList(String workCode, String keyWord, int defaultItemNum, int flag, String bizType);
    /**
     * 删除整单缓存
     * @param bizType：业务类型
     * @param userId:用户id
     */
    void deleteCollectionData(String refType, String bizType, String userId,
                              String companyCode);
}
