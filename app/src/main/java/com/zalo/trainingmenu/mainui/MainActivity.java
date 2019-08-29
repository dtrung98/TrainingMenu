package com.zalo.trainingmenu.mainui;


import android.os.Bundle;

import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.downloader.ui.main.DownloadActivity;
import com.zalo.trainingmenu.model.Item;
import com.zalo.trainingmenu.mainui.base.AbsMenuActivity;
import com.zalo.trainingmenu.fundamental.FundamentalActivity;

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
    protected void onInitRecyclerView() {
        setBackButtonVisibility(false);
        super.onInitRecyclerView();
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
