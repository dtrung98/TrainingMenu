package com.zalo.trainingmenu.newsfeed3d.model;

public final class ParallaxImageObject {
    private final String mOriginal;

    public String getOriginal() {
        return mOriginal;
    }

    public String getDepth() {
        return mDepth;
    }

    private String mDepth;

    public ParallaxImageObject(String original, String depth) {
        mOriginal = original;
        mDepth = depth;
    }
}
