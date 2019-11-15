package com.ldt.menulayout.ui;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.ldt.menulayout.R;

public abstract class AbsListActivity extends AbsLocaleActivity{
    private static final String TAG = "AbsListActivity";

    RecyclerView mRecyclerView;

    SwipeRefreshLayout mSwipeRefresh;

    TextView mTitle;

    View mBackButton;

    protected void back(View v) {
        finish();
    }

    private void bind() {
        mRecyclerView = findViewById(R.id.recycler_view);
        mSwipeRefresh = findViewById(R.id.swipe_refresh);
        mTitle = findViewById(R.id.title);
        mBackButton = findViewById(R.id.back_button);
        mBackButton.setOnClickListener(this::back);
    }



    @LayoutRes
    protected int contentLayout() {
        return R.layout.list_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(contentLayout());
        bind();
        init();
        refreshData();
    }

    private View getBackButton() {
        return mBackButton;
    }

    protected void setBackButtonVisibility(boolean show) {
        if(mBackButton!=null) {
            if (show) mBackButton.setVisibility(View.VISIBLE);
            else mBackButton.setVisibility(View.GONE);
        }
    }

    private void init() {
        mTitle.setText(getTitle());
        onInitRecyclerView();
        mSwipeRefresh.setOnRefreshListener(this::refreshData);
    }

    protected void onInitRecyclerView() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,2,RecyclerView.VERTICAL,false));
    }

    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeRefresh;
    }

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    protected void refreshData() {
       mSwipeRefresh.setRefreshing(false);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if(mTitle!=null)
        mTitle.setText(title);
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        mTitle.setText(titleId);
    }
}
