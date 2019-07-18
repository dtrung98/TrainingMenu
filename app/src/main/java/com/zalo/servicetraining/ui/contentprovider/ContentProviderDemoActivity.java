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
import com.zalo.servicetraining.ui.base.AbsMenuActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class ContentProviderDemoActivity extends AbsMenuActivity {
    private static final String TAG = "ContentProviderDemoActivity";

    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();
        list.add(new Item().setTitle("SQLite Note App").setDescription("A simple note app using SQLite to store data").setDestinationActivityClass(DemoNoteApp.class));
        list.add(new Item().setTitle("Another"));
        return list;
    }

}
