package com.ldt.parallaximageview.model;

import androidx.annotation.NonNull;

public final class ParallaxImageObject {
    private final Object mOriginal;

    public Object getOriginal() {
        return mOriginal;
    }

    public Object getDepth() {
        return mDepth;
    }

    private final Object mDepth;

    public ParallaxImageObject(@NonNull Object original, @NonNull Object depth) {
        mOriginal = original;
        mDepth = depth;
    }
}
