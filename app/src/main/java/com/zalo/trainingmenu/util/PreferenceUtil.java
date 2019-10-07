package com.zalo.trainingmenu.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.zalo.trainingmenu.App;

public final class PreferenceUtil {
    public static final String SAVED_VERTEX_SHADER = "saved_vertex_shader";
    public static final String SAVED_FRAGMENT_SHADER = "saved_fragment_shader";
    public static final String SAVED_ORIGINAL_3D_PHOTO = "saved_original_3d_photo";
    public static final String SAVED_DEPTH_PHOTO = "saved_depth_photo";
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

    public String  getSavedDepthPhoto() {
        return mPreferences.getString(SAVED_DEPTH_PHOTO,null);
    }

    public void setSavedDepthPhoto(String path) {
        mPreferences.edit().putString(SAVED_DEPTH_PHOTO,path).apply();
    }
}
