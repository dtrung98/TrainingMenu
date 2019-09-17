package com.zalo.trainingmenu.fundamental.photo3d;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;

import com.zalo.trainingmenu.fundamental.texture.GLTextureView;

public class Photo3DView extends GLTextureView {
    private Photo3DRenderer mRenderer;

    public void setOriginalPhoto(Bitmap bitmap) {
        mRenderer.setBitmap(bitmap);
        requestLayout();
    }

    public void setDepthPhoto(Bitmap bitmap) {
        mRenderer.setDepthMap(bitmap);
        requestLayout();
    }

    public void removeBitmaps() {
        mRenderer.removeBitmaps();
        requestLayout();
    }

    public Photo3DView(Context context) {
        super(context);
        init();
    }

    public Photo3DView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Photo3DView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        setEGLContextClientVersion(2);
        mRenderer = new Photo3DRenderer();
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();

        if(mRenderer!=null) {
            Bitmap bitmap = mRenderer.getBitmap();
            Bitmap depth = mRenderer.getDepthMap();
            if (bitmap == null && depth == null) {
                if (w > h) w = h;
                else h = w;
            } else {
                float ratio = 1f;
                if (bitmap != null)
                    ratio = (float) bitmap.getHeight() / bitmap.getWidth();
                else ratio = (float) depth.getHeight() / depth.getWidth();
                h = (int) (ratio*w);
            }
        }
        setMeasuredDimension(w,h);
    }
}
