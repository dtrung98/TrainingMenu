package com.zalo.servicetraining.mainui;


import android.os.Bundle;

import com.zalo.servicetraining.R;
import com.zalo.servicetraining.downloader.ui.main.DownloadActivity;
import com.zalo.servicetraining.model.Item;
import com.zalo.servicetraining.mainui.base.AbsMenuActivity;
import com.zalo.servicetraining.fundamental.FundamentalActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AbsMenuActivity {
    public static final String TAG = "MainActivity";

    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();
        list.add(Item.with(this).setTitle(R.string.downloader).setDescription(R.string.downloads_description).setDestinationActivityClass(DownloadActivity.class).setDrawableRes(R.drawable.download_icon).setDrawablePadding(6).get());
        list.add(Item.with(this).setTitle(R.string.basic).setDrawableRes(R.drawable.redo).setDestinationActivityClass(FundamentalActivity.class).get());
        return list;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int title() {
        return R.string.menu;
    }
}
