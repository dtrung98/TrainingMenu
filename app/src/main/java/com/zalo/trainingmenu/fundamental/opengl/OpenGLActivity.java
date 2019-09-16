package com.zalo.trainingmenu.fundamental.opengl;

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

import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.downloader.ui.base.PermissionActivity;
import com.zalo.trainingmenu.fundamental.texture.GLTextureView;

public class OpenGLActivity extends PermissionActivity implements View.OnClickListener {

    public static final String ACTION_PICK_IMAGE = "PICK_IMAGE";
    public static final int REQUEST_CODE_PICK_IMAGE = 1;
    private GLTextureView mGLView;
    private BitmapDrawingRenderer mRenderer;


    private View mButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opengl_layout);

        mGLView = findViewById(R.id.gl_surface_view);
        mButton = findViewById(R.id.button);

        mRenderer = new BitmapDrawingRenderer();

        mGLView.setEGLContextClientVersion(1);
        mGLView.setRenderer(mRenderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        if(mButton!=null) mButton.setOnClickListener(this);
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

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.button) {
            Intent intent = new Intent(ACTION_PICK_IMAGE);
            executeWriteStorageAction(intent);
        }
    }

    @Override
    public void onPermissionResult(Intent intent, boolean granted) {
        super.onPermissionResult(intent, granted);
        if(intent!=null&&granted) {
            String action = intent.getAction();
            if(action!=null) {
                switch (action) {
                    case ACTION_PICK_IMAGE:
                        Intent i = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(i, REQUEST_CODE_PICK_IMAGE);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_PICK_IMAGE:
                if(resultCode== Activity.RESULT_OK && data !=null) {
                    Uri selected = data.getData();

                    String[] fileCols = {MediaStore.Images.Media.DATA};
                    Cursor c = getContentResolver().query(selected,fileCols,null,null,null);
                    c.moveToFirst();
                    int index = c.getColumnIndex(fileCols[0]);
                    String path = c.getString(index);
                    c.close();
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    //mImageView.setImageBitmap(bitmap);
                    mRenderer.setBitmap(bitmap);
                    mGLView.requestRender();
                }

                return;
        }
    }
}
