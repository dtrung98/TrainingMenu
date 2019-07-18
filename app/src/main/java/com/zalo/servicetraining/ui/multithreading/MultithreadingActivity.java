package com.zalo.servicetraining.ui.multithreading;


import com.zalo.servicetraining.model.Item;
import com.zalo.servicetraining.ui.base.AbsMenuActivity;
import com.zalo.servicetraining.ui.contentprovider.DemoNoteApp;

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
