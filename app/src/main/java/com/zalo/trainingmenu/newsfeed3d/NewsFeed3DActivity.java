package com.zalo.trainingmenu.newsfeed3d;

import android.annotation.SuppressLint;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.ldt.menulayout.ui.AbsListActivity;
import com.zalo.trainingmenu.R;

import java.util.ArrayList;

public class NewsFeed3DActivity extends AbsListActivity {
    private NewsFeedAdapter mAdapter;

    @Override
    protected int title() {
        return R.string.photo_3d_list;
    }

    @Override
    protected void onInitRecyclerView() {
        mAdapter = new NewsFeedAdapter();
        getRecyclerView().setLayoutManager(new LinearLayoutManager(this));
        getRecyclerView().setAdapter(mAdapter);
    }



    @Override
    protected void refreshData() {
        ArrayList<Object> list = new ArrayList<>();
        list.add("ABC");
        list.add("ABC");
        list.add("ABC");
        list.add("ABC");
        list.add("ABC");
        list.add("ABC");
        list.add("ABC");
        list.add("ABC");
        list.add("ABC");
        list.add("ABC");

        mAdapter.setData(list);
        getSwipeRefreshLayout().setRefreshing(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
