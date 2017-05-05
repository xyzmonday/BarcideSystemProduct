package com.richfit.common_lib.basetreerv;


import android.content.Context;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.richfit.common_lib.IInterface.IAdapterState;
import com.richfit.common_lib.IInterface.OnItemMove;
import com.richfit.common_lib.R;
import com.richfit.common_lib.baseadapterrv.base.ItemViewDelegate;
import com.richfit.common_lib.baseadapterrv.base.ItemViewDelegateManager;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.utils.Global;
import com.richfit.domain.bean.TreeNode;

import java.util.List;

public abstract class MultiItemTypeTreeAdapter<T extends TreeNode> extends RecyclerView.Adapter<ViewHolder> {
    protected Context mContext;

    protected List<T> mVisibleNodes;
    protected List<T> mAllNodes;


    protected ItemViewDelegateManager mItemViewDelegateManager;
    protected OnItemClickListener mOnItemClickListener;
    protected OnItemMove<T> mOnItemMove;
    protected IAdapterState mAdapterState;


    public MultiItemTypeTreeAdapter(Context context, List<T> allNodes) {
        mContext = context;
        mAllNodes = allNodes;
        mVisibleNodes = RecycleTreeViewHelper.filterVisibleNodes(allNodes);
        mItemViewDelegateManager = new ItemViewDelegateManager();
    }

    @Override
    public int getItemViewType(int position) {
        if (!useItemViewDelegateManager()) return super.getItemViewType(position);
        return mItemViewDelegateManager.getItemViewType(mVisibleNodes.get(position), position);
    }

    /**
     * 创建ViewHolder
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemViewDelegate itemViewDelegate = mItemViewDelegateManager.getItemViewDelegate(viewType);
        int layoutId = itemViewDelegate.getItemViewLayoutId();
        ViewHolder holder = ViewHolder.createViewHolder(mContext, parent, layoutId);
        setListener(parent, holder, viewType);
        return holder;
    }


    public void convert(ViewHolder holder, T item) {
        mItemViewDelegateManager.convert(holder, item, holder.getAdapterPosition());
    }

    protected boolean isEnabled(int viewType) {
        return true;
    }

    /**
     * 设置item的点击和长按监听
     *
     * @param parent
     * @param viewHolder
     * @param viewType
     */
    protected void setListener(final ViewGroup parent, final ViewHolder viewHolder, int viewType) {
        if (!isEnabled(viewType)) return;
        viewHolder.getConvertView().setOnClickListener(v -> {
            if (mOnItemClickListener != null) {
                int position = viewHolder.getAdapterPosition();
                mOnItemClickListener.onItemClick(v, viewHolder, position);
            }
        });

        viewHolder.getConvertView().setOnLongClickListener(v -> {
            if (mOnItemClickListener != null) {
                int position = viewHolder.getAdapterPosition();
                return mOnItemClickListener.onItemLongClick(v, viewHolder, position);
            }
            return false;
        });
        setItemNodeEditAndDeleteListener(viewHolder);
    }

    /**
     * 绑定该ViewHolder的数据，由ItemDelegate去实现
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        onViewHolderBindInternal(holder, getItemViewType(position));
        convert(holder, mVisibleNodes.get(position));
    }

    protected void onViewHolderBindInternal(ViewHolder holder, int viewType) {
        //不能修改子节点的数据明细，只能修改父节点和子节点的抬头
        if (mAdapterState != null) {
            mAdapterState.onBindViewHolder(holder, viewType);
        }
    }

    @Override
    public int getItemCount() {
        int itemCount = mVisibleNodes.size();
        return itemCount;
    }

    public void removeAllVisibleNodes() {
        if (mAllNodes != null && mVisibleNodes != null) {
            mAllNodes.clear();
            mVisibleNodes.clear();
            notifyDataSetChanged();
        }
    }


    public void addAll(List<T> data) {
        if (data == null || data.size() == 0)
            return;
        mAllNodes = data;
        mVisibleNodes = RecycleTreeViewHelper.filterVisibleNodes(data);
        notifyDataSetChanged();
    }

    public T getItem(int position) {
        if (mVisibleNodes != null && position >= 0 && position < mVisibleNodes.size()) {
            return mVisibleNodes.get(position);
        }
        return null;
    }

    public List<T> getDatas() {
        return mVisibleNodes;
    }

    public MultiItemTypeTreeAdapter addItemViewDelegate(ItemViewDelegate<T> itemViewDelegate) {
        mItemViewDelegateManager.addDelegate(itemViewDelegate);
        return this;
    }

    public MultiItemTypeTreeAdapter addItemViewDelegate(int viewType, ItemViewDelegate<T> itemViewDelegate) {
        mItemViewDelegateManager.addDelegate(viewType, itemViewDelegate);
        return this;
    }

    protected boolean useItemViewDelegateManager() {
        return mItemViewDelegateManager.getItemViewDelegateCount() > 0;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, RecyclerView.ViewHolder holder, int position);

        boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemEditAndDeleteListener(OnItemMove<T> listener) {
        mOnItemMove = listener;
    }

    public void setAdapterStateListener(IAdapterState listener) {
        mAdapterState = listener;
    }

    /**
     * 点击搜索或者展开
     *
     * @param position
     */
    protected void expandOrCollapse(int position) {
        if (position < 0 || position > mVisibleNodes.size() - 1)
            return;
        T n = mVisibleNodes.get(position);
        if (n != null) {
            if (n.isLeaf())
                return;
            n.setExpand(!n.isExpand());
            mVisibleNodes = RecycleTreeViewHelper.filterVisibleNodes(mAllNodes);
            notifyDataSetChanged();
        }
    }


