package com.zalo.trainingmenu.vrsample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ldt.menulayout.model.Item;
import com.ldt.menulayout.ui.AbsMenuActivity;
import com.zalo.trainingmenu.MenuActivity;
import com.zalo.trainingmenu.R;

import java.util.ArrayList;
import java.util.List;

public class VRMenuActivity extends AbsMenuActivity {
    private static final String TAG = "VRMenuActivity";

    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();
        list.add(Item.with(this).setTitle(R.string.vr_sample).setDescription(R.string.vr_sample_description).setDestinationActivityClass(VrSampleActivity.class).setAction(VrSampleActivity.ACTION_VIEW_SAMPLE).get());
        list.add(Item.with(this).setTitle(R.string.choose_in_gallery).setDescription(R.string.choose_vr_photo_description).setDestinationActivityClass(VrSampleActivity.class).setAction(VrSampleActivity.ACTION_VIEW_FROM_GALLERY).get());
        list.add(Item.with(this).setTitle(R.string.vr_list_sample).setDescription(R.string.vr_list_description).setDestinationActivityClass(VRListActivity.class).get());
        return list;
    }

    @Override
    protected void back(View v) {
        startActivity(new Intent(this, MenuActivity.class));
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int title() {
        return R.string.vr;
    }
}
