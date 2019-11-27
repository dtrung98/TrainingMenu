package com.ldt.vrview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ldt.vrview.model.VRPhoto;
import com.ldt.vrview.transform.TransformListener;
import com.ldt.vrview.transform.TransformManager;

import java.text.DecimalFormat;

public class VRView extends FrameLayout implements TransformListener {

    public int id = 0;
    public VRView(@NonNull Context context) {
        super(context);
        init(null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mControlView.onResume();
    }

    @Override
    protected void onDetachedFromWindow() {
        mControlView.onPause();
        super.onDetachedFromWindow();
    }

    public VRView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public VRView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        if(mControlView!=null)
        mControlView.getGestureAttacher().setOnLongClickListener(l);
    }


    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        if(mControlView!=null)
            mControlView.getGestureAttacher().setOnClickListener(l);
    }

    private void init(AttributeSet attrs) {
     buildLayout();
    }

    private void buildLayout() {
        if(mControlView==null) {
            removeAllViews();
            mControlView = new VRControlView(getContext());
            mControlView.setTransformListener(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            addView(mControlView,params);
            View mOptionView = LayoutInflater.from(getContext()).inflate(R.layout.vr_option,this,false);
            mAlignButton = mOptionView.findViewById(R.id.align_button);
            mAlignButton.setOnClickListener((view)-> align());
            mTextView = mOptionView.findViewById(R.id.text_view);
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
    protected AlignButton mAlignButton;
    protected TextView mTextView;

    public void align() {
        if(mControlView!=null) mControlView.align();
    }


    /*public ArrayList<VRPhoto> mSampleVRPhotos = new ArrayList<>();
    public int curSamplePos = 0;*/

 /*   @Override
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
    }*/

    public void setViewID(int id) {
        this.id = id;
        mControlView.setViewID(id);
    }
    DecimalFormat df = new DecimalFormat("0.00");
    @Override
    public void onTransformChanged(int which, float[] angle3) {
        if(mAlignButton!=null) {
            mAlignButton.setRotateDegree(angle3[0]);
            mAlignButton.setUpDownDegree(angle3[1]);
        }

        if(which== TransformManager.GESTURE_TRANSFORMER)
            post(() -> {
               mAlignButton.keepActive();
            });

        post(() -> {
            if (mTextView != null)
                mTextView.setText("transform " + df.format(angle3[0]) + ", " + df.format(angle3[1]));
        });

    }
}
