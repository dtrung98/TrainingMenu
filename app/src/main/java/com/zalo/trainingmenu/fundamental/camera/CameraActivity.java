package com.zalo.trainingmenu.fundamental.camera;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.ldt.menulayout.ui.permission.PermissionActivity;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.util.PreferenceUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CameraActivity extends PermissionActivity {
    private static final String TAG = "Photo3DActivity";

    public static final String ACTION_ACTIVE_CAMERA = "active_camera";
    public static final int REQUEST_CODE_ACTIVE_CAMERA = 1;
    public static final int REQUEST_CODE_PICK_PHOTO = 2;
    public static final String ACTION_PICK_PHOTO = "pick_photo";

    @BindView(R.id.cameraTextureView)
    CameraTextureView mCameraTextureView;

    private final boolean RESTORE_MODE = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
        ButterKnife.bind(this);

    }

    @Override
    protected void onPause() {
        mCameraTextureView.closeCamera();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
/*        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);*/
        orderToActiveCamera();
    }

    void orderToActiveCamera() {
        Intent intent = new Intent(ACTION_ACTIVE_CAMERA);
        executePermissionAction(intent,PermissionActivity.PERMISSION_CAMERA);
    }

    void activeCamera() {
        mCameraTextureView.activeCamera();
    }

    @Override
    public void onRequestPermissionsResult(Intent intent, int permissionType, boolean granted) {
        super.onRequestPermissionsResult(intent, permissionType, granted);
        if(intent!=null&&granted) {
            String action = intent.getAction();
            if(action!=null) {
                Intent i;
                switch (action) {
                    case ACTION_ACTIVE_CAMERA:
                        activeCamera();
                        break;
                    case ACTION_PICK_PHOTO:
                        i = new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(i, REQUEST_CODE_PICK_PHOTO);
                        break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_PICK_PHOTO:
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
                        }
                    }
                }
                break;
        }
    }
}
