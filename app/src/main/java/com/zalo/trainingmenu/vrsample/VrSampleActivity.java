package com.zalo.trainingmenu.vrsample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ldt.menulayout.ui.AbsLocaleActivity;
import com.ldt.vrview.VRView;
import com.ldt.vrview.model.VRPhoto;
import com.zalo.trainingmenu.R;

import java.util.ArrayList;

public class VrSampleActivity extends AbsLocaleActivity {
    private static final String TAG = "VrContextActivity";

    public static final String ACTION_VIEW_NEWS_FEED = "view_news_feed";
    public static final String EXTRA_VR_NEWS_FEED = "vr_news_feed";


    private VRView mVRView;
    private View mFullScreenView;
    private void buildLayout(ViewGroup root) {
        if(mFullScreenView==null) {
            mFullScreenView = LayoutInflater.from(this).inflate(R.layout.fullscreen_vr_layout,root,false);
            mFullScreenView.findViewById(R.id.back_button).setOnClickListener((v) -> finish());
            mVRView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mFullScreenView.getVisibility()==View.VISIBLE)
                    mFullScreenView.animate().alpha(0).withEndAction(() -> mFullScreenView.setVisibility(View.GONE)).start();
                    else mFullScreenView.animate().alpha(1).withStartAction(() -> mFullScreenView.setVisibility(View.VISIBLE)).start();
                }
            });
            root.addView(mFullScreenView);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vr_layout);
        mVRView = findViewById(R.id.vr_view);
        buildLayout(findViewById(R.id.root));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR );

        Intent intent = getIntent();
        if(intent != null && ACTION_VIEW_NEWS_FEED.equals(intent.getAction())) {
            // view news feed
            VRNewsFeed newsFeed = intent.getParcelableExtra(EXTRA_VR_NEWS_FEED);


            if(newsFeed!=null) {
                if(mFullScreenView!=null) {
                    ((TextView)mFullScreenView.findViewById(R.id.author_text_view)).setText(newsFeed.mAuthor);
                    ((TextView)mFullScreenView.findViewById(R.id.description_text_view)).setText(newsFeed.mDescription);
                }

                AsyncTask.execute(() -> {
                    newsFeed.mVRPhoto = createPhoto(newsFeed.mDrawableID);
                    mVRView.post(()->mVRView.setVRPhoto(newsFeed.getVRPhoto()));
                });
            }

        } else
            buildSample();
    }

    ArrayList<VRPhoto> mVRPhotos;
    private int mCurrentPos = 0;

    private void buildSample() {
        mVRView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!mVRPhotos.isEmpty()) {
                    mCurrentPos++;
                    if(mCurrentPos == mVRPhotos.size()) mVRView.setVRPhoto(null);
                    else {
                        if(mCurrentPos == mVRPhotos.size()+1) mCurrentPos = 0;
                        mVRView.setVRPhoto(mVRPhotos.get(mCurrentPos));
                    }
                }
                return true;
            }
        });

        if(mVRPhotos==null) mVRPhotos = new ArrayList<>(4);
        AsyncTask.execute(() -> {
            mVRPhotos.add(createPhoto(R.drawable._360sp));
            mVRPhotos.add(createPhoto(R.drawable._360x));
            mVRPhotos.add(createPhoto(R.drawable.rural));
            mVRPhotos.add(createPhoto(R.drawable.down1));
            mVRPhotos.add(createPhoto(R.drawable.down2));
            mVRView.post(() -> {
                mVRView.setVRPhoto(mVRPhotos.get(0));
            });
        });
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

    @Override
    protected int title() {
        return R.string.vr_sample;
    }

    @Override
    protected void onResume() {
       super.onResume();
       mVRView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVRView.onPause();
    }
}
