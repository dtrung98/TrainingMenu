package com.zalo.trainingmenu.downloader.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.mainui.MainActivity;
import com.zalo.trainingmenu.mainui.base.AbsLocaleActivity;

import java.util.ArrayList;

public class SettingActivity extends AbsLocaleActivity {

    protected Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
}
