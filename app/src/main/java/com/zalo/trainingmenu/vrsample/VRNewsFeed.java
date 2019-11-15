package com.zalo.trainingmenu.vrsample;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;

import com.ldt.vrview.model.VRPhoto;

public class VRNewsFeed implements Parcelable {
    public final String mAuthor;
    public final String mDescription;
    public VRPhoto mVRPhoto;
    public int mLike = 356;
    public int mComment = 121;
    public long mPublishTime = System.currentTimeMillis();

    @DrawableRes
    public int mDrawableID = -1;

    public String mUri = null;

    public String getAuthor() {
        return mAuthor;
    }

    public String getDescription() {
        return mDescription;
    }

    public VRPhoto getVRPhoto() {
        return mVRPhoto;
    }

    public int getLike() {
        return mLike;
    }

    public int getComment() {
        return mComment;
    }

    public long getPublishTime() {
        return mPublishTime;
    }

    public VRNewsFeed(String author, String description) {
        mAuthor = author;
        mDescription = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mAuthor);
        dest.writeString(this.mDescription);
        dest.writeInt(this.mLike);
        dest.writeInt(this.mComment);
        dest.writeLong(this.mPublishTime);
        dest.writeInt(this.mDrawableID);
        dest.writeString(this.mUri);
    }

    protected VRNewsFeed(Parcel in) {
        this.mAuthor = in.readString();
        this.mDescription = in.readString();
        this.mLike = in.readInt();
        this.mComment = in.readInt();
        this.mPublishTime = in.readLong();
        this.mDrawableID = in.readInt();
        this.mUri = in.readString();
    }

    public static final Parcelable.Creator<VRNewsFeed> CREATOR = new Parcelable.Creator<VRNewsFeed>() {
        @Override
        public VRNewsFeed createFromParcel(Parcel source) {
            return new VRNewsFeed(source);
        }

        @Override
        public VRNewsFeed[] newArray(int size) {
            return new VRNewsFeed[size];
        }
    };
}