    /**
     * 设置子节点的margin
     */
    private void setChildNodeMargin(View itemView) {
        RecyclerView.LayoutParams itemLayoutParams = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        itemLayoutParams.setMargins((int) mContext.getResources().getDimension(R.dimen.child_left_padding), 0, 0, 0);
    }

    /**
     * 可编辑节点的删除和修改监听
     *
     * @param holder
     */
    private void setItemNodeEditAndDeleteListener(ViewHolder holder) {
        //设置点击和删除监听
        holder.setOnClickListener(R.id.item_edit, view -> {
            if (mOnItemMove != null) {
                int position = holder.getAdapterPosition();
                mOnItemMove.editNode(mVisibleNodes.get(position), position);
            }
        });
        holder.setOnClickListener(R.id.item_delete, view -> {
            int position = holder.getAdapterPosition();
            showDialogForNodeDelete(mVisibleNodes.get(position), position);
        });
    }

    /**
     * 提示用户是否删除该子节点
     *
     * @param node：将要删除的子节点
     * @param position：将要删除的子节点在显示列表的位置
     */
    protected void showDialogForNodeDelete(final T node, final int position) {
        Builder builder = new Builder(mContext);
        builder.setTitle("警告");
        builder.setIcon(R.mipmap.icon_warning);
        builder.setMessage("您真的要删除该条数据?点击确定删除.");
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("确定", (dialog, which) -> {
            if (node != null) {
                if (mOnItemMove != null) {
                    mOnItemMove.deleteNode(node, position);
                }
            }
        });
        builder.show();
    }

    /**
     * 对于无参考的模块，直接删除该节点
     *
     * @param position:该节点在明细界面的位置
     */
    public void removeItemByPosition(int position) {
        if (mVisibleNodes != null && position >= 0 && position < mVisibleNodes.size()) {
            mAllNodes.remove(position);
            mVisibleNodes.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * 对于父子节点结构的明细，当户删除子节点后，如果需要修改父节点的某些字段信息。
     * 如果删除的子节点是最后一个子节点，那么需要自动删除子节点的头节点
     *
     * @param position:需要删除的节点位置。注意这里对于父子节点的明细只有子节点能够删除。 对于无参考的业务(包括有参考但是非父子节点的明细界面)，
     *                                                   节点删除有两类：第一类是直接删除该节点(对于这种情况直接调用removeItem方法即可)；
     *                                                   第二类是修改该节点的某些字段(比如说验收)。
     */
    public void removeNodeByPosition(final int position) {

        final TreeNode node = mVisibleNodes.get(position);
        final TreeNode parentNode = node.getParent();
        if (parentNode != null) {
            int parentPos = mVisibleNodes.indexOf(parentNode);
            //修改父节点的相关数据(注意这里需要先刷新父节点的某些数据，因为如果先删除子节点，那么子节点的数据再也不能拿到)
            notifyParentNodeChanged(position, parentPos);
            //移除子节点的抬头节点
            if (parentNode.getChildren().size() == 2 && parentNode.getChildren().get(0).getViewType() == Global.CHILD_NODE_HEADER_TYPE) {

                TreeNode childNode = parentNode.getChildren().get(0);
                int indexOf = mVisibleNodes.indexOf(childNode);

                mAllNodes.remove(childNode);
                mVisibleNodes.remove(childNode);
                notifyItemRemoved(indexOf);
            }
            //移除需要删除的节点
            mAllNodes.remove(node);
            mVisibleNodes.remove(node);
            //刷新子节点删除
            notifyItemRemoved(position);
        }
        //修改本节点
        notifyNodeChanged(position);
    }

    /**
     * 子类需要根据具体的业务修改父节点的字段数据。比如常见的就是
     * 修改累计数量。
     *
     * @param childNodePosition:子节点在明细列表的位置
     * @param parentNodePosition:父节点在明细列表的位置
     */
    public abstract void notifyParentNodeChanged(int childNodePosition, int parentNodePosition);

    /**
     * 对于无参考或者验收等没有父子节点结构明细界面，直接删除该节点
     *
     * @param position
     */
    public abstract void notifyNodeChanged(int position);

}
