package com.ldt.vrview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.ldt.vrview.transform.Transformer;

public class AlignButton extends View {
    private static final String TAG = "AlignButton";
    private Paint mPaint;

    public AlignButton(Context context) {
        super(context);
        init(null);
    }

    public AlignButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public AlignButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setWillNotDraw(false);

        _1dp = getResources().getDimension(R.dimen.oneDp);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    void attachTransformer(Transformer transformer) {

    }

    void detachTransformer() {

    }

    private int mDrawTop;
    private int mDrawBottom;
    private int mDrawLeft;
    private int mDrawRight;
    private int mDrawWidth;
    private int mDrawHeight;

    private int mCenterX;
    private int mCenterY;
    private float mRadius;
    private float mSize;
    private RectF mRectF;
    private RectF mEllipseRectF = new RectF();

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDrawTop = getPaddingTop();
        mDrawBottom = h - getPaddingBottom();
        mDrawLeft = getPaddingStart();
        mDrawRight = w - getPaddingEnd();

        mDrawWidth = mDrawRight - mDrawLeft;
        mDrawHeight = mDrawBottom - mDrawTop;
        mSize = Math.min(mDrawWidth, mDrawHeight);
        mCenterX = mDrawLeft + mDrawWidth/2;
        mCenterY = mDrawTop + mDrawHeight/2;
        mRadius = mSize/2;

        if (mRectF==null)
            mRectF = new RectF(mDrawLeft,mDrawTop, mDrawRight,mDrawBottom);
        else {
            mRectF.left = mDrawLeft;
            mRectF.top = mDrawTop;
            mRectF.right = mDrawRight;
            mRectF.bottom = mDrawBottom;
        }

        mInMargin = 3/32f * mSize; // 3dp of 50 dp
        mOutMargin = 5/32f * mSize;
        mCenterDotRadius = 1.5f / 32f * mSize;
        mOutlineWidth = 1.5f /32f * mSize;


        mRadialGradient = new RadialGradient(mCenterX, mCenterY, (float) (mRadius - mOutMargin - mInMargin - mCenterDotRadius - mOutlineWidth), 0x66FFFFFF,
                0x00FFFFFF, android.graphics.Shader.TileMode.CLAMP);

    }

    int centerDotColor = 0x88FFFFFF;
    int circleBackgroundColor = 0xAA000000;
    int ellipseColor = Color.WHITE;
    int outlineColor = Color.WHITE;
    int viewPortColor = 0x88ffffff;

   /* int centerDotColor = 0xFF0068FF;
    int circleBackgroundColor = 0xAA0068FF;
    int ellipseColor = 0xAA0068FF;
    int outlineColor = Color.WHITE;
    int viewPortColor = 0x88ffffff;*/

    boolean isOutlineFill = false;

    //#0068FF

    float _1dp = 1;
    float mInMargin = 0;
    float mOutMargin = 0;
    float mCenterDotRadius = 0;
    float mOutlineWidth;
    RadialGradient mRadialGradient;
    Path mLightPath;

    private Path getLightPath(float x1, float y1, float x2, float y2,float x3, float y3, float x4, float y4) {
        if(mLightPath ==null) mLightPath = new Path();
        else mLightPath.rewind();
        mLightPath.moveTo(x1,y1);
        mLightPath.lineTo(x2,y2);
        mLightPath.lineTo(x3,y3);
        mLightPath.lineTo(x4,y4);

        mLightPath.close();
        return mLightPath;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float ellipseRadiusHeight = mRadius/7f;
        float ellipseCenterY = mCenterY - mRadius + mOutMargin + mOutlineWidth + mInMargin + ellipseRadiusHeight/2;
        float ellipseRadiusWidth = (float) Math.sqrt((mRadius- mInMargin - mOutMargin - mOutlineWidth)*(mRadius - mInMargin - mOutMargin - mOutlineWidth) - (mCenterY - ellipseCenterY)*(mCenterY - ellipseCenterY));

        mEllipseRectF.set(mCenterX-ellipseRadiusWidth,ellipseCenterY- ellipseRadiusHeight,mCenterX+ellipseRadiusWidth,ellipseCenterY+ellipseRadiusHeight);

        // Draw a translucent background circle
        mPaint.setColor(circleBackgroundColor);
        mPaint.setStyle(Paint.Style.STROKE);
        //mPaint.setStyle(Paint.Style.FILL);
        float sw = (mRadius - mCenterDotRadius);
        mPaint.setStrokeWidth(sw);
        canvas.drawCircle(mCenterX,mCenterY,sw/2 + mCenterDotRadius,mPaint);

        // Draw an outline stroke circle
        mPaint.setColor(outlineColor);
        if(isOutlineFill)
        mPaint.setStyle(Paint.Style.FILL);
        else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mOutlineWidth);
        }
        canvas.drawCircle(mCenterX,mCenterY,mRadius - mOutMargin, mPaint);

        canvas.save();
        canvas.rotate(mRotateDegree,mCenterX,mCenterY);

        // Draw the light
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setDither(true);
        mPaint.setShader(mRadialGradient);
        Path path = getLightPath(
                mCenterX - mCenterDotRadius,mCenterY,
                mCenterX-ellipseRadiusWidth,ellipseCenterY,
                mCenterX+ellipseRadiusWidth,ellipseCenterY,
                mCenterX + mCenterDotRadius, mCenterY);
        canvas.drawPath(path,mPaint);
        mPaint.setDither(false);
        mPaint.setShader(null);

        // Draw the ellipse

        mPaint.setColor(ellipseColor);
        mPaint.setStyle(Paint.Style.FILL);

        canvas.drawOval(mEllipseRectF,mPaint);
        canvas.restore();

        // draw the center dot
        mPaint.setColor(centerDotColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mCenterX,mCenterY,mCenterDotRadius,mPaint);
    }



    public float getRotateDegree() {
        return mRotateDegree;
    }

    public void setRotateDegree(float rotateDegree) {
        rotateDegree%=360;
        if(rotateDegree!=mRotateDegree) {
            mRotateDegree = rotateDegree;
            postInvalidate();
        }
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float scale) {
        if(scale!=mScale) {
            mScale = scale;
            postInvalidate();
        }
    }

    public float getUpDownDegree() {
        return mUpDownDegree;
    }

    public void setUpDownDegree(float upDownDegree) {
        upDownDegree%=360;
        if(upDownDegree!=mUpDownDegree) {
            mUpDownDegree = upDownDegree;
            postInvalidate();
        }
    }

    private float mUpDownDegree = 0; // from -180 -> 0 -> 180
    private float mRotateDegree = 0; // from -180 -> 0 -> 180
    private float mScale = 1; // 1 -> 0
}
