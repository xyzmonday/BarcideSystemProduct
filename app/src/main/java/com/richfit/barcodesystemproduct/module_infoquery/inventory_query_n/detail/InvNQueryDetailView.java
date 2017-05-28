package com.richfit.barcodesystemproduct.module_infoquery.inventory_query_n.detail;

import com.richfit.barcodesystemproduct.base.base_detail.IBaseDetailView;
import com.richfit.domain.bean.InventoryEntity;

import java.util.List;

/**
 * Created by monday on 2017/5/25.
 */

public interface InvNQueryDetailView extends IBaseDetailView<InventoryEntity> {
    /**
     * 显示库存
     * @param list
     */
    void showInventory(List<InventoryEntity> list);
    void loadInventoryFail(String message);

}
