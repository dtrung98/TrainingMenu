package com.zalo.servicetraining.ui.fundamental.contentprovider;


import com.zalo.servicetraining.R;
import com.zalo.servicetraining.model.Item;
import com.zalo.servicetraining.ui.base.AbsMenuActivity;

import java.util.ArrayList;
import java.util.List;

public class ContentProviderDemoActivity extends AbsMenuActivity {
    private static final String TAG = "ContentProviderDemoActivity";

    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();
        list.add(new Item().setTitle(R.string.sqlite_note_app).setDescription(getString(R.string.note_app_description)).setDestinationActivityClass(DemoNoteApp.class));
        list.add(new Item().setTitle(R.string.another));
        return list;
    }

}
