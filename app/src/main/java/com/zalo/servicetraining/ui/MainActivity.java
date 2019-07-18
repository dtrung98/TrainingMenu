package com.zalo.servicetraining.ui;


import android.os.Bundle;

import com.zalo.servicetraining.R;
import com.zalo.servicetraining.model.Item;
import com.zalo.servicetraining.ui.base.AbsMenuActivity;
import com.zalo.servicetraining.ui.contentprovider.ContentProviderDemoActivity;
import com.zalo.servicetraining.ui.multithreading.MultithreadingActivity;
import com.zalo.servicetraining.ui.multithreading.PipeExampleActivity;
import com.zalo.servicetraining.ui.network.NetworkMenuActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AbsMenuActivity {
    public static final String TAG = "MainActivity";

    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();
        list.add(new Item().setTitle("Service").setDescription("Create a simple foreground service").setDestinationActivityClass(ServiceDemoActivity.class));
        list.add(new Item().setTitle("Content Provider").setDescription("A note app using SQLite to store data").setDestinationActivityClass(ContentProviderDemoActivity.class));
        list.add(new Item().setTitle("Network").setDescription("Network handler and JSON Parsing").setDestinationActivityClass(NetworkMenuActivity.class));
        list.add(new Item().setTitle("Multithreading").setDescription("Doing something in background").setDestinationActivityClass(MultithreadingActivity.class));

        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
    }
}
