package com.zalo.trainingmenu.newsfeed3d.model;

import com.ldt.parallaximageview.model.ParallaxImageObject;

public class NewsFeedObject {
    public String getContentText() {
        return mContentText;
    }

    public NewsFeedObject setContentText(String contentText) {
        mContentText = contentText;
        return this;
    }

    public ParallaxImageObject getImageObject() {
        return mImageObject;
    }

    public NewsFeedObject setImageObject(ParallaxImageObject imageObject) {
        mImageObject = imageObject;
        return this;
    }

    private String mContentText = "";
    private ParallaxImageObject mImageObject;
}
