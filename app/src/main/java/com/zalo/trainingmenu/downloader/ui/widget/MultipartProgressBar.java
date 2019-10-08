package com.zalo.trainingmenu.downloader.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.zalo.trainingmenu.R;

import java.util.Random;

public class MultipartProgressBar extends View {
    private static final String TAG = "MultipartProgressBar";

    public MultipartProgressBar(Context context) {
        super(context);
        init(context,null);
    }

    public MultipartProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public MultipartProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private int mForegroundBarColor;
    private int mBackgroundBarColor;
    private Paint mForegroundBarPaint;
    private Paint mBackgroundBarPaint;

    private boolean mIsIndeterminate = true;
    private int mThickness;

    private void init(Context context, AttributeSet attrs) {

        mIVAnimator.setRepeatMode(ValueAnimator.RESTART);
        mIVAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mIVAnimator.setStartDelay(100);
        mIVAnimator.setInterpolator(new DecelerateInterpolator());
        mIVAnimator.addUpdateListener(valueAnimator -> {
            mCurrentIVValue = (float) valueAnimator.getAnimatedValue();
            update();
        });

        if(attrs!=null && context!=null) {
            TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.MultipartProgressBar);


            mForegroundBarPaint = new Paint();
            mBackgroundBarPaint = new Paint();
            mForegroundBarPaint.setAntiAlias(true);
            mBackgroundBarPaint.setAntiAlias(true);
            mForegroundBarPaint.setStyle(Paint.Style.FILL);
            mBackgroundBarPaint.setStyle(Paint.Style.FILL);

            setForegroundBarColor(t.getColor(R.styleable.MultipartProgressBar_foregroundBarColor,context.getResources().getColor(R.color.FlatOrange)));
            setBackgroundBarColor(t.getColor(R.styleable.MultipartProgressBar_backgroundBarColor,context.getResources().getColor(R.color.FlatWhite)));


            mThickness = (int) t.getDimension(R.styleable.MultipartProgressBar_thickness,context.getResources().getDimension(R.dimen.dp_6));


            int number = t.getInteger(R.styleable.MultipartProgressBar_connections,6);
            mPercentages = new int[number];
            if(t.getBoolean(R.styleable.MultipartProgressBar_randomPercentages,false))
            randomPercentages();

            setIndeterminateDuration(t.getInteger(R.styleable.MultipartProgressBar_indeterminateDuration,context.getResources().getInteger(android.R.integer.config_mediumAnimTime)));
            setIndeterminate(t.getBoolean(R.styleable.MultipartProgressBar_indeterminate,false));
            mIsAnimateOnChanged = t.getBoolean(R.styleable.MultipartProgressBar_animateOnChanged,true);

            t.recycle();
        }
    }

    private void randomPercentages() {
        Random random = new Random();
        for (int i = 0; i < mPercentages.length; i++) {
            mPercentages[i] = random.nextInt(100);
        }
    }

    public void setPercentages(int... values) {
        mPercentages = values;
        update();
    }

    public void updatePercentage(int index, int value) {
        if(index<mPercentages.length) {
            mPercentages[index] = value;
            update();
        }
    }


    public boolean isAnimateOnChanged() {
        return mIsAnimateOnChanged;
    }

    public void setAnimateOnChanged(boolean animateOnChanged) {
        mIsAnimateOnChanged = animateOnChanged;
        setPercentages(5,6,7);
    }

    private boolean mIsAnimateOnChanged;


    public long getIndeterminateDuration() {
        return mIndeterminateDuration;
    }

    public void setIndeterminateDuration(long indeterminateDuration) {
        mIndeterminateDuration = indeterminateDuration;
        mIVAnimator.setDuration(mIndeterminateDuration);
    }

    private long mIndeterminateDuration;


    public int getThickness() {
        return mThickness;
    }

    public void setThickness(int thickness) {
        mThickness = thickness;
    }

    public int getForegroundBarColor() {
        return mForegroundBarColor;
    }

    public void setForegroundBarColor(int foregroundBarColor) {
        mForegroundBarColor = foregroundBarColor;
        mForegroundBarPaint.setColor(mForegroundBarColor);
        update();
    }

    public boolean isIndeterminate() {
        return mIsIndeterminate;
    }

    public int getBackgroundBarColor() {
        return mBackgroundBarColor;
    }

    public void setBackgroundBarColor(int backgroundBarColor) {
        mBackgroundBarColor = backgroundBarColor;
        mBackgroundBarPaint.setColor(mBackgroundBarColor);
        update();
    }


    public void setIndeterminate(boolean value) {
        Log.d(TAG, "set indeterminate to "+value+" from "+mIsIndeterminate);
        mIsIndeterminate = value;
        if(mIsIndeterminate && !mIVAnimator.isRunning()) {
            mIVAnimator.start();
        } else if(!mIsIndeterminate && mIVAnimator.isRunning()) mIVAnimator.cancel();
        update();
    }

    float mCurrentIVValue = 0;
    ValueAnimator mIVAnimator = ValueAnimator.ofFloat(0,1);

    private void update() {
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDrawTop = getPaddingTop();
        mDrawBottom = h - getPaddingBottom();
        mDrawLeft = getPaddingStart();
        mDrawRight = w - getPaddingEnd();

        mProgressBarTop = (mDrawTop + mDrawBottom - mThickness)/2;
        mProgressBarBottom = mProgressBarTop + mThickness;

        mProgressBarLeft = mDrawLeft;
        mProgressBarRight = mDrawRight;

        mProgressBarWidth = mProgressBarRight - mProgressBarLeft;
    }
    private int mDrawTop;
    private int mDrawBottom;
    private int mDrawLeft;
    private int mDrawRight;

    private int mProgressBarTop;
    private int mProgressBarBottom;
    private int mProgressBarLeft;
    private int mProgressBarRight;

    private int mProgressBarWidth;
    private int[] mPercentages;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackgroundBar(canvas);
        if(isIndeterminate())
            drawIndeterminate(canvas);
        else
        drawForegroundBar(canvas);
    }

    private void drawIndeterminate(Canvas canvas) {
        Log.d(TAG, "draw indeterminate");

        float indeterminateWidth = mProgressBarWidth * 112/240f;
        float indeterminateLeft = (mProgressBarLeft  - indeterminateWidth) + (mProgressBarWidth + indeterminateWidth)*mCurrentIVValue;
        float indeterminateRight = indeterminateLeft +indeterminateWidth;
        if(indeterminateLeft<mProgressBarLeft) indeterminateLeft = mProgressBarLeft;
        if(indeterminateRight> mProgressBarRight) indeterminateRight = mProgressBarRight;
        canvas.drawRect(indeterminateLeft, mProgressBarTop,indeterminateRight,mProgressBarBottom,mForegroundBarPaint);
    }

    private void drawBackgroundBar(Canvas canvas) {
        canvas.drawRect(mProgressBarLeft,mProgressBarTop, mProgressBarRight, mProgressBarBottom,mBackgroundBarPaint);
    }

    private void drawForegroundBar(Canvas canvas) {
        Log.d(TAG, "draw foreground");

        int number = mPercentages.length;
        float chunkWidth = (float) mProgressBarWidth/number;
        for (int i = 0; i < number; i++) {
            float chunkLeft = mProgressBarLeft + chunkWidth*i;
            float chunkPercentWidth = chunkWidth * mPercentages[i]/100;
            canvas.drawRect(chunkLeft,mProgressBarTop,chunkLeft + chunkPercentWidth , mProgressBarBottom,mForegroundBarPaint);
        }

    }
}
