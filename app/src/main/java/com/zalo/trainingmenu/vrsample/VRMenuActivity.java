package com.zalo.trainingmenu.vrsample;

import android.os.Bundle;

import com.ldt.menulayout.model.Item;
import com.ldt.menulayout.ui.AbsMenuActivity;
import com.zalo.trainingmenu.R;

import java.util.ArrayList;
import java.util.List;

public class VRMenuActivity extends AbsMenuActivity {
    private static final String TAG = "VRMenuActivity";

    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();
        list.add(Item.with(this).setTitle(R.string.vr_sample).setDescription(R.string.vr_sample_description).setDestinationActivityClass(VrSampleActivity.class).get());
        list.add(Item.with(this).setTitle(R.string.vr_list_sample).setDescription(R.string.vr_list_description).setDestinationActivityClass(VRListActivity.class).get());
        return list;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int title() {
        return R.string.fundamental;
    }
}
