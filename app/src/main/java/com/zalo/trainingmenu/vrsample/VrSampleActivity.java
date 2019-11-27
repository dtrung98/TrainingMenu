package com.zalo.trainingmenu.vrsample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import com.zalo.trainingmenu.App;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.downloader.ui.base.OptionBottomSheet;
import com.zalo.trainingmenu.util.Util;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class VrSampleActivity extends AbsLocaleActivity {
    private static final String TAG = "VrContextActivity";

    public static final String ACTION_VIEW_NEWS_FEED = "view_news_feed";
    public static final String EXTRA_VR_NEWS_FEED = "vr_news_feed";
    public static final String ACTION_PICK_PHOTO_FROM_GALLERY = "ACTION_PICK_PHOTO_FROM_GALLERY";
    public static final int REQUEST_CODE_PICK_FROM_GALLERY = 1;

    private VRView mVRView;
    private View mFullScreenView;

    private void buildLayout(ViewGroup root) {
        if(mFullScreenView==null) {
            mFullScreenView = LayoutInflater.from(this).inflate(R.layout.fullscreen_vr_layout,root,false);
            mFullScreenView.findViewById(R.id.back_button).setOnClickListener((v) -> finish());
            mVRView.setOnClickListener(v -> {
                if(mFullScreenView.getVisibility()==View.VISIBLE)
                mFullScreenView.animate().alpha(0).withEndAction(() -> mFullScreenView.setVisibility(View.GONE)).start();
                else mFullScreenView.animate().alpha(1).withStartAction(() -> mFullScreenView.setVisibility(View.VISIBLE)).start();
            });

            View menuButton = mFullScreenView.findViewById(R.id.menu_button);
            if(menuButton!=null) menuButton.setOnClickListener(v -> showOption());

            View pickButton = mFullScreenView.findViewById(R.id.pick_image_button);
            if(pickButton!=null) pickButton.setOnClickListener(v -> pickFromGallery());

            root.addView(mFullScreenView);
        }
    }

    private void updateMode(int mode) {
        if(mode!=mMode) {
            mMode = mode;
            if (mMode == MODE_SAMPLE) {
                mFullScreenView.findViewById(R.id.sample_group).setVisibility(View.VISIBLE);
                mFullScreenView.findViewById(R.id.pick_image_button).setVisibility(View.GONE);

            } else if (mMode == MODE_GALLERY) {
                mFullScreenView.findViewById(R.id.sample_group).setVisibility(View.GONE);
                mFullScreenView.findViewById(R.id.pick_image_button).setVisibility(View.VISIBLE);
            }

            if(mFullScreenView.getVisibility()!=View.VISIBLE)
                mFullScreenView.animate().alpha(1).withStartAction(() -> mFullScreenView.setVisibility(View.VISIBLE)).start();
        }
    }

    private void pickFromGallery() {
        executeWriteStorageAction(new Intent(ACTION_PICK_PHOTO_FROM_GALLERY));
    }

    @Override
    public void onRequestPermissionsResult(Intent intent, int permissionType, boolean granted) {
        super.onRequestPermissionsResult(intent, permissionType, granted);
        if(intent!=null&&ACTION_PICK_PHOTO_FROM_GALLERY.equals(intent.getAction())&&granted) {
            try {
                Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
                pickIntent.setType("image/*");
                startActivityForResult(pickIntent, REQUEST_CODE_PICK_FROM_GALLERY);
            } catch (Exception e) {
                Toasty.error(this,"Something went wrong!").show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE_PICK_FROM_GALLERY && resultCode== Activity.RESULT_OK&&data!=null) {
            setVRPhotoWithUri(data.getData());
        }
    }

    private void setVRPhotoWithUri(Uri data) {
        boolean valid = true;
        if(data==null) valid = false;
        Bitmap bitmap = null;
        if(valid)
        try {
            bitmap = Util.getBitmapWithUri(this,data);
        } catch (Exception e) {
            valid = false;
        }

        if(bitmap==null) valid = false;

        if(!valid)
            Toasty.error(App.getInstance(),"Image is unavailable").show();
        else {
            // valid
            mVRView.setVRPhoto(VRPhoto.with(this).setBitmap(bitmap).get());
        }
    }

    private int[] mSampleOptionMenu = new int[] {
            R.string.warning_divider,
            R.string.sample_mode,
            R.string.normal,
            R.string.gallery_chooser_mode
    };

    private int[] mGalleryOptionMenu = new int[] {
            R.string.sample_mode,
            R.string.warning_divider,
            R.string.gallery_chooser_mode
    };

    public static final int MODE_SAMPLE = 0;
    public static final int MODE_GALLERY = 1;

    private int mMode = MODE_SAMPLE;

    void showOption() {
        int[] options = (mMode==MODE_SAMPLE) ? mSampleOptionMenu : mGalleryOptionMenu;
        OptionBottomSheet.newInstance(options, new OptionBottomSheet.CallBack() {
            @Override
            public boolean onOptionClicked(int optionID) {
                switch (optionID) {
                    case R.string.sample_mode:
                        updateMode(MODE_SAMPLE);
                        break;
                    case R.string.gallery_chooser_mode:
                        updateMode(MODE_GALLERY);
                        break;
                }
                return true;
            }
        }).show(getSupportFragmentManager(),"OPTION_MENU");
    };


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
                if(!mVRPhotos.isEmpty()&&mMode==MODE_SAMPLE) {
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
