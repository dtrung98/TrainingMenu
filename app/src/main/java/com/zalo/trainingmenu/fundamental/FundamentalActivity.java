package com.zalo.trainingmenu.fundamental;


import android.os.Bundle;

import com.ldt.menulayout.model.Item;
import com.ldt.menulayout.ui.AbsMenuActivity;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.fundamental.camera.CameraActivity;
import com.zalo.trainingmenu.fundamental.camera.CameraXActivity;
import com.zalo.trainingmenu.fundamental.index.NetworkMenuActivity;
import com.zalo.trainingmenu.fundamental.index.OpenGLActivity;
import com.zalo.trainingmenu.fundamental.index.ContentProviderDemoActivity;
import com.zalo.trainingmenu.fundamental.index.MultithreadingActivity;
import com.zalo.trainingmenu.newsfeed3d.NewsFeed3DActivity;
import com.zalo.trainingmenu.fundamental.servicedemo.ServiceDemoActivity;

import java.util.ArrayList;
import java.util.List;

public class FundamentalActivity extends AbsMenuActivity {
    private static final String TAG = "FundamentalActivity";

    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();
        list.add(Item.with(this).setTitle(R.string.news_feed_3d).setDescription(R.string.news_feed_3d_description).setDestinationActivityClass(NewsFeed3DActivity.class).get());
        list.add(Item.with(this).setTitle(R.string.opengl).setDescription(R.string.opengl_description).setDestinationActivityClass(OpenGLActivity.class).get());
        list.add(Item.with(this).setTitle(R.string.camera).setDescription(R.string.camera_description).setDestinationActivityClass(CameraActivity.class).get());
        list.add(Item.with(this).setTitle(R.string.cameraX).setDescription(R.string.cameraX_description).setDestinationActivityClass(CameraXActivity.class).get());
        list.add(Item.with(this).setTitle(R.string.cameraKit).setDescription(R.string.cameraKit_description).setDestinationActivityClass(CameraXActivity.class).get());
        list.add(Item.with(this).setTitle(R.string.service).setDescription(R.string.service_description).setDestinationActivityClass(ServiceDemoActivity.class).get());
        list.add(Item.with(this).setTitle(R.string.content_provider).setDescription(R.string.content_provider_description).setDestinationActivityClass(ContentProviderDemoActivity.class).get());
        list.add(Item.with(this).setTitle(R.string.network).setDescription(R.string.network_description).setDestinationActivityClass(NetworkMenuActivity.class).get());
        list.add(Item.with(this).setTitle(R.string.multithreading).setDescription(R.string.multithreading_description).setDestinationActivityClass(MultithreadingActivity.class).get());
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
