package com.ldt.vrview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ldt.vrview.model.VRImage;

public class VRView extends FrameLayout {

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
     initViews();
    }

    private void initViews() {
        if(mControlView==null) {
            removeAllViews();
            mControlView = new VRControlView(getContext());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            addView(mControlView,params);
        }
    }

    public void attachVRImageObject(VRImage vrio) {

    }

    public void detachVRImageObject() {

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
}
