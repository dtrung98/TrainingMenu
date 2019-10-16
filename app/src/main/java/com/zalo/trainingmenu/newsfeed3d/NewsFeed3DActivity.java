package com.zalo.trainingmenu.newsfeed3d;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.ldt.menulayout.ui.AbsListActivity;
import com.ldt.parallaximageview.model.ParallaxImageObject;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.newsfeed3d.model.NewsFeedObject;

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
        ArrayList<NewsFeedObject> list = new ArrayList<>();
        list.add(new NewsFeedObject().setContentText("Office").setImageObject(new ParallaxImageObject(BitmapFactory.decodeFile("/storage/emulated/0/Download/office.jpg"),BitmapFactory.decodeFile("/storage/emulated/0/Download/office_depth.jpg"))));
        list.add(new NewsFeedObject().setContentText("Girl").setImageObject(new ParallaxImageObject(BitmapFactory.decodeFile("/storage/emulated/0/Download/girl.jpg"),BitmapFactory.decodeFile("/storage/emulated/0/Download/girl_depth.jpg"))));
        list.add(new NewsFeedObject().setContentText("Yamasa").setImageObject(new ParallaxImageObject(BitmapFactory.decodeFile("/storage/emulated/0/Download/yamasa.jpg"),BitmapFactory.decodeFile("/storage/emulated/0/Download/yamasa_depth.png"))));
        list.add(new NewsFeedObject().setContentText("Mountain").setImageObject(new ParallaxImageObject(BitmapFactory.decodeFile("/storage/emulated/0/Download/source.jpg"),BitmapFactory.decodeFile("/storage/emulated/0/Download/source_depth.jpeg"))));
        list.add(new NewsFeedObject().setContentText("A bowl").setImageObject(new ParallaxImageObject(BitmapFactory.decodeFile("/storage/emulated/0/Download/bow.jpg"),BitmapFactory.decodeFile("/storage/emulated/0/Download/bow_depth.jpg"))));
        list.add(new NewsFeedObject().setContentText("A ball").setImageObject(new ParallaxImageObject(BitmapFactory.decodeFile("/storage/emulated/0/Download/img/ball.jpg"),BitmapFactory.decodeFile("/storage/emulated/0/Download/img/ball_depth.jpg"))));
        list.add(new NewsFeedObject().setContentText("Canyon").setImageObject(new ParallaxImageObject(BitmapFactory.decodeFile("/storage/emulated/0/Download/img/canyon.jpg"),BitmapFactory.decodeFile("/storage/emulated/0/Download/img/canyon_depth.jpg"))));
        list.add(new NewsFeedObject().setContentText("Lady").setImageObject(new ParallaxImageObject(BitmapFactory.decodeFile("/storage/emulated/0/Download/img/lady.jpg"),BitmapFactory.decodeFile("/storage/emulated/0/Download/img/lady_depth.jpg"))));
        list.add(new NewsFeedObject().setContentText("Another mountain").setImageObject(new ParallaxImageObject(BitmapFactory.decodeFile("/storage/emulated/0/Download/img/mount.jpg"), BitmapFactory.decodeFile("/storage/emulated/0/Download/img/mount-map.jpg"))));
        list.add(new NewsFeedObject().setContentText("Dog").setImageObject(new ParallaxImageObject(BitmapFactory.decodeFile("/storage/emulated/0/Download/dog1.jpg"), BitmapFactory.decodeFile("/storage/emulated/0/Download/dog3.jpg"))));
        list.add(new NewsFeedObject().setContentText("A coffee").setImageObject(new ParallaxImageObject(BitmapFactory.decodeFile("/storage/emulated/0/Download/download.jpeg"), BitmapFactory.decodeFile("/storage/emulated/0/Download/download_depth.png"))));

        mAdapter.setData(list);
        getSwipeRefreshLayout().setRefreshing(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
