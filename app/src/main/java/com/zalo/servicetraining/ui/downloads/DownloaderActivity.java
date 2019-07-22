package com.zalo.servicetraining.ui.downloads;

import android.view.Gravity;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zalo.servicetraining.R;
import com.zalo.servicetraining.ui.base.AbsListActivity;

import butterknife.BindView;

public class DownloaderActivity extends AbsListActivity {

    @BindView(R.id.root)
    CoordinatorLayout mRoot;

    FloatingActionButton mAddButton;

    void addNewTask() {
        AddTaskFormBottomSheet.newInstance().show(getSupportFragmentManager(),AddTaskFormBottomSheet.TAG);
    }

    private void addPlusButton() {
        mAddButton = new FloatingActionButton(this);
        mAddButton.setImageResource(R.drawable.ic_add_black_24dp);
        float oneDP = getResources().getDimension(R.dimen.oneDp);
        mAddButton.setCustomSize((int) (60*oneDP));
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams((int)(60*oneDP),(int)(60*oneDP));
        params.gravity = Gravity.BOTTOM|Gravity.END;
        params.bottomMargin = (int)(oneDP*16);
        params.setMarginEnd(params.bottomMargin);
        mAddButton.setOnClickListener(view -> addNewTask());
        mRoot.addView(mAddButton,params);
    }

    @Override
    protected void onInitRecyclerView() {
        addPlusButton();
    }

    @Override
    protected void refreshData() {
        super.refreshData();
    }
}
