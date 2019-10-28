package com.zalo.trainingmenu.vrsample;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ldt.menulayout.ui.AbsLocaleActivity;
import com.zalo.trainingmenu.R;

import java.util.ArrayList;

public class VRListActivity extends AbsLocaleActivity {
    RecyclerView mRecyclerView;
    VRListAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vr_list);

        mRecyclerView = findViewById(R.id.recycler_view);
        mAdapter = new VRListAdapter();

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        ArrayList<Object> list = new ArrayList<>();
        list.add(5);
        list.add(5);
        list.add(5);
        list.add(5);
        list.add(5);
        list.add(5);
        list.add(5);
        list.add(5);
        list.add(5);
        list.add(5);
        list.add(5);
        list.add(5);
        list.add(5);
        mAdapter.setData(list);
    }

    @Override
    protected int title() {
        return R.string.vr_list_sample;
    }
}
