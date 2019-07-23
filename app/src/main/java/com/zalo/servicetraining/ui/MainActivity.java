package com.zalo.servicetraining.ui;


import android.os.Bundle;

import com.zalo.servicetraining.R;
import com.zalo.servicetraining.model.Item;
import com.zalo.servicetraining.ui.base.AbsMenuActivity;
import com.zalo.servicetraining.downloader.ui.DownloaderActivity;
import com.zalo.servicetraining.fundamental.FundamentalActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AbsMenuActivity {
    public static final String TAG = "MainActivity";

    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();
        list.add(new Item().setTitle(R.string.downloader).setDescription(R.string.downloads_description).setDestinationActivityClass(DownloaderActivity.class).setDrawable(R.drawable.download_icon).setDrawablePadding(6));
        list.add(new Item().setTitle(R.string.basic).setDrawable(R.drawable.redo).setDestinationActivityClass(FundamentalActivity.class));
        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
    }
}
