package com.zalo.trainingmenu.vrsample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.ldt.menulayout.ui.AbsLocaleActivity;
import com.ldt.vrview.VRView;
import com.ldt.vrview.model.VRPhoto;
import com.zalo.trainingmenu.R;

public class VrSampleActivity extends AbsLocaleActivity implements View.OnClickListener {
    private static final String TAG = "VrContextActivity";

    private VRView mGLView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vr_layout);
        mGLView = findViewById(R.id.vr_view);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR );

        mGLView.setOnClickListener(this);
        AsyncTask.execute(() -> {
            mGLView.mSampleVRPhotos.add(createPhoto(R.drawable._360sp));
            mGLView.mSampleVRPhotos.add(createPhoto(R.drawable._360x));
            mGLView.mSampleVRPhotos.add(createPhoto(R.drawable.down1));
            mGLView.mSampleVRPhotos.add(createPhoto(R.drawable.down2));
            mGLView.post(() -> {
               mGLView.setVRPhoto(mGLView.mSampleVRPhotos.get(0));
            });
        });
    }

    private void createPhoto() {
        int resId = R.drawable._360sp;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeResource(getResources(),resId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mVRPhoto = VRPhoto.with(this).setBitmap(bitmap).get();
    }

    private VRPhoto createPhoto(int resId) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeResource(getResources(),resId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return VRPhoto.with(this).setBitmap(bitmap).get();
    }

    VRPhoto mVRPhoto;

    @Override
    protected int title() {
        return R.string.vr_sample;
    }

    @Override
    protected void onResume() {
       super.onResume();
       mGLView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    @Override
    public void onClick(View v) {
        mGLView.recalibrate();
    }
}
