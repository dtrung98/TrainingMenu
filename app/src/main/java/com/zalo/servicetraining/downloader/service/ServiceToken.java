package com.zalo.servicetraining.downloader.service;

import android.content.ContextWrapper;

public final class ServiceToken {
        public ContextWrapper mWrappedContext;

        public ServiceToken(final ContextWrapper context) {
            mWrappedContext = context;
        }
    }