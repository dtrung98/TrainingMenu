package com.zalo.trainingmenu.downloader.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.zalo.trainingmenu.App;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.downloader.ui.permission.PermissionActivity;
import com.zalo.trainingmenu.mainui.MainActivity;
import com.zalo.trainingmenu.mainui.base.AbsLocaleActivity;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class SettingActivity extends AbsLocaleActivity {
    public static final String ACTION_CHOOSE_DOWNLOAD_FOLDER = "choose_download_folder";

    protected Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        bindView();
        setToolbarIcon(R.drawable.ic_arrow_back_24dp);
        setupToolbarAndStatusBar(mToolbar);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new SettingFragment())
                .commit();
    }

    @Override
    protected int title() {
        return R.string.settings;
    }

    private void bindView() {
        mToolbar = findViewById(R.id.toolbar);
    }

    private boolean canBack(){
        return true;}

    private void setupToolbarAndStatusBar(@Nullable Toolbar toolbar) {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (canBack()) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    if (canBack()) {
                        View navIcon = getToolbarNavigationIcon(toolbar);
                        if (navIcon != null) {
                            navIcon.setOnLongClickListener(v -> {
                                Intent intent = new Intent(this, MainActivity.class);
                                startActivity(intent);
                                finish();
                                return true;
                            });
                        }
                    }
                }
            }
        }
    }

    @Nullable private View getToolbarNavigationIcon(Toolbar toolbar) {
        boolean hadContentDescription = TextUtils.isEmpty(toolbar.getNavigationContentDescription());
        String contentDescription = !hadContentDescription ? String.valueOf(toolbar.getNavigationContentDescription()) : "navigationIcon";
        toolbar.setNavigationContentDescription(contentDescription);
        ArrayList<View> potentialViews = new ArrayList<>();
        toolbar.findViewsWithText(potentialViews, contentDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
        View navIcon = null;
        if (potentialViews.size() > 0) {
            navIcon = potentialViews.get(0);
        }
        if (hadContentDescription) toolbar.setNavigationContentDescription(null);
        return navIcon;
    }


    protected void setToolbarIcon(@DrawableRes int res) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(res);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (canBack()) {
            if (item.getItemId() == android.R.id.home) {
                onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(Intent intent, int permissionType, boolean granted) {
        super.onRequestPermissionsResult(intent, permissionType, granted);
        if(intent==null) return;
        String action = intent.getAction();
        if(action!=null&&!action.isEmpty()&&permissionType== PermissionActivity.PERMISSION_STORAGE)
            switch (action) {
                case ACTION_CHOOSE_DOWNLOAD_FOLDER:
                    if(granted) {
                        FolderChooserDialog.newInstance().show(getSupportFragmentManager(),FolderChooserDialog.TAG);
                    }
                    else Toasty.error(App.getInstance().getApplicationContext(),"Couldn't choose folder without storage permissions!").show();
                    break;
            }
    }
}
