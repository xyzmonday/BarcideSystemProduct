package com.richfit.barcodesystemproduct.base;

import android.content.Context;
import android.support.annotation.ArrayRes;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.richfit.common_lib.IInterface.IPresenter;
import com.richfit.common_lib.basetreerv.RecycleTreeViewHelper;
import com.richfit.common_lib.rxutils.RxBus;
import com.richfit.common_lib.rxutils.SimpleRxBus;
import com.richfit.common_lib.utils.Global;
import com.richfit.data.repository.Repository;
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.SimpleEntity;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * presenter的基类，实现类IPresenter的方法.
 * 注意在BasePresenter的构造方法中使用了@Inject注解，
 * 所以凡是通过继承BasePresenter实现的IPresenter实例的构造
 * 方法必须传入相关的实例。
 * 在Activity和Fragment中，由于ActivityComponent提供了activity和rxManager，
 * 所以只要将activityComponent或者fragmentComponent注入到相应
 * 的activity和fragment中。
 * Created by monday on 2016/9/30.
 */

public class BasePresenter<T extends BaseView> implements IPresenter<T> {

    /*presenter持有View层的实例引用。为了防止内存泄露采用弱应用的形式保存*/
    private Reference<T> mViewRef;
    protected Context mContext;
    private CompositeDisposable mCompositeDisposable;
    @Inject
    protected Repository mRepository;
    @Inject
    protected SimpleRxBus mSimpleRxBus;
    @Inject
    protected RxBus mRxBus;

    @Inject
    public BasePresenter(Context context) {
        this.mContext = context;
    }

    @Override
    public void attachView(T view) {
        this.mViewRef = new WeakReference<>(view);
        this.mCompositeDisposable = new CompositeDisposable();
        onStart();
    }

    /**
     * Presenter层创建后，在onStart可注册一些监听
     */
    protected void onStart() {

    }

    @Override
    public boolean isLocal() {
        return mRepository.isLocal();
    }

    @Override
    public void setLocal(boolean isLocal) {
        mRepository.setLocal(isLocal);
    }

    //解除view与presenter的绑定
    @Override
    public void detachView() {
        if (mViewRef != null) {
            mViewRef.clear();
            mViewRef = null;
        }
        unSubscribe();
    }

    //获取View
    public T getView() {
        if (mViewRef != null && mViewRef.get() != null) {
            return mViewRef.get();
        }
        return null;
    }

    //判断View是否还与该Presenter绑定
    public boolean isAttached() {
        return mViewRef != null && mViewRef.get() != null;
    }

    /*添加订阅*/
    public void addSubscriber(Disposable disposable) {
        if (disposable != null && mCompositeDisposable != null) {
            mCompositeDisposable.add(disposable);
        }
    }

