package com.zalo.servicetraining.ui.userdata;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import com.zalo.servicetraining.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserProfileActivity extends AppCompatActivity {
    @BindView(R.id.avatar)
    ImageView avatar;
    @BindView(R.id.title)
    TextView username;
    @BindView(R.id.description)
    TextView description;
    Bitmap bmp;
    String str_username;
    int mID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        // FullScreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        ButterKnife.bind(this);

        byte[] byteArray = getIntent().getByteArrayExtra("avatar");
        str_username = getIntent().getStringExtra("username");
        mID = getIntent().getIntExtra("id",0);
        bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        avatar.setImageBitmap(bmp);

        username.setText(str_username);
        description.setText("ID "+ mID);
    }

    @OnClick(R.id.back_button)
    void back() {
        finish();
    }
}
