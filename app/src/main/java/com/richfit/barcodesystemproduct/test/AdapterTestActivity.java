package com.richfit.barcodesystemproduct.test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.adapter.AdapterTest;
import com.richfit.common_lib.IInterface.IAdapterState;
import com.richfit.common_lib.baseadapterrv.base.ViewHolder;
import com.richfit.common_lib.basetreerv.MultiItemTypeTreeAdapter;
import com.richfit.domain.bean.RefDetailEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by monday on 2017/3/18.
 */

public class AdapterTestActivity extends AppCompatActivity implements IAdapterState{

    MultiItemTypeTreeAdapter<RefDetailEntity> mAdapter;
    List<RefDetailEntity> mDatas;

    RecyclerView mRecyclerview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adatper_test);

        initView();
        initData();
        initAdapter();
    }

    private void initView() {
        mRecyclerview = (RecyclerView) findViewById(R.id.base_detail_recycler_view);
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerview.setLayoutManager(lm);
        mRecyclerview.setHasFixedSize(true);
    }

    private void initData() {
        if (mDatas == null)
            mDatas = new ArrayList<>();
        RefDetailEntity data = null;
        for (int i = 0; i < 10; i++) {
            data = new RefDetailEntity();
            data.lineNum = i + "#";
            data.insLot = "2000" + i;
            data.lineNum105 = "1000" + i;
            data.materialNum = "100016609" + i;
            data.materialDesc = "测试数据";
            data.workCode = "2n01";
            data.invCode = "2001";
            data.refDoc = "100" + i;
            data.refDocItem = i;
            mDatas.add(data);
        }
    }

    private void initAdapter() {
        if (mAdapter == null) {
            mAdapter = new AdapterTest(this, R.layout.item_asy_detail_parent_header, mDatas);
            mRecyclerview.setAdapter(mAdapter);
            mAdapter.setAdapterStateListener(this);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int viewType) {


    }
}
