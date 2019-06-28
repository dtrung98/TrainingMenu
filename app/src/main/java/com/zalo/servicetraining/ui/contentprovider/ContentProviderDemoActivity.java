package com.zalo.servicetraining.ui.contentprovider;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.zalo.servicetraining.R;
import com.zalo.servicetraining.model.Item;
import com.zalo.servicetraining.ui.MenuAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class ContentProviderDemoActivity extends AppCompatActivity implements MenuAdapter.OnItemClickListener {
    private static final String TAG = "ContentProviderDemoActivity";

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

    MenuAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);
        init();

        refreshData();
    }
    private void init() {
        mBackButton.setVisibility(View.VISIBLE);
        mTitle.setText(R.string.content_provider);
        mAdapter = new MenuAdapter();
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,2,RecyclerView.VERTICAL,false));
        mSwipeRefresh.setOnRefreshListener(this::refreshData);

    }

    ArrayList<Item> mList = new ArrayList<>();

    private void refreshData() {
       mList.clear();
       mList.add(new Item().setTitle("SQLite Note"));
       mList.add(new Item().setTitle("Other"));
       mAdapter.setData(mList);
       mSwipeRefresh.setRefreshing(false);
    }

    @Override
    public void onEventItemClick(Item item) {
        switch (item.getTitle()) {
            case "SQLite Note" :
                startActivity(new Intent(this, DemoNoteApp.class)); break;
            case "Other" :
                Toasty.info(this,"Coming soon!").show(); break;
        }
    }

}
