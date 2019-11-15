package com.zalo.trainingmenu;


import android.os.Bundle;

import com.ldt.menulayout.model.Item;
import com.ldt.menulayout.ui.AbsMenuActivity;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.downloader.ui.main.DownloadActivity;
import com.zalo.trainingmenu.fundamental.FundamentalActivity;
import com.zalo.trainingmenu.vrsample.VRMenuActivity;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AbsMenuActivity {
    public static final String TAG = "MenuActivity";

    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();
        list.add(Item.with(this).setTitle(R.string.downloader).setDescription(R.string.downloads_description).setDestinationActivityClass(DownloadActivity.class).setDrawableRes(R.drawable.download_icon).setDrawablePadding(6).get());
        list.add(Item.with(this).setTitle(R.string.basic).setDrawableRes(R.drawable.redo).setDestinationActivityClass(FundamentalActivity.class).get());
        list.add(Item.with(this).setTitle(R.string.vr).setDescription(R.string.vr_description).setDestinationActivityClass(VRMenuActivity.class).setDrawableRes(R.drawable.rotate_360).setTintColor(0xFF72CAAF).setDrawablePadding(6).get());
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
