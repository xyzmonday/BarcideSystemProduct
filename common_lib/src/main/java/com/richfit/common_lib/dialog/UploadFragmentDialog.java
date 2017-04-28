package com.richfit.common_lib.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.richfit.common_lib.R;
import com.richfit.common_lib.adapter.TimeLineAdapter;
import com.richfit.common_lib.adapter.animation.DividerVerticalTimeLine;
import com.richfit.common_lib.utils.AppCompat;
import com.richfit.common_lib.utils.L;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.UploadMsgEntity;

import java.util.ArrayList;


/**
 * Created by monday on 2017/4/20.
 */

public class UploadFragmentDialog extends DialogFragment implements TimeLineAdapter.ItemClickListener {
    private static final String UPLOAD_INFO_KEY = "upload_info";
    private ArrayList<UploadMsgEntity> mDatas;
    private TimeLineAdapter mAdapter;

    public static UploadFragmentDialog newInstance(UploadMsgEntity data) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(UPLOAD_INFO_KEY, data);
        UploadFragmentDialog fragment = new UploadFragmentDialog();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatas = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.framgent_dialog_show_info, container, false);
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.rv_info_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.addItemDecoration(new DividerVerticalTimeLine(AppCompat.getColor(R.color.blue_a200, getContext()), 60,
                UiUtil.dpToPx(3), AppCompat.getDrawable(getContext(), R.mipmap.icon_timeline_mark)));
        mAdapter = new TimeLineAdapter(getContext(), mDatas);
        mAdapter.setOnItemClickListener(this);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //启用窗体的扩展特性。
        getDialog().requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        getDialog().getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.fragment_dialog_upload_title);

        //设置数据
        Bundle arguments = getArguments();
        UploadMsgEntity data = arguments.getParcelable(UPLOAD_INFO_KEY);
        mDatas.clear();
        mDatas.add(data);
        mAdapter.notifyDataSetChanged();
    }

    public void addMessage(UploadMsgEntity info) {
        if (mAdapter != null) {
            mDatas.add(info);
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onItemLongClick(int position, UploadMsgEntity info) {
        if (info == null || !info.isEror)
            return;
        if (mOnEditLocalDataListener != null) {
            mOnEditLocalDataListener.onItemClick(info);
        }
    }

    @Override
    public void onDestroyView() {
        L.e("UploadFragmentDialog onDestroyView");
        if (mAdapter != null) {
            mAdapter.setOnItemClickListener(null);
        }
        mOnEditLocalDataListener = null;
        super.onDestroyView();
    }

    private OnEditLocalDataListener mOnEditLocalDataListener;

    public void setOnEditLocalDataListener(OnEditLocalDataListener listener) {
        this.mOnEditLocalDataListener = listener;
    }

    /**
     * 用户修改离线采集的数据
     */
    public interface OnEditLocalDataListener {
        void onItemClick(UploadMsgEntity info);
    }
}
