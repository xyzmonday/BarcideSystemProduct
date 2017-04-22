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
import android.widget.ImageView;

import com.richfit.common_lib.R;
import com.richfit.common_lib.adapter.ErrorListAdapter;

import java.util.Arrays;


/**
 * 使用DialogFragment生成错误列表对话框
 * Created by monday on 2016/10/19.
 */

public class ShowErrorMessageDialog extends DialogFragment {

    public static final String MESSAGES_KEY = "messages";
    RecyclerView mRvmessages;
    ImageView mIvError;


    public static ShowErrorMessageDialog newInstance(String[] messages) {
        Bundle bundle = new Bundle();
        bundle.putStringArray(MESSAGES_KEY, messages);
        ShowErrorMessageDialog fragment = new ShowErrorMessageDialog();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.framgent_dialog_show_info, container, false);
        mRvmessages = (RecyclerView) view.findViewById(R.id.rv_info_list);
        RecyclerView.LayoutManager lm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRvmessages.setLayoutManager(lm);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //注意这里我们需要显示一个title所以不能设置该属性
//        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //启用窗体的扩展特性。
        getDialog().requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onActivityCreated(savedInstanceState);
        //注意这里如果不设置背景Drawable那么使用系统默认有padding的InsetDrawable
//        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        //设置自定义的title  layout
        getDialog().getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.fragment_dialiog_error_title);
        //初始化适配器
        String[] messages = getArguments().getStringArray(MESSAGES_KEY);

        ErrorListAdapter adapter = new ErrorListAdapter(Arrays.asList(messages));
        mRvmessages.setAdapter(adapter);
    }
}
