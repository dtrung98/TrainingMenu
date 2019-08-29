package com.zalo.trainingmenu.mainui.base;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.zalo.trainingmenu.R;

import java.util.zip.GZIPOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public abstract class AbsListActivity extends AbsLocaleActivity {
    private static final String TAG = "AbsListActivity";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;

    @BindView(R.id.title)
    TextView mTitle;

    @BindView(R.id.back_button)
    View mBackButton;

    @OnClick(R.id.back_button)
    void back() {
        finish();
    }

    @LayoutRes
    protected int contentLayout() {
        return R.layout.list_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(contentLayout());
        ButterKnife.bind(this);
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
