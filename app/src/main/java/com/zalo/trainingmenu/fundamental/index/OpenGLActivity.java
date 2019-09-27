package com.zalo.trainingmenu.fundamental.index;

import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.fundamental.camera.CameraActivity;
import com.zalo.trainingmenu.fundamental.opengl.SimpleBitmapActivity;
import com.zalo.trainingmenu.fundamental.opengl.photo3d.Photo3DActivity;
import com.zalo.trainingmenu.fundamental.opengl.photo3d.ShaderSetActivity;
import com.zalo.trainingmenu.fundamental.userlist.UserDataListActivity;
import com.zalo.trainingmenu.fundamental.weather.DemoWeatherApp;
import com.zalo.trainingmenu.mainui.base.AbsMenuActivity;
import com.zalo.trainingmenu.mainui.base.MenuAdapter;
import com.zalo.trainingmenu.model.Item;

import java.util.ArrayList;
import java.util.List;

public class OpenGLActivity extends AbsMenuActivity implements MenuAdapter.OnItemClickListener {
    private static final String TAG = "OpemGLActivity";

    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();
        list.add(Item.with(this).setTitle(R.string.simple_bitmap).setDescription(R.string.simple_bitmap_description).setDestinationActivityClass(SimpleBitmapActivity.class).get());
        list.add(Item.with(this).setTitle(R.string.photo3D).setDescription(R.string.photo3D_description).setDestinationActivityClass(Photo3DActivity.class).get());
        list.add(Item.with(this).setTitle(R.string.photo3D_dynamic).setDescription(R.string.dynamic_shader_description).setDestinationActivityClass(ShaderSetActivity.class).get());

        return list;
    }

    @Override
    protected int title() {
        return R.string.opengl;
    }
}
