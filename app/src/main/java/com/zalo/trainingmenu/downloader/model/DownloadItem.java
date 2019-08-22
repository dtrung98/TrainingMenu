package com.zalo.trainingmenu.downloader.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.zalo.trainingmenu.util.Util;

import static com.zalo.trainingmenu.util.Util.getCurrentDownloadDirectoryPath;
import static com.zalo.trainingmenu.util.Util.generateTitle;

public final class DownloadItem implements Parcelable {
    private final String mUrl;

    private final String mFileTitle;
    private final String mDirectoryPath;

    private final boolean mIsAutoGeneratedTitle;
    private final boolean mIsAutoGeneratedPath;

    public boolean isAutogeneratedTitle() {
        return mIsAutoGeneratedTitle;
    }

    public boolean isAutogeneratedPath() {
        return mIsAutoGeneratedPath;
    }

    public DownloadItem(String url, String title, String directoryPath) {
        this.mUrl = url;

        if(directoryPath!=null && !directoryPath.isEmpty()) {
            this.mDirectoryPath = directoryPath;
            this.mIsAutoGeneratedPath = false;
        } else {
            this.mIsAutoGeneratedPath = true;
            this.mDirectoryPath = getCurrentDownloadDirectoryPath();
        }

        if(title!=null && !title.isEmpty()) {
            this.mIsAutoGeneratedTitle = false;
            this.mFileTitle = title;
        } else {
            this.mIsAutoGeneratedTitle = true;
            this.mFileTitle = Util.generateTitle(mUrl,mDirectoryPath);
        }
    }

    public DownloadItem(String url) {
        this.mUrl = url;
        this.mIsAutoGeneratedTitle = true;
        this.mIsAutoGeneratedPath = true;

        this.mDirectoryPath = Util.getCurrentDownloadDirectoryPath();
        this.mFileTitle = Util.generateTitle(mUrl,mDirectoryPath);
    }

    public DownloadItem(String url, String title) {
        this.mUrl = url;

        this.mIsAutoGeneratedPath = true;
        this.mDirectoryPath = getCurrentDownloadDirectoryPath();

        if(title!=null && !title.isEmpty()) {
            this.mFileTitle = title;
            this.mIsAutoGeneratedTitle = false;
        } else {
            this.mFileTitle = generateTitle(url,mDirectoryPath);
            this.mIsAutoGeneratedTitle = true;
        }
    }

    public String getUrlString() {
        return mUrl;
    }

    public String getFileTitle() {
        return mFileTitle;
    }


    public DownloadItem(DownloadItem item) {
        this.mUrl = item.mUrl;
        this.mFileTitle = item.mFileTitle;
        this.mDirectoryPath = item.mDirectoryPath;
        this.mIsAutoGeneratedTitle = item.mIsAutoGeneratedTitle;
        this.mIsAutoGeneratedPath = item.mIsAutoGeneratedPath;
    }

    public String getDirectoryPath() {
        return mDirectoryPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mUrl);
        dest.writeString(this.mFileTitle);
        dest.writeString(this.mDirectoryPath);
        dest.writeByte(this.mIsAutoGeneratedTitle ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mIsAutoGeneratedPath ? (byte) 1 : (byte) 0);
    }

    protected DownloadItem(Parcel in) {
        this.mUrl = in.readString();
        this.mFileTitle = in.readString();
        this.mDirectoryPath = in.readString();
        this.mIsAutoGeneratedTitle = in.readByte() != 0;
        this.mIsAutoGeneratedPath = in.readByte() != 0;
    }

    public static final Parcelable.Creator<DownloadItem> CREATOR = new Parcelable.Creator<DownloadItem>() {
        @Override
        public DownloadItem createFromParcel(Parcel source) {
            return new DownloadItem(source);
        }

        @Override
        public DownloadItem[] newArray(int size) {
            return new DownloadItem[size];
        }
    };
}
