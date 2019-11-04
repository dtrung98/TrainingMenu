package com.zalo.trainingmenu.fundamental.camera;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.camerakit.CameraKitView;
import com.zalo.trainingmenu.R;

import org.jetbrains.annotations.NotNull;


public class CameraKitActivity extends AppCompatActivity {
    private static final String TAG = "CameraXActivity";
    private CameraKitView cameraKitView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_kit_layout);
        cameraKitView = findViewById(R.id.camera);
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
