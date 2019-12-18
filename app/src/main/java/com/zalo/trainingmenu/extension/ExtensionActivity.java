package com.zalo.trainingmenu.extension;

import com.ldt.menulayout.model.Item;
import com.ldt.menulayout.ui.AbsMenuActivity;
import com.zalo.trainingmenu.R;

import java.util.ArrayList;
import java.util.List;

public class ExtensionActivity extends AbsMenuActivity {
    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();
        list.add(Item.with(this).setTitle(R.string.index_list).setDestinationActivityClass(IndexListActivity.class).setDescription(R.string.index_list_description).get());
        list.add(Item.with(this).setTitle(R.string.set_launcher_actitivy).setDestinationActivityClass(SetLauncherActivity.class).setDescription(R.string.set_launcher_description).get());
        return list;
    }

    @Override
    protected int title() {
        return R.string.extension;
    }
}
