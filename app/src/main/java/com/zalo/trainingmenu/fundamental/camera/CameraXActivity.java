package com.zalo.trainingmenu.fundamental.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.view.TextureView;

import androidx.annotation.Nullable;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ldt.menulayout.ui.permission.PermissionActivity;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.fundamental.camera.util.AutoFitPreviewBuilder;
import com.zalo.trainingmenu.fundamental.camera.util.LuminosityAnalyzer;
import com.zalo.trainingmenu.util.PreferenceUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CameraXActivity extends PermissionActivity {
    private static final String TAG = "CameraXActivity";

    public static final String ACTION_ACTIVE_CAMERA = "active_camera";
    public static final int REQUEST_CODE_ACTIVE_CAMERA = 1;
    public static final int REQUEST_CODE_PICK_PHOTO = 2;
    public static final String ACTION_PICK_PHOTO = "pick_photo";

    @BindView(R.id.root)
    ConstraintLayout mContainer;

    @BindView(R.id.textureView)
    TextureView mCameraTextureView;

    private int mDisplayId = -1;
    private CameraX.LensFacing mLensFacing = CameraX.LensFacing.BACK;
    private Preview mPreview;
    private ImageCapture mImageCapture;
    private ImageAnalysis mImageAnalyzer;

    private HandlerThread mAnalyzerThread;
    private DisplayManager mDisplayManager;
    private DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        @Override
        public void onDisplayAdded(int i) {
        }

        @Override
        public void onDisplayRemoved(int i) {
        }

        @Override
        public void onDisplayChanged(int id) {
            if(mDisplayId == id) {
                int rotation = mContainer.getDisplay().getRotation();
                Log.d(TAG, "onDisplayChanged: "+rotation);
                if(mPreview!=null)
                mPreview.setTargetRotation(rotation);

                if(mPreview!=null)
                mImageCapture.setTargetRotation(rotation);

                if(mPreview!=null)
                mImageAnalyzer.setTargetRotation(rotation);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_x_layout);
        ButterKnife.bind(this);
        mAnalyzerThread = new HandlerThread("LuminosityAnalysis");
        mAnalyzerThread.start();

        mDisplayManager = (DisplayManager) mCameraTextureView.getContext().getSystemService(Context.DISPLAY_SERVICE);
        if(mDisplayManager!=null) mDisplayManager.registerDisplayListener(mDisplayListener,null);

        mCameraTextureView.post(() -> {
            mDisplayId = mCameraTextureView.getDisplay().getDisplayId();
            updateCameraUi();
            bindCameraUsecases();
        });
    }

    private void bindCameraUsecases() {
        DisplayMetrics metrics = new DisplayMetrics();
        mCameraTextureView.getDisplay().getRealMetrics(metrics);
        Rational screenAspectRatio = new Rational(metrics.widthPixels, metrics.heightPixels);
        Log.d(TAG, "Screen metrics: "+ metrics.widthPixels+" x "+metrics.heightPixels);

        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setLensFacing(mLensFacing)
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(mCameraTextureView.getDisplay().getRotation())
                .build();

        mPreview = AutoFitPreviewBuilder.Companion.build(previewConfig, mCameraTextureView);

        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()
                .setLensFacing(mLensFacing)
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(mCameraTextureView.getDisplay().getRotation())
                .build();

        mImageCapture = new ImageCapture(imageCaptureConfig);

        ImageAnalysisConfig analysisConfig = new ImageAnalysisConfig.Builder()
                .setLensFacing(mLensFacing)
                .setCallbackHandler(new Handler(mAnalyzerThread.getLooper()))
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .setTargetRotation(mCameraTextureView.getDisplay().getRotation())
                .build();

        mImageAnalyzer = new ImageAnalysis(analysisConfig);
        LuminosityAnalyzer analyzer = new LuminosityAnalyzer();
        mImageAnalyzer.setAnalyzer(analyzer);


        CameraX.bindToLifecycle(this,mPreview,mImageCapture, mImageAnalyzer);
    }

    private void updateCameraUi() {

    }


    @Override
    protected void onPause() {

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
