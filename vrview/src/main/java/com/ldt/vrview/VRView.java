package com.ldt.vrview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ldt.vrview.model.VRPhoto;

import java.util.ArrayList;

public class VRView extends FrameLayout implements View.OnClickListener, View.OnLongClickListener {

    public int id = 0;
    public VRView(@NonNull Context context) {
        super(context);
        init(null);
    }

    public VRView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public VRView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
     buildLayout();
    }

    private void buildLayout() {
        if(mControlView==null) {
            removeAllViews();
            mControlView = new VRControlView(getContext());
            mControlView.getGestureAttacher().setOnClickListener(this);
            mControlView.getGestureAttacher().setOnLongClickListener(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            addView(mControlView,params);
            View mOptionView = LayoutInflater.from(getContext()).inflate(R.layout.vr_option,this,false);
            addView(mOptionView);
        }
    }

    public void setVRPhoto(VRPhoto vrio) {
        if(mControlView!=null) {
            mControlView.setVRPhoto(vrio);
        }
    }

    public void clearVRPhoto() {
        if(mControlView!=null) {
            mControlView.setVRPhoto(null);
        }
    }

    public void onPause() {
        if(mControlView!=null) mControlView.onPause();
    }

    public void onResume() {
        if(mControlView!=null) mControlView.onResume();
    }

    protected VRControlView mControlView;

    public void recalibrate() {
        if(mControlView!=null) mControlView.recalibrate();
    }

    @Override
    public void onClick(View v) {
        mControlView.recalibrate();
    }

    public ArrayList<VRPhoto> mSampleVRPhotos = new ArrayList<>();
    public int curSamplePos = 0;

    @Override
    public boolean onLongClick(View v) {
        if(!mSampleVRPhotos.isEmpty()) {
            curSamplePos++;
            if(curSamplePos==mSampleVRPhotos.size()) mControlView.setVRPhoto(null);
            else {
                if(curSamplePos==mSampleVRPhotos.size()+1) curSamplePos=0;
                mControlView.setVRPhoto(mSampleVRPhotos.get(curSamplePos));
            }
        }
        return true;
    }

    public void setViewID(int id) {
        this.id = id;
        mControlView.setViewID(id);
    }
}
