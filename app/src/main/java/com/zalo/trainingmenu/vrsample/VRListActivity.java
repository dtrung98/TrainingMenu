package com.zalo.trainingmenu.vrsample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ldt.menulayout.ui.AbsListActivity;
import com.ldt.vrview.model.VRPhoto;
import com.zalo.trainingmenu.R;

import java.util.ArrayList;

import butterknife.BindDimen;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.OnClick;

public class VRListActivity extends AbsListActivity {
    VRListAdapter mAdapter;

    @Override
    protected int title() {
        return R.string.new_feeds;
    }


    @Override
    protected void onInitRecyclerView() {

         findViewById(R.id.root).setBackgroundColor(Color.WHITE);
        ((ImageView)findViewById(R.id.back_button)).setColorFilter(0xff0a0a0a);
        findViewById(R.id.back_button).setBackgroundResource(R.drawable.circle_background_support_touch_inverse);
        mAdapter = new VRListAdapter();
        getRecyclerView().setPadding(0,0,0,0);
        getRecyclerView().setLayoutManager(new LinearLayoutManager(this));
        getRecyclerView().setAdapter(mAdapter);
    }
    ArrayList<VRNewsFeed> list;

    @Override
    protected void refreshData() {
        getSwipeRefreshLayout().setRefreshing(true);
        if(list==null) {
            list = new ArrayList<>();
            AsyncTask.execute(() -> {
                list.add(createNewsFeed(
                        "Spherical Images",
                        "Create your own immersive experiences using 360° photos and videos. Sphere optimizes the delivery, for a fast loading time and smooth playback.",
                        R.drawable._360sp));
                list.add(createNewsFeed(
                        "Impressive Panoramic",
                        "360 photos are special because they are spherical images, and it is not so easy to map the surface of a sphere onto a flat plane . All standard image file formats assume a flat, planar image.",
                        R.drawable._360x));
                list.add(createNewsFeed(
                        "Pano History",
                        "There are several practical sphere-to-plane mappings. Two are widely used: Equirectangular is a latitude/longitude map, 360 degrees wide x 180 degrees high. The 2:1 aspect ratio is often used as a clue by 360-aware software. ",
                        R.drawable.down1, new float[]{0,0,320,180}));
                list.add(createNewsFeed(
                        "VR Enjoy",
                        "360 panoramas can be saved in regular bitmap file types (bmp, jpg, png, …etc) that are distorted in some areas of the image (according the projection method used).",
                        R.drawable.down2, new float[]{0,30,360,120}));
           /*     list.add(createNewsFeed(
                        "Reda Maged",
                        "This is an example of a 360 panorama done using spherical projection method. You can see the stretching resulted in the lower and upper parts and the compression in the middle.",
                        R.drawable.rural));*/
                list.add(createNewsFeed("Countryside",
                                "I like living in the countryside because of some reasons. Environmentally speaking, it is a peaceful place. The air is fresh. The space is quiet. We can enjoy healthy natural conditions without worrying much about environmental pollution.\n" +
                                "\n" +
                                "As for social security, the countryside is a safer place than a city. While urban security situation is always complicated with all kinds of crimes, rural areas are much more secure because most of countrymen are friendly and ready to help one another.",
                        R.drawable._360x));
                list.add(createNewsFeed(
                        "Photographic",
                        "Beyond the practical limitations of getting on site with expensive 360 video equipment, we have had consistent user feedback that people expect VR resolution to match the HD screen based content they are used to.",
                        R.drawable.down1));
                list.add(createNewsFeed("Impressive Panoramic",
                        "This is an example of a 360 panorama done using spherical projection method.",
                        R.drawable.down2));
                list.add(createNewsFeed(
                        "Spherical Images",
                        "This is an example of a 360 panorama done using spherical projection method.",
                        R.drawable._360sp));
                list.add(createNewsFeed(
                        "Spherical Images",
                        "This is an example of a 360 panorama done using spherical projection method.",
                        R.drawable._360x));
                list.add(createNewsFeed("",
                        "Spherical Images",
                        R.drawable.down1));
                list.add(createNewsFeed(
                        "Spherical Images",
                        "This is an example of a 360 panorama done using spherical projection method.",
                        R.drawable.down2));
                getRecyclerView().post(() -> {
                    mAdapter.setData(list);
                    getSwipeRefreshLayout().setRefreshing(false);
                });
            });
        } else {
            mAdapter.setData(list);
            getSwipeRefreshLayout().setRefreshing(false);
        }
    }

    private VRNewsFeed createNewsFeed(String author,String content, int resId) {
        return createNewsFeed(author,content,resId, VRPhoto.getDefaultAngleAreas());
    }

    private VRNewsFeed createNewsFeed(String author,String content, int resId, float[] area) {
        VRNewsFeed newsFeed = new VRNewsFeed(author, content);
        newsFeed.mDrawableID = resId;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeResource(getResources(),resId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        newsFeed.mVRPhoto = VRPhoto.with(this).setBitmap(bitmap).setAreaAngles(area).get();
        return newsFeed;
    }
}
