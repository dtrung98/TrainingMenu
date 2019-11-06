package com.zalo.trainingmenu.vrsample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ldt.menulayout.ui.AbsLocaleActivity;
import com.ldt.vrview.model.VRPhoto;
import com.zalo.trainingmenu.R;

import java.util.ArrayList;

public class VRListActivity extends AbsLocaleActivity {
    RecyclerView mRecyclerView;
    VRListAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vr_list);

        mRecyclerView = findViewById(R.id.recycler_view);
        mAdapter = new VRListAdapter();

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        ArrayList<VRPhoto> list = new ArrayList<>();
        AsyncTask.execute(() -> {
            list.add(createPhoto(R.drawable._360sp));
            list.add(createPhoto(R.drawable._360x));
            list.add(createPhoto(R.drawable.down1));
            list.add(createPhoto(R.drawable.down2));
            list.add(createPhoto(R.drawable._360sp));
            list.add(createPhoto(R.drawable._360x));
            list.add(createPhoto(R.drawable.down1));
            list.add(createPhoto(R.drawable.down2));
            list.add(createPhoto(R.drawable._360sp));
            list.add(createPhoto(R.drawable._360x));
            list.add(createPhoto(R.drawable.down1));
            list.add(createPhoto(R.drawable.down2));
            mRecyclerView.post(() -> {
                mAdapter.setData(list);
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
        return R.string.vr_list_sample;
    }
}
