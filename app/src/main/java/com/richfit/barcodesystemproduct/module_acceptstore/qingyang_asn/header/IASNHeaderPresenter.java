package com.richfit.barcodesystemproduct.module_acceptstore.qingyang_asn.header;


import com.richfit.barcodesystemproduct.base.base_header.IBaseHeaderPresenter;

/**
 * Created by monday on 2016/11/16.
 */

public interface IASNHeaderPresenter extends IBaseHeaderPresenter<IASNHeaderView> {

    /**
     * 初始化工厂列表
     */
    void getWorks(int flag);

    /**
     * 初始化供应商列表
     *
     * @param workCode:工厂编码
     */
    void getSupplierList(String workCode, String keyWord, int defaultItemNum, int flag);

    /**
     * 获取移动类型列表
     */
    void getMoveTypeList(int flag);

    /**
     * 删除整单缓存
     * @param bizType：业务类型
     * @param userId:用户id
     */
    void deleteCollectionData(String refType, String bizType, String userId, String companyCode);
}
