package com.zalo.servicetraining.imageloader;

import android.widget.ImageView;

public class ImageRequest {
    private final ImageView mImageView;
    private final String mUrl;
    private ImageLoaderTask mTask;

    ImageRequest(ImageView imageView, String url) {
        mImageView = imageView;
        mUrl = url;
    }

    public ImageView getImageView() {
        return mImageView;
    }

    public String getUrl() {
        return mUrl;
    }

    public void cancel() {
        if(mTask!=null) mTask.cancel(true);
        mTask = null;
    }

    public void load() {
        cancel();
        mTask = new ImageLoaderTask(this);
        mTask.execute();
    }

}
