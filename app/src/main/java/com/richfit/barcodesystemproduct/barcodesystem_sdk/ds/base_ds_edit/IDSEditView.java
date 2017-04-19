package com.richfit.barcodesystemproduct.barcodesystem_sdk.ds.base_ds_edit;


import com.richfit.barcodesystemproduct.base.base_edit.IBaseEditView;
import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.RefDetailEntity;

import java.util.List;

/**
 * Created by monday on 2016/11/21.
 */

public interface IDSEditView extends IBaseEditView {
    /**
     * 显示库存
     * @param list
     */
    void showInventory(List<InventoryEntity> list);
    void loadInventoryFail(String message);

    /**
     * 获取缓存成功
     * @param cache
     * @param batchFlag
     * @param location
     */
    void onBindCache(RefDetailEntity cache, String batchFlag, String location);
    void loadCacheFail(String message);
}
