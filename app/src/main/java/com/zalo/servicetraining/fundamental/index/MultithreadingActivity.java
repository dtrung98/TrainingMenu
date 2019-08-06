package com.zalo.servicetraining.fundamental.index;


import com.zalo.servicetraining.fundamental.pipe.PipeExampleActivity;
import com.zalo.servicetraining.model.Item;
import com.zalo.servicetraining.mainui.base.AbsMenuActivity;

import java.util.ArrayList;
import java.util.List;

public class MultithreadingActivity extends AbsMenuActivity {
    private static final String TAG = "MultithreadingActivity";


    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();
        list.add(new Item().setTitle("Pipe Example").setDescription("Simple program that using pipes.").setDestinationActivityClass(PipeExampleActivity.class));
        list.add(new Item().setTitle("Another"));
        return list;
    }

}
