package com.ldt.vrview.model;

import android.content.Context;
import android.graphics.Bitmap;

public class VRPhoto {
    private static final String TAG = "VRImageObject";
    public static final String GPANO_FULL_PANO_HEIGHT_PIXELS = "GPano:FullPanoHeightPixels";
    public static final String GPANO_FULL_PANO_WIDTH_PIXELS = "GPano:FullPanoWidthPixels";
    public static final String GPANO_CROPPED_AREA_IMAGE_WIDTH_PIXELS = "GPano:CroppedAreaImageWidthPixels";
    public static final String GPANO_CROPPED_AREA_IMAGE_HEIGHT_PIXELS = "GPano:CroppedAreaImageHeightPixels";
    public static final String GPANO_CROPPED_AREA_TOP_PIXELS = "GPano:CroppedAreaTopPixels";
    public static final String GPANO_CROPPED_AREA_LEFT_PIXELS = "GPano:CroppedAreaLeftPixels";
    public static final String GPANO_PROJECTION_TYPE = "GPano:ProjectionType";
    public static float[] getDefaultAngleAreas( ){
        return new float[] {0,0,360,180};
    }


    private final float[] mStartingAngles;
    private float[] mCurrentAngles = new float[3];

    private float mCurrentScale;
    private final float mStartingScale;
    private Bitmap mThumbnailBitmap;

    public float[] getAngleAreas() {
        return mAngleAreas;
    }

    private final float[] mAngleAreas = new float[] {0,0,360,180};

    public void setThumbnailBitmap(Bitmap thumbnailBitmap) {
        mThumbnailBitmap = thumbnailBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    private Bitmap mBitmap;

    public String getPhotoTitle() {
        return mPhotoTitle;
    }

    public String getPhotoDescription() {
        return mPhotoDescription;
    }

    private final String mPhotoTitle;
    private final String mPhotoDescription;

    public VRPhoto(Builder builder) {
        mThumbnailBitmap = builder.mThumbnailBitmap;
        mBitmap = builder.mBitmap;
        mCurrentAngles = mStartingAngles = builder.mStartingVector;
        mCurrentScale = mStartingScale = builder.mStartingScale;
        mPhotoTitle = builder.mPhotoTitle;
        mPhotoDescription = builder.mPhotoDescription;
        System.arraycopy(builder.mAreaAngles,0,mAngleAreas,0,4);
    }

    public float getCurrentScale() {
        return mCurrentScale;
    }

    public void setCurrentScale(float currentScale) {
        mCurrentScale = currentScale;
    }

    public float getStartingScale() {
        return mStartingScale;
    }


    public VRPhoto setStartingVector(float x, float y, float z) {
        mStartingAngles[0] = x;
        mStartingAngles[1] = y;
        mStartingAngles[2] = z;
        return this;
    }

    public VRPhoto setCurrentVector(float x, float y, float z) {
        mCurrentAngles[0] = x;
        mCurrentAngles[1] = y;
        mCurrentAngles[2] = z;
        return this;
    }


    public float[] getStartingAngles() {
        float[] temp = new float[3];
        System.arraycopy(mStartingAngles,0,temp,0,3);
        return temp;
    }

    public float[] getCurrentAngles() {
        float[] temp = new float[3];
        System.arraycopy(mCurrentAngles,0,temp,0,3);
        return temp;
    }


    public Bitmap getThumbnailBitmap() {
        return mThumbnailBitmap;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    public static class Builder {
        private Context mContext;
        public Builder(Context context) {
            mContext = context;
        }

        private float[] mStartingVector = new float[3];

        private float mStartingScale = 1;
        private Bitmap mThumbnailBitmap;
        private Bitmap mBitmap;
        private String mPhotoTitle = "";

        public Builder setAreaAngles(float[] areaAngles) {
            if(areaAngles!=null&&areaAngles.length>=4)
            System.arraycopy(areaAngles,0,mAreaAngles,0,4);
            return this;
        }

        public Builder setAreaAngles(float leftAngle, float topAngle, float horizontalAngle, float verticalAngle) {
            mAreaAngles[0] = leftAngle;
            mAreaAngles[1] = topAngle;
            mAreaAngles[2] = horizontalAngle;
            mAreaAngles[3] = verticalAngle;
            return this;
        }

        private float[] mAreaAngles = new float[]{0,0,360,180};

        public Builder setPhotoDescription(String photoDescription) {
            mPhotoDescription = photoDescription;
            return this;
        }

        private String mPhotoDescription = "";
        public Builder setPhotoTitle(String title) {
            if(title!=null)
            mPhotoTitle = title;
            return this;
        }

        public Builder setStartingScale(float value) {
            mStartingScale = value;
            return this;
        }

        public Builder setStartingVector(float[] vector3) {
            System.arraycopy(vector3,0,mStartingVector,0,3);
            return this;
        }

        public Builder setThumbNail(Bitmap bitmap) {
            mThumbnailBitmap = bitmap;
            return this;
        }

        public Builder setBitmap(Bitmap bitmap) {
            mBitmap = bitmap;
            return this;
        }

        public VRPhoto get() {
            mContext = null;
            return new VRPhoto(this);
        }
    }
}
