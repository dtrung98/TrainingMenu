package com.zalo.servicetraining.fundamental.index;

import com.zalo.servicetraining.R;
import com.zalo.servicetraining.fundamental.userlist.UserDataListActivity;
import com.zalo.servicetraining.fundamental.weather.DemoWeatherApp;
import com.zalo.servicetraining.model.Item;
import com.zalo.servicetraining.mainui.base.MenuAdapter;
import com.zalo.servicetraining.mainui.base.AbsMenuActivity;

import java.util.ArrayList;
import java.util.List;

public class NetworkMenuActivity extends AbsMenuActivity implements MenuAdapter.OnItemClickListener {
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
