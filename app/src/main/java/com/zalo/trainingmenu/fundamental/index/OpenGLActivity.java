package com.zalo.trainingmenu.fundamental.index;

import com.ldt.menulayout.model.Item;
import com.ldt.menulayout.ui.AbsMenuActivity;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.fundamental.opengl.airhockey.AirHockeyActivity;
import com.zalo.trainingmenu.fundamental.opengl.SimpleBitmapActivity;
import com.zalo.trainingmenu.newsfeed3d.photo3d.Photo3DActivity;

import java.util.ArrayList;
import java.util.List;

public class OpenGLActivity extends AbsMenuActivity {
    private static final String TAG = "OpemGLActivity";

    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();
        list.add(Item.with(this).setTitle(R.string.simple_bitmap).setDescription(R.string.simple_bitmap_description).setDestinationActivityClass(SimpleBitmapActivity.class).get());
        list.add(Item.with(this).setTitle(R.string.photo3D).setDescription(R.string.photo3D_description).setDestinationActivityClass(Photo3DActivity.class).get());
        list.add(Item.with(this).setTitle(R.string.air_hockey).setDescription(R.string.air_hockey_description).setDestinationActivityClass(AirHockeyActivity.class).get());

        return list;
    }

    @Override
    protected int title() {
        return R.string.opengl;
    }
}
