package com.richfit.barcodesystemproduct.barcodesystem_sdk.ms.base_msn_edit;

import com.richfit.barcodesystemproduct.base.base_edit.IBaseEditView;
import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.ReferenceEntity;

import java.util.List;

/**
 * Created by monday on 2016/11/22.
 */

public interface IMSNEditView extends IBaseEditView {
    /**
     * 输入物料获取缓存后，刷新界面
     *
     * @param refData
     * @param batchFlag
     */
    void onBindCommonUI(ReferenceEntity refData, String batchFlag);

    void loadTransferSingleInfoFail(String message);

    void loadTransferSingleInfoComplete();

    /**
     * 显示库存
     *
     * @param list
     */
    void showInventory(List<InventoryEntity> list);

    void loadInventoryFail(String message);

    /**
     * 加载库存完毕
     */
    void loadInventoryComplete();

    /**
     * 显示接收库存
     * */
    void showRecLocations(List<String> recLocations);
    void loadRecLocationsFail(String message);

}
