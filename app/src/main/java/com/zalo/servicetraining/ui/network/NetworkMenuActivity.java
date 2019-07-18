package com.zalo.servicetraining.ui.network;

import com.zalo.servicetraining.model.Item;
import com.zalo.servicetraining.ui.MenuAdapter;
import com.zalo.servicetraining.ui.base.AbsMenuActivity;
import com.zalo.servicetraining.ui.userdata.UserDataListActivity;

import java.util.ArrayList;
import java.util.List;

public class NetworkMenuActivity extends AbsMenuActivity implements MenuAdapter.OnItemClickListener {
    private static final String TAG = "NetworkMenuActivity";

    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();
        list.add(new Item().setTitle("Weather App").setDescription("A weather app fetching data from internet").setDestinationActivityClass(DemoWeatherApp.class));
        list.add(new Item().setTitle("User List").setDescription("A list that shows numerous user, show a profile on click item").setDestinationActivityClass(UserDataListActivity.class));
        list.add(new Item().setTitle("Other"));
        return list;
    }

}
