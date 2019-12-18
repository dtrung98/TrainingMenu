package com.zalo.trainingmenu.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.zalo.trainingmenu.App;
import com.zalo.trainingmenu.R;

public final class PreferenceUtil {
    public static final String SAVED_VERTEX_SHADER = "saved_vertex_shader";
    public static final String SAVED_FRAGMENT_SHADER = "saved_fragment_shader";
    public static final String SAVED_ORIGINAL_3D_PHOTO = "saved_original_3d_photo";
    public static final String SAVED_DEPTH_PHOTO = "saved_depth_photo";
    public static final String SAVED_VR_SOURCE = "saved_vr_source";
    public static final String SAVED_LAUNCHING_ACTIVITY_ID = "saved_launching_activity_id";
    public static final String SAVED_LAUNCHING_ACTIVITY_TITLE = "saved_launching_activity_title";
    private static PreferenceUtil sInstance;
    private final SharedPreferences mPreferences;

    private PreferenceUtil(@NonNull final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferenceUtil getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceUtil(App.getInstance().getApplicationContext());
        }
        return sInstance;
    }

    public static PreferenceUtil getInstance() {
        if (sInstance == null) {
            sInstance = new PreferenceUtil(App.getInstance().getApplicationContext());
        }
        return sInstance;
    }

    public String getSavedVertexShader() {
        return mPreferences.getString(SAVED_VERTEX_SHADER,null);
    }

    public void setSavedVertexShader(String v) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(SAVED_VERTEX_SHADER, v);
        editor.apply();
    }

    public String getSavedFragmentShader() {
        return mPreferences.getString(SAVED_FRAGMENT_SHADER,null);
    }

    public void setSavedFragmentShader(String f) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(SAVED_FRAGMENT_SHADER, f);
        editor.apply();
    }

    public String  getSavedOriginal3DPhoto() {
        return mPreferences.getString(SAVED_ORIGINAL_3D_PHOTO,null);
    }

    public void setSavedOriginal3DPhoto(String path) {
        mPreferences.edit().putString(SAVED_ORIGINAL_3D_PHOTO,path).apply();
    }

    public String getSavedDepthPhoto() {
        return mPreferences.getString(SAVED_DEPTH_PHOTO,null);
    }

    public void setSavedDepthPhoto(String path) {
        mPreferences.edit().putString(SAVED_DEPTH_PHOTO,path).apply();
    }

    public String getSavedVRSource() {
        return mPreferences.getString(SAVED_VR_SOURCE,null);
    }
    public void saveVRSource(String source) {
        mPreferences.edit().putString(SAVED_VR_SOURCE,source).apply();
    }

    public int getSavedLaunchingActivityId() {
        return mPreferences.getInt(SAVED_LAUNCHING_ACTIVITY_ID,R.string.menu);
    }

    public String getSavedLaunchingActivityTitle() {
        return mPreferences.getString(SAVED_LAUNCHING_ACTIVITY_TITLE,"");
    }

    public void saveLaunchingActivityId(int id) {
        mPreferences.edit().putInt(SAVED_LAUNCHING_ACTIVITY_ID,id).apply();
    }

    public void saveLaunchingActivityTitle(String title) {
        mPreferences.edit().putString(SAVED_LAUNCHING_ACTIVITY_TITLE, title).apply();
    }
}