    /*取消订阅*/
    public void unSubscribe() {
        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.dispose();
        }
        if(mSimpleRxBus.hasSubscribers()) {
            mSimpleRxBus.unregisterAll();
        }
    }

    /*获取String资源*/
    protected String getString(@StringRes int res) {
        return mContext.getString(res);
    }

    public List<String> getStringArray(@ArrayRes int id) {
        return Arrays.asList(mContext.getResources().getStringArray(id));
    }

    /**
     * 针对父子节点结构展示的数据，为父节点增加树状结构的信息
     *
     * @param details
     */
    protected void addTreeInfo(final List<RefDetailEntity> details) {
        if (details == null || details.size() == 0)
            return;
        //配置节点信息
        int treeId = 1;
        int pTreeId = 0;//注意id从1开始，pid从0开始是为了保证一级父节点没有父节点
        for (RefDetailEntity detail : details) {
            detail.setViewType(Global.PARENT_NODE_HEADER_TYPE);
            detail.setTreeId(treeId++);
            detail.setpTreeId(pTreeId);
            detail.setHasChild(false);
        }
    }

    /**
     * 为父节点增加节点的信息,以便方便树形显示
     */
    protected ReferenceEntity addTreeInfo(final ReferenceEntity refData) {
        addTreeInfo(refData.billDetailList);
        return refData;
    }

    /**
     * 将缓存的二级节点的数据赋值到原始单据上（这里赋值的字段需要根据采集界面确定）
     * @param parentNode
     * @param cachedNode
     */
    protected void copyCache2Refdetail(RefDetailEntity parentNode,RefDetailEntity cachedNode) {
        //先将缓存的数据相关字段保存到父节点中
        parentNode.invId = cachedNode.invId;
        parentNode.invCode = cachedNode.invCode;
        parentNode.invName = cachedNode.invName;
        parentNode.recInvId = cachedNode.recInvId;
        parentNode.recInvCode = cachedNode.recInvCode;
        parentNode.recInvName = cachedNode.recInvName;
        parentNode.totalQuantity = cachedNode.totalQuantity;
        //105必检
        parentNode.returnQuantity = cachedNode.returnQuantity;
        parentNode.projectText = cachedNode.returnQuantity;
        parentNode.moveCauseDesc = cachedNode.moveCauseDesc;
        parentNode.decisionCode = cachedNode.decisionCode;
        parentNode.moveCause = cachedNode.moveCause;
    }

    protected void copy(RefDetailEntity targetNode,RefDetailEntity sourceNode) {
        if(targetNode == null || sourceNode == null)
            return;
        targetNode.refLineId = sourceNode.refLineId;
        targetNode.lineNum = sourceNode.lineNum;
        targetNode.lineNum105 = sourceNode.lineNum105;
        targetNode.materialId = sourceNode.materialId;
        targetNode.materialNum = sourceNode.materialNum;
        targetNode.materialDesc = sourceNode.materialDesc;
        targetNode.materialGroup = sourceNode.materialGroup;
        targetNode.materialUnit = sourceNode.materialUnit;
        targetNode.unit = sourceNode.unit;
        targetNode.materialUnit = sourceNode.materialUnit;
        targetNode.recordUnit = sourceNode.recordUnit;
        targetNode.unitRate = sourceNode.unitRate;
        targetNode.actQuantity = sourceNode.actQuantity;
        targetNode.arrivalQuantity = sourceNode.arrivalQuantity;
        targetNode.batchFlag = sourceNode.batchFlag;
        targetNode.inspectionDate = sourceNode.inspectionDate;
        targetNode.inspectionResult = sourceNode.inspectionResult;
        targetNode.inspectionStandard = sourceNode.inspectionStandard;
        targetNode.specialInvFlag = sourceNode.specialInvFlag;
        targetNode.specialInvNum = sourceNode.specialInvNum;
        targetNode.orderQuantity = sourceNode.orderQuantity;
        targetNode.workId = sourceNode.workId;
        targetNode.workName = sourceNode.workName;
        targetNode.workCode = sourceNode.workCode;
        targetNode.invId = sourceNode.invId;
        targetNode.invName = sourceNode.invName;
        targetNode.invCode = sourceNode.invCode;
        targetNode.qmFlag = sourceNode.qmFlag;
        targetNode.recWorkId = sourceNode.recWorkId;
        targetNode.recWorkName = sourceNode.recWorkName;
        targetNode.recWorkCode = sourceNode.recWorkName;
        targetNode.recInvId = sourceNode.recInvId;
        targetNode.recInvName = sourceNode.recInvName;
        targetNode.recInvCode = sourceNode.recInvCode;
        targetNode.totalQuantity = sourceNode.totalQuantity;
        targetNode.insLot = sourceNode.insLot;
        targetNode.refDoc = sourceNode.refDoc;
        targetNode.refDocItem = sourceNode.refDocItem;
        targetNode.insLotQuantity = sourceNode.insLotQuantity;
        targetNode.returnQuantity = sourceNode.returnQuantity;
    }

    /**
     * 为子节点增加树形结构的信息（主要是增加子节点的父节点id）
     */
    protected void addTreeInfo(RefDetailEntity parentNode, RefDetailEntity childNode,
                               ArrayList<RefDetailEntity> collectedList) {
        if (parentNode == null || childNode == null)
            return;
        //处理子节点
        //向确认该父节点是否已经有子节点，如果没有，那么添加一个子节点的头布局
        if (!parentNode.isHasChild()) {
            RefDetailEntity childHeader = new RefDetailEntity();
            childHeader.setViewType(Global.CHILD_NODE_HEADER_TYPE);
            childHeader.setTreeId(--Global.CHILD_NODE_MAX_ID);
            childHeader.setpTreeId(parentNode.getTreeId());
            collectedList.add(childHeader);
            parentNode.setHasChild(true);
        }
        //给子节点赋予节点信息
        childNode.setViewType(Global.CHILD_NODE_ITEM_TYPE);
        childNode.setTreeId(--Global.CHILD_NODE_MAX_ID);
        childNode.setpTreeId(parentNode.getTreeId());
        collectedList.add(childNode);
    }

    /**
     * 获取父节点的位置
     *
     * @param refData:明细数据列表
     * @param refLineId:需要查询的父节点的id
     */
    protected int getParentNodePosition(ReferenceEntity refData, String refLineId) {
        int pos = -1;
        if (TextUtils.isEmpty(refLineId)) {
            return pos;
        }
        List<RefDetailEntity> list = refData.billDetailList;
        if (list != null && list.size() > 0) {
            for (RefDetailEntity entity : list) {
                ++pos;
                if (refLineId.equals(entity.refLineId))
                    return pos;
            }
        }
        return pos;
    }

    /**
     * 通过refLineId将获取原始单据中的明细行
     *
     * @param refLineId:单据行Id
     * @param refData:原始单据信息
     * @return
     */
    protected Flowable<RefDetailEntity> getMatchedLineData(String refLineId, ReferenceEntity refData) {
        if (TextUtils.isEmpty(refLineId))
            return Flowable.error(new Throwable("该行的行Id不存在,请检查该单据是否正确"));
        List<RefDetailEntity> list = refData.billDetailList;
        for (RefDetailEntity entity : list) {
            if (refLineId.equals(entity.refLineId))
                return Flowable.just(entity);
        }
        return Flowable.error(new Throwable("未能找到与该行匹配的缓存"));
    }

    /**
     * 通过refLineId将缓存和原始单据行关联起来
     */
    protected RefDetailEntity getLineDataByRefLineId(RefDetailEntity refLineData, ReferenceEntity cachedRefData) {
        if (refLineData == null) {
            return null;
        }
        final String refLineId = refLineData.refLineId;
        if (TextUtils.isEmpty(refLineId))
            return null;
        //通过refLineId匹配出缓存中的明细行
        List<RefDetailEntity> detail = cachedRefData.billDetailList;
        for (RefDetailEntity entity : detail) {
            if (refLineId.equals(entity.refLineId)) {
                return entity;
            }
        }
        return null;
    }


    /**
     * 展示之前的节点排序
     *
     * @param nodes
     * @return
     */
    protected Flowable<ArrayList<RefDetailEntity>> sortNodes(final ArrayList<RefDetailEntity> nodes) {
        return Flowable.create(emitter -> {
            try {
                ArrayList<RefDetailEntity> allNodes = RecycleTreeViewHelper.getSortedNodes(nodes, 1);
                emitter.onNext(allNodes);
                emitter.onComplete();
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(new Throwable(e.getMessage()));
            }
        }, BackpressureStrategy.BUFFER);
    }


    protected ArrayList<String> wrapper2Str(ArrayList<SimpleEntity> list) {
        ArrayList<String> strs = new ArrayList<>();
        StringBuffer sb = new StringBuffer();
        for (SimpleEntity entity : list) {
            sb.setLength(0);
            sb.append(entity.code).append("_").append(entity.name);
            strs.add(sb.toString());
        }
        return strs;
    }

    /**
     * 为单据行增加批次信息。在登陆的时候，已经拿到了batch_flag标识，该标识表示
     * #Y:全部物料启用批次管理
     * #N:全部物料不启用批次管理
     * #T:根据SAP的MARC表判断物料是否启用批次管理
     *
     * @param refData
     * @return
     */
    protected ReferenceEntity addBatchManagerStatus(ReferenceEntity refData) {
        if ("Y".equalsIgnoreCase(Global.BATCHMANAGERSTATUS)) {
            addBatchManagerStatus(refData.billDetailList, true);
        } else if ("N".equalsIgnoreCase(Global.BATCHMANAGERSTATUS)) {
            addBatchManagerStatus(refData.billDetailList, false);
        } else if ("T".equalsIgnoreCase(Global.BATCHMANAGERSTATUS)) {
            List<RefDetailEntity> list = refData.billDetailList;
            for (RefDetailEntity data : list) {
                String batchManagerStatus = mRepository.getBatchManagerStatus(data.workId, data.materialId);
                //如果是X那么表示打开了批次管理
                data.batchManagerStatus = "X".equalsIgnoreCase(batchManagerStatus);
            }
        }
        return refData;
    }

    private void addBatchManagerStatus(List<RefDetailEntity> list, boolean isOpenBatchManager) {
        for (RefDetailEntity item : list) {
            item.batchManagerStatus = isOpenBatchManager;
        }
    }
}
