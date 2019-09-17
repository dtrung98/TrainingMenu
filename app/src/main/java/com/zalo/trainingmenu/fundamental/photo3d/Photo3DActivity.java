package com.zalo.trainingmenu.fundamental.photo3d;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;

import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.downloader.ui.base.PermissionActivity;
import com.zalo.trainingmenu.fundamental.texture.GLTextureView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Photo3DActivity extends PermissionActivity {
    private static final String TAG = "Photo3DActivity";

    public static final String ACTION_PICK_ORIGINAL_PHOTO = "pick_original_photo";
    public static final int REQUEST_CODE_PICK_ORIGINAL_PHOTO = 1;
    public static final int REQUEST_CODE_PICK_DEPTH_PHOTO = 2;
    public static final String ACTION_PICK_DEPTH_PHOTO = "pick_depth_photo";
    private Photo3DRenderer mRenderer;

    @BindView(R.id.gl_view)
    GLTextureView mGLView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo3d_layout);
        ButterKnife.bind(this);
        mRenderer = new Photo3DRenderer();
        mRenderer.setBitmap(BitmapFactory.decodeFile("/storage/emulated/0/Download/ball.jpg"));
        mRenderer.setDepthMap(BitmapFactory.decodeFile("/storage/emulated/0/Download/ball_depth.jpg"));

        mGLView.setEGLContextClientVersion(2);
        mGLView.setRenderer(mRenderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

    }

    @OnClick(R.id.back_button)
    void back() {
        finish();
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

    @OnClick(R.id.remove_button)
    void removeClicked() {
        mRenderer.removeBitmaps();
    }

    @OnClick(R.id.original_button)
    void chooseOriginalPhoto() {
        Intent intent = new Intent(ACTION_PICK_ORIGINAL_PHOTO);
        executeWriteStorageAction(intent);
    }

    @OnClick(R.id.depth_button)
    void chooseDepthPhoto() {
        Intent intent = new Intent(ACTION_PICK_DEPTH_PHOTO);
        executeWriteStorageAction(intent);
    }

    @Override
    public void onPermissionResult(Intent intent, boolean granted) {
        super.onPermissionResult(intent, granted);
        if(intent!=null&&granted) {
            String action = intent.getAction();
            if(action!=null) {
                Intent i;
                switch (action) {
                    case ACTION_PICK_ORIGINAL_PHOTO:
                        i = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(i, REQUEST_CODE_PICK_ORIGINAL_PHOTO);
                        break;
                    case ACTION_PICK_DEPTH_PHOTO:
                        i = new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(i, REQUEST_CODE_PICK_DEPTH_PHOTO);
                        break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_PICK_ORIGINAL_PHOTO:
                if(resultCode== Activity.RESULT_OK && data !=null) {
                    Uri selected = data.getData();
                    if(selected!=null) {
                        String[] fileCols = {MediaStore.Images.Media.DATA};
                        Cursor c = getContentResolver().query(selected,fileCols,null,null,null);
                        if(c!=null) {
                            c.moveToFirst();
                            int index = c.getColumnIndex(fileCols[0]);
                            String path = c.getString(index);
                            Log.d(TAG, "onActivityResult: "+path);
                            c.close();
                            Bitmap bitmap = BitmapFactory.decodeFile(path);
                            //mImageView.setImageBitmap(bitmap);
                            mRenderer.setBitmap(bitmap);
                            mGLView.requestRender();
                        }
                    }
                }
                break;
            case REQUEST_CODE_PICK_DEPTH_PHOTO:
                if(resultCode== Activity.RESULT_OK && data !=null) {
                    Uri selected = data.getData();
                    if(selected!=null) {
                        String[] fileCols = {MediaStore.Images.Media.DATA};
                        Cursor c = getContentResolver().query(selected, fileCols, null, null, null);
                        if (c != null) {
                            c.moveToFirst();
                            int index = c.getColumnIndex(fileCols[0]);
                            String path = c.getString(index);
                            Log.d(TAG, "onActivityResult: "+path);
                            c.close();
                            Bitmap bitmap = BitmapFactory.decodeFile(path);
                            //mImageView.setImageBitmap(bitmap);
                            mRenderer.setDepthMap(bitmap);
                            mGLView.requestRender();
                        }
                    }
                }
                break;
        }
    }
}
