package com.zalo.trainingmenu.fundamental.index;


import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.fundamental.noteapp.DemoNoteActivity;
import com.zalo.trainingmenu.model.Item;
import com.zalo.trainingmenu.mainui.base.AbsMenuActivity;

import java.util.ArrayList;
import java.util.List;

public class ContentProviderDemoActivity extends AbsMenuActivity {
    private static final String TAG = "ContentProviderDemoActivity";

    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();
        list.add(Item.with(this).setTitle(R.string.sqlite_note_app).setDescription(getString(R.string.note_app_description)).setDestinationActivityClass(DemoNoteActivity.class).get());
        list.add(Item.with(this).setTitle(R.string.another).get());
        return list;
    }

    @Override
    protected int title() {
        return R.string.content_provider;
    }
}
