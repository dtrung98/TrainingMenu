package com.zalo.trainingmenu.newsfeed3d.photo3d;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ldt.menulayout.ui.permission.PermissionActivity;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.util.PreferenceUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Photo3DActivity extends PermissionActivity {
    private static final String TAG = "Photo3DActivity";

    public static final String ACTION_PICK_ORIGINAL_PHOTO = "pick_original_photo";
    public static final int REQUEST_CODE_PICK_ORIGINAL_PHOTO = 1;
    public static final int REQUEST_CODE_PICK_DEPTH_PHOTO = 2;
    public static final String ACTION_PICK_DEPTH_PHOTO = "pick_depth_photo";

    @BindView(R.id.gl_view)
    Photo3DView mGLView;

    private final boolean RESTORE_MODE = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo3d_layout);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        boolean valid = false;
        if(intent!=null) {
            String action = intent.getAction();
            if(ShaderSetActivity.ACTION_SHADER_SET.equals(action)) {
                String vertex = intent.getStringExtra(ShaderSetActivity.VERTEX_SHADER);
                String fragment = intent.getStringExtra(ShaderSetActivity.FRAGMENT_SHADER);
                if(vertex!=null && !vertex.isEmpty() && fragment!=null && !fragment.isEmpty()) {
                    valid = true;
                    mGLView.createRenderer(vertex,fragment);
                }
            }
        }

        if(!valid)
        mGLView.createRenderer();

        if(RESTORE_MODE) {
            Bitmap b = null;
            try {
                String original = PreferenceUtil.getInstance().getSavedOriginal3DPhoto();
                if (original != null && !original.isEmpty()) b = BitmapFactory.decodeFile(original);
                if (b == null)
                    b = BitmapFactory.decodeFile("/storage/emulated/0/Download/ball.jpg");
                if (b == null)
                    b = BitmapFactory.decodeFile("/storage/emulated/0/download/poro.jpg");
                if (b != null)
                    mGLView.setOriginalPhoto(b);

            } catch (Exception ignored) {
            }

            try {
                b = null;
                String depth = PreferenceUtil.getInstance().getSavedDepthPhoto();
                if (depth != null && !depth.isEmpty()) b = BitmapFactory.decodeFile(depth);
                if (b == null)
                    b = BitmapFactory.decodeFile("/storage/emulated/0/Download/ball_depth.jpg");
                if (b == null)
                    b = BitmapFactory.decodeFile("/storage/emulated/0/download/poro_depth.jpg");
                if (b != null)
                    mGLView.setDepthPhoto(b);
            } catch (Exception ignored) {
            }
        }
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
        mGLView.removeBitmaps();
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
    public void onRequestPermissionsResult(Intent intent, int permissionType, boolean granted) {
        super.onRequestPermissionsResult(intent, permissionType, granted);
        if(intent!=null&&permissionType==Photo3DActivity.PERMISSION_STORAGE&&granted) {
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

                            if(bitmap!=null) PreferenceUtil.getInstance().setSavedOriginal3DPhoto(path);

                            //mImageView.setImageBitmap(bitmap);
                            mGLView.setOriginalPhoto(bitmap);
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
                            if(bitmap!=null) PreferenceUtil.getInstance().setSavedDepthPhoto(path);
                            //mImageView.setImageBitmap(bitmap);
                           mGLView.setDepthPhoto(bitmap);
                            mGLView.requestRender();
                        }
                    }
                }
                break;
        }
    }
}
