package com.zalo.servicetraining.mainui.base;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.zalo.servicetraining.model.Item;

import java.util.List;

import es.dmoral.toasty.Toasty;

public abstract class AbsMenuActivity extends AbsListActivity implements MenuAdapter.OnItemClickListener {
    private static final String TAG = "AbsMenuActivity";

    protected MenuAdapter mAdapter;

    @Override
    protected void onInitRecyclerView() {
        super.onInitRecyclerView();
        mAdapter = new MenuAdapter();
        getRecyclerView().setAdapter(mAdapter);
        mAdapter.setListener(this);

    }

    @Override
    protected void refreshData() {
       mAdapter.setData(onRefreshDataList());
       super.refreshData();
    }

    protected abstract List<Item> onRefreshDataList();

    @Override
    public final void onEventItemClick(Item item) {
        Class<? extends AppCompatActivity> cls = item.getDestinationActivityClass();
        if(cls!=null) {
            startActivity(new Intent(this, cls));
        } else if(item.getTitle()!=null&&!item.getTitle().isEmpty()) Toasty.normal(this,item.getTitle() +" is coming soon!").show();
        else Toasty.normal(this,"Coming soon!").show();
    }

}
