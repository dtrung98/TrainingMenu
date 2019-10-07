package com.zalo.trainingmenu.fundamental.index;

import com.ldt.menulayout.model.Item;
import com.ldt.menulayout.ui.AbsMenuActivity;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.fundamental.userlist.UserDataListActivity;
import com.zalo.trainingmenu.fundamental.weather.DemoWeatherApp;

import java.util.ArrayList;
import java.util.List;

public class NetworkMenuActivity extends AbsMenuActivity {
    private static final String TAG = "NetworkMenuActivity";

    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();
        list.add(Item.with(this).setTitle(R.string.weather_app).setDescription(R.string.weather_app_description).setDestinationActivityClass(DemoWeatherApp.class).get());
        list.add(Item.with(this).setTitle(R.string.user_list).setDescription(R.string.user_list_description).setDestinationActivityClass(UserDataListActivity.class).get());
        list.add(Item.with(this).setTitle(R.string.other).get());
        return list;
    }

    @Override
    protected int title() {
        return R.string.network;
    }
}
