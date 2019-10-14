package com.zalo.trainingmenu.newsfeed3d;

import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.ldt.menulayout.ui.AbsListActivity;
import com.ldt.parallaximageview.model.ParallaxImageObject;
import com.zalo.trainingmenu.R;

import java.util.ArrayList;

public class NewsFeed3DActivity extends AbsListActivity {
    private NewsFeedAdapter mAdapter;

    @Override
    protected int title() {
        return R.string.photo_3d_list;
    }

    @Override
    protected void onInitRecyclerView() {
       // findViewById(R.id.root).setBackgroundColor(Color.WHITE);
        ((ImageView)findViewById(R.id.back_button)).setColorFilter(0xff0a0a0a);
        findViewById(R.id.back_button).setBackgroundResource(R.drawable.circle_background_support_touch_inverse);
        mAdapter = new NewsFeedAdapter();
        getRecyclerView().setPadding(0,0,0,0);
        getRecyclerView().setLayoutManager(new LinearLayoutManager(this));
        getRecyclerView().setAdapter(mAdapter);
    }

    @Override
    protected void refreshData() {
        ArrayList<Object> list = new ArrayList<>();
        list.add(new ParallaxImageObject("/storage/emulated/0/Download/girl.jpg","/storage/emulated/0/Download/girl_depth.jpg"));
        list.add(new ParallaxImageObject("/storage/emulated/0/Download/poro.jpg","/storage/emulated/0/Download/poro_depth.jpg"));
        list.add(new ParallaxImageObject("/storage/emulated/0/Download/source.jpg","/storage/emulated/0/Download/source_depth.jpeg"));
        list.add(new ParallaxImageObject("/storage/emulated/0/Download/img/ball.jpg","/storage/emulated/0/Download/img/ball-map.jpg"));
        list.add(new ParallaxImageObject("/storage/emulated/0/Download/img/canyon.jpg","/storage/emulated/0/Download/img/canyon-map.jpg"));
        list.add(new ParallaxImageObject("/storage/emulated/0/Download/img/lady.jpg","/storage/emulated/0/Download/img/lady-map.jpg"));
        list.add(new ParallaxImageObject("/storage/emulated/0/Download/img/mount.jpg","/storage/emulated/0/Download/img/mount-map.jpg"));
        list.add(new ParallaxImageObject("/storage/emulated/0/Download/dog1.jpg","/storage/emulated/0/Download/dog3.jpg"));
        list.add(new ParallaxImageObject("/storage/emulated/0/Download/img/ball.jpg","/storage/emulated/0/Download/img/ball-map.jpg"));

        mAdapter.setData(list);
        getSwipeRefreshLayout().setRefreshing(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
