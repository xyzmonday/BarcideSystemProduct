package com.richfit.barcodesystemproduct.barcodesystem_sdk.as.base_as_detail;

import android.text.TextUtils;
import android.widget.TextView;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.ASYDetailAdapter;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.base.base_detail.BaseDetailFragment;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.SPrefUtil;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.TreeNode;

import java.util.List;

import butterknife.BindView;

import static com.richfit.common_lib.utils.SPrefUtil.getData;

/**
 * 物资基础类，从这里开始确定入库明细界面的布局。
 * 注意这里已经到了物资入库，已经确定了明细界面数据的具体类型，所以
 * 可以提供有关于明细数据的相关数据。
 * Created by monday on 2017/3/17.
 */

public abstract class BaseASDetailFragment<P extends IASDetailPresenter> extends
        BaseDetailFragment<P, RefDetailEntity> implements IASDetailView<RefDetailEntity> {

    /*物资入库明细界面的公共组件(注意这必须只能给出父节点上的公共组件，因为如果明细时父子节点结构，那么子节点的公共组件
    不能抽象出来)*/
    /*特殊库存标识*/
    @BindView(R.id.specailInventoryFlag)
    protected TextView tvSpecialInvFag;
    @BindView(R.id.refLineNum)
    /*参考单据行*/
    protected TextView tvRefLineNum;
    /*检验批*/
    @BindView(R.id.insLot)
    protected TextView tvInsLot;
    /*参考物料凭证*/
    @BindView(R.id.refDoc)
    protected TextView tvRefDoc;
    /*参考物料凭证行号*/
    @BindView(R.id.refDocItem)
    protected TextView tvRefDocItem;
    /*应收数量*/
    @BindView(R.id.actQuantity)
    protected TextView tvActQuantity;
    /*库存地点*/
    @BindView(R.id.inv)
    protected TextView tvInv;
    /*工厂*/
    @BindView(R.id.work)
    protected TextView tvWork;


    @Override
    protected int getContentId() {
        return R.layout.fragment_base_asy_detail;
    }

    /**
     * 判断有参考情况下，抬头需要采集的基本字段信息。
     * 子类如果需要检测更多的字段应该重写该方法。
     */
    @Override
    public void initDataLazily() {
        if (mRefData == null) {
            showMessage("请现在抬头界面获取单据数据");
            return;
        }

        if (TextUtils.isEmpty(mRefData.recordNum)) {
            showMessage("请现在抬头界面输入验收清单号");
            return;
        }

        if (TextUtils.isEmpty(mRefData.bizType)) {
            showMessage("未获取到业务类型");
            return;
        }

        if (TextUtils.isEmpty(mRefData.refType)) {
            showMessage("未获取到单据类型");
            return;
        }

        if (mSubFunEntity.headerConfigs != null && !checkExtraData(mSubFunEntity.headerConfigs, mRefData.mapExt)) {
            showMessage("请在抬头界面输入额外必输字段信息");
            return;
        }
        //开始刷新
        startAutoRefresh();
    }

    /**
     * 显示物资入库明细界面，注意这里显示的标准界面
     *
     * @param allNodes
     */
    @Override
    public void showNodes(List<RefDetailEntity> allNodes) {
        saveTransId(allNodes);
        if (mAdapter == null) {
            mAdapter = new ASYDetailAdapter(mActivity, allNodes, mSubFunEntity.parentNodeConfigs,
                    mSubFunEntity.childNodeConfigs);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemEditAndDeleteListener(this);
            mAdapter.setAdapterStateListener(this);
        } else {
            mAdapter.addAll(allNodes);
        }
    }

    @Override
    public void refreshComplete() {

    }

    /**
     * 保存明细节点的TransId
     */
    protected void saveTransId(List<RefDetailEntity> allNodes) {
        for (RefDetailEntity node : allNodes) {
            if (!TextUtils.isEmpty(node.transId)) {
                mTransId = node.transId;
                break;
            }
        }
    }

    /**
     * 修改明细里面的子节点。注意如果该明细界面不具有父子节点结构那么需要重写该方法。
     *
     * @param node
     * @param position
     */
    @Override
    public void editNode(RefDetailEntity node, int position) {
        String state = (String) getData(mBizType + mRefType, "0");
        if (!"0".equals(state)) {
            showMessage("已经过账,不允许修改");
            return;
        }
        mPresenter.editNode(null,null, mRefData, node, mCompanyCode, mBizType,
                mRefType, getSubFunName(), -1);
    }

    /**
     * 删除子节点信息。注意如果该明细界面不具有父子节点结构那么需要重写该方法。
     * @param node
     * @param position
     */
    @Override
    public void deleteNode(RefDetailEntity node, int position) {
        String state = (String) getData(mBizType + mRefType, "0");
        if (!"0".equals(state)) {
            showMessage("已经过账,不允许删除");
            return;
        }
        TreeNode parentNode = node.getParent();
        String lineDeleteFlag;
        if (parentNode == null) {
            lineDeleteFlag = "N";
        } else {
            lineDeleteFlag = parentNode.getChildren().size() > 1 ? "N" : "Y";
        }
        mPresenter.deleteNode(lineDeleteFlag, node.transId, node.transLineId,
                node.locationId, mRefData.refType, mRefData.bizType, position, mCompanyCode);
    }

    /**
     * 删除明细节点成功。如果不具有父子节点结构的明细界面，那么子类需要重写
     *
     * @param position：节点在明细列表的位置
     */
    @Override
    public void deleteNodeSuccess(int position) {
        showMessage("删除成功");
        if (mAdapter != null) {
            mAdapter.removeNodeByPosition(position);
        }
    }

    /**
     * 显示过账，上传等底部菜单之前进行必要的检查。注意子类可以根据自己的需求
     * 自行添加检查的字段。父类仅仅做了最基本的检查。
     * @return
     */
    @Override
    public boolean checkDataBeforeOperationOnDetail() {
        if (mRefData == null) {
            showMessage("请先获取单据数据");
            return false;
        }
        if (TextUtils.isEmpty(mTransId)) {
            showMessage("未获取缓存标识");
            return false;
        }

        if (TextUtils.isEmpty(mRefData.voucherDate)) {
            showMessage("请先选择过账日期");
            return false;
        }
        return true;
    }

    /**
     * 1.过账
     */
    protected void submit2BarcodeSystem(String transToSapFlag) {
        String state = (String) SPrefUtil.getData(mBizType + mRefType, "0");
        if (!"0".equals(state)) {
            showMessage(getString(R.string.detail_on_location));
            return;
        }
        mTransNum = "";
        mFlagMap.clear();
        mFlagMap.put("transToSapFlag", transToSapFlag);
        mPresenter.submitData2BarcodeSystem(mTransId, mBizType, mRefType, Global.USER_ID,
                mRefData.voucherDate, mFlagMap, createExtraHeaderMap());
    }

    /**
     * 第一步过账成功显示物料凭证
     */
    @Override
    public void submitBarcodeSystemSuccess() {
        showSuccessDialog(mTransNum);
    }

    /**
     * 第一步过账失败显示错误信息
     * @param message
     */
    @Override
    public void submitBarcodeSystemFail(String message) {
        mTransNum = "";
        showErrorDialog(TextUtils.isEmpty(message) ? "过账失败" : message);
    }

    /**
     * 2.数据上传
     */
    protected void submit2SAP(String transToSapFlag) {
        String state = (String) getData(mBizType + mRefType, "0");
        if ("0".equals(state)) {
            showMessage("请先过账");
            return;
        }
        mFlagMap.clear();
        mFlagMap.put("transToSapFlag", transToSapFlag);
        mInspectionNum = "";
        mPresenter.submitData2SAP(mTransId, mRefData.bizType, mRefType, Global.USER_ID,
                mRefData.voucherDate, mFlagMap, createExtraHeaderMap());
    }

    /**
     * 第二步数据上传成功
     */
    @Override
    public void submitSAPSuccess() {
        setRefreshing(false, "上架成功");
        showSuccessDialog(mInspectionNum);
        if (mAdapter != null) {
            mAdapter.removeAllVisibleNodes();
        }
        //注意这里必须清除单据数据
        mRefData = null;
        mTransNum = "";
        mTransId = "";
        mPresenter.showHeadFragmentByPosition(BaseFragment.HEADER_FRAGMENT_INDEX);
    }

    /**
     * 第二步上架失败显示失败信息
     * @param messages
     */
    @Override
    public void submitSAPFail(String[] messages) {
        mInspectionNum = "";
        showErrorDialog(messages);
    }

    protected void sapUpAndDownLocation(String transToSapFlag) {

    }

    @Override
    public void upAndDownLocationFail(String[] messages) {

    }

    @Override
    public void upAndDownLocationSuccess() {

    }

    @Override
    protected boolean checkTransStateBeforeRefresh() {
        String transferFlag = (String) getData(mBizType + mRefType, "0");
        if ("1".equals(transferFlag)) {
            showMessage(getString(R.string.detail_on_location));
            return false;
        }
        return true;
    }

    /*子类返回修改模块的名称*/
    protected abstract String getSubFunName();
}
