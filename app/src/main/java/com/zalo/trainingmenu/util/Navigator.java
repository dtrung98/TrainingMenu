package com.zalo.trainingmenu.util;

import android.content.Context;
import android.util.SparseArray;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.zalo.trainingmenu.MenuActivity;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.downloader.ui.main.DownloadActivity;
import com.zalo.trainingmenu.extension.ExtensionActivity;
import com.zalo.trainingmenu.extension.IndexListActivity;
import com.zalo.trainingmenu.fundamental.FundamentalActivity;
import com.zalo.trainingmenu.fundamental.index.ContentProviderDemoActivity;
import com.zalo.trainingmenu.fundamental.index.NetworkMenuActivity;
import com.zalo.trainingmenu.fundamental.index.OpenGLActivity;
import com.zalo.trainingmenu.fundamental.noteapp.DemoNoteActivity;
import com.zalo.trainingmenu.fundamental.opengl.airhockey.AirHockeyActivity;
import com.zalo.trainingmenu.fundamental.servicedemo.ServiceDemoActivity;
import com.zalo.trainingmenu.fundamental.userlist.UserDataListActivity;
import com.zalo.trainingmenu.fundamental.weather.DemoWeatherApp;
import com.zalo.trainingmenu.vrsample.VRListActivity;
import com.zalo.trainingmenu.vrsample.VRMenuActivity;
import com.zalo.trainingmenu.vrsample.VrSampleActivity;

import java.util.ArrayList;
import java.util.Collection;

public class Navigator {
    private static Navigator sInstance;
    public static Navigator getInstance() {
        if(sInstance==null) sInstance = new Navigator();
        return sInstance;
    }
    private Navigator() {
        init();
    }

    private final SparseArray<Class<? extends AppCompatActivity>> mActivityList = new SparseArray<>();
    public Class<? extends AppCompatActivity> findActivityById(@StringRes int id)  {
        return mActivityList.get(id);
    }

    private void add(Class<? extends AppCompatActivity> cls, int id) {
        mActivityList.put(id,cls);
    }

    public Class<? extends AppCompatActivity> getDefaultActivityCls() {
        return findActivityById(R.string.menu);
    }

    public void setLaunchingActivityCls(Context context, int activityId) {
        PreferenceUtil.getInstance().saveLaunchingActivityId(activityId);
        PreferenceUtil.getInstance().saveLaunchingActivityTitle(context.getResources().getString(activityId));
    }

    public Class<? extends AppCompatActivity> getLaunchingActivityCls(Context context) {
        Class<? extends AppCompatActivity> cls = findActivityById(PreferenceUtil.getInstance().getSavedLaunchingActivityId());
        if(cls==null) {
            String title = PreferenceUtil.getInstance().getSavedLaunchingActivityTitle();
            if(!title.isEmpty()) {
                int id = findActivityIdByTitle(context, title);
                if(id!=-1) cls = findActivityById(id);
            }
        }

        if(cls != null) return cls;
        return getDefaultActivityCls();
    }

    public int findActivityIdByTitle(Context context, String title) {
        ArrayList<Integer> list = getAllActivityIds();
        for (int item :
                list) {
            if(context.getResources().getString(item).equals(title)) return item;
        }
        return -1;
    }

    public ArrayList<Integer> getAllActivityIds() {
        ArrayList<Integer> l =  new ArrayList<>(mActivityList.size());
        int count = mActivityList.size();
        for (int i = 0; i < count; i++) {
            l.add(mActivityList.keyAt(i));
        }

        return l;
    }

    private void init() {
        add(MenuActivity.class, R.string.menu);
        add(DownloadActivity.class,R.string.downloader);
        add(FundamentalActivity.class,R.string.fundamental);
        add(VRMenuActivity.class,R.string.vr);
        add(OpenGLActivity.class,R.string.opengl);
        add(VrSampleActivity.class,R.string.vr_sample);
        add(VRListActivity.class,R.string.new_feeds);

        add(ServiceDemoActivity.class,R.string.service);
        add(ContentProviderDemoActivity.class,R.string.content_provider);
        add(DemoNoteActivity.class,R.string.notes);
        add(NetworkMenuActivity.class,R.string.network);
        add(DemoWeatherApp.class,R.string.weather);
        add(UserDataListActivity.class,R.string.users);
        add(AirHockeyActivity.class,R.string.air_hockey);
        add(ExtensionActivity.class,R.string.extension);
        add(IndexListActivity.class,R.string.index_list);
    }
}
