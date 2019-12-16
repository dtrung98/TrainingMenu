package com.zalo.trainingmenu.fundamental.opengl.airhockey;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.Nullable;

import com.ldt.menulayout.ui.permission.PermissionActivity;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.fundamental.opengl.texture.GLTextureView;

public class AirHockeyActivity extends PermissionActivity {

    private GLTextureView mGLView;
    private GLTextureView.Renderer mRenderer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.air_hockey_layout);

        mGLView = findViewById(R.id.gl_surface_view);

        mRenderer = new AirHockeyRenderer(this);

        mGLView.setEGLContextClientVersion(2);
        mGLView.setRenderer(mRenderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }
}
