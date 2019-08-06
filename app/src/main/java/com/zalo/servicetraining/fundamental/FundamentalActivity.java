package com.zalo.servicetraining.fundamental;


import android.os.Bundle;

import com.zalo.servicetraining.R;
import com.zalo.servicetraining.fundamental.index.ContentProviderDemoActivity;
import com.zalo.servicetraining.fundamental.index.MultithreadingActivity;
import com.zalo.servicetraining.model.Item;
import com.zalo.servicetraining.fundamental.servicedemo.ServiceDemoActivity;
import com.zalo.servicetraining.mainui.base.AbsMenuActivity;
import com.zalo.servicetraining.fundamental.index.NetworkMenuActivity;

import java.util.ArrayList;
import java.util.List;

public class FundamentalActivity extends AbsMenuActivity {
    public static final String TAG = "MainActivity";

    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();

        list.add(new Item().setTitle(R.string.service).setDescription(R.string.service_description).setDestinationActivityClass(ServiceDemoActivity.class));
        list.add(new Item().setTitle(R.string.content_provider).setDescription(R.string.content_provider_description).setDestinationActivityClass(ContentProviderDemoActivity.class));
        list.add(new Item().setTitle(R.string.network).setDescription(R.string.network_description).setDestinationActivityClass(NetworkMenuActivity.class));
        list.add(new Item().setTitle(R.string.multithreading).setDescription(R.string.multithreading_description).setDestinationActivityClass(MultithreadingActivity.class));

        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
    }
}
