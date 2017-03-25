package com.richfit.barcodesystemproduct.base.base_detail;

import com.richfit.common_lib.IInterface.IPresenter;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;

import java.util.ArrayList;
import java.util.Map;

/**
 * 注意如果Presenter实现类实现了该接口，那么View必须实现IBaseDetailView接口
 * Created by monday on 2017/3/17.
 */

public interface IBaseDetailPresenter<V extends IBaseDetailView> extends IPresenter<V> {

    /**
     * 获取整单缓存。这里给出的是有参考和无参考两大类获取整单缓存的接口。
     *
     * @param refData：抬头界面获取的单据数据
     * @param refCodeId：单据id
     * @param bizType:业务类型
     * @param refType：单据类型
     */
    void getTransferInfo(ReferenceEntity refData, String refCodeId, String bizType, String refType,
                         String userId, String workId, String invId, String recWorkId, String recInvId);

    /**
     * 删除一个子节点
     *
     * @param lineDeleteFlag：是否删除整行(Y/N)
     * @param transId：缓存头id
     * @param transLineId:缓存行id
     * @param locationId:缓存仓位id
     * @param bizType:业务类型
     * @param position：该子节点在所有节点中的真实位置
     */
    void deleteNode(String lineDeleteFlag, String transId, String transLineId, String locationId,
                    String refType, String bizType, int position, String companyCode);

    /**
     * 修改子节点
     *
     * @param sendLocations:对于没有父子节点的明细需要传入已经上架的仓位(发出仓位)
     * @param refData：单据数据
     * @param node：需要修改的子节点
     * @param subFunName:子功能编码
     * @param position :父节点在明细中的位置，如果是父子节点结构的明细那么position为-1
     */
    void editNode(ArrayList<String> sendLocations, ArrayList<String> recLocations,
                  ReferenceEntity refData, RefDetailEntity node, String companyCode,
                  String bizType, String refType, String subFunName, int position);

    /**
     * 上传入库明细到条码系统
     *
     * @param transId：缓存头id
     * @param bizType:业务类型
     * @param voucherDate:过账日期
     */
    void submitData2BarcodeSystem(String transId, String bizType, String refType, String userId, String voucherDate,
                                  Map<String, Object> flagMap, Map<String, Object> extraHeaderMap);

    /**
     * 提交数据到sap
     *
     * @param transId：缓存头id
     * @param bizType:业务类型
     * @param voucherDate:过账日期
     * @param userId：用户id
     */
    void submitData2SAP(String transId, String bizType, String refType, String userId, String voucherDate,
                        Map<String, Object> flagMap, Map<String, Object> extraHeaderMap);

    /**
     * sap上下架处理
     *
     * @param transId
     * @param bizType
     * @param refType
     * @param userId
     * @param voucherDate
     * @param flagMap
     * @param extraHeaderMap
     * @param submitFlag
     */
    void sapUpAndDownLocation(String transId, String bizType, String refType, String userId, String voucherDate,
                              Map<String, Object> flagMap, Map<String, Object> extraHeaderMap, int submitFlag);

    /**
     * 数据提交到sap后，从数据明细界面跳转到抬头界面
     *
     * @param position
     */
    void showHeadFragmentByPosition(int position);
}
