package com.zalo.trainingmenu.fundamental.index;


import com.ldt.menulayout.model.Item;
import com.ldt.menulayout.ui.AbsMenuActivity;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.fundamental.pipe.PipeExampleActivity;

import java.util.ArrayList;
import java.util.List;

public class MultithreadingActivity extends AbsMenuActivity {
    private static final String TAG = "MultithreadingActivity";


    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();
        list.add(Item.with(this).setTitle(R.string.pipe_example).setDescription(R.string.pipe_example_description).setDestinationActivityClass(PipeExampleActivity.class).get());
        list.add(Item.with(this).setTitle(R.string.another).get());
        return list;
    }

    @Override
    protected int title() {
        return R.string.multithreading;
    }
}
