package com.zalo.servicetraining.imageloader;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public class ImageLoaderTask extends AsyncTask<Void, Void, String> {
        private static final String TAG = "ImageLoaderTask";
        private final WeakReference<ImageRequest> mWeakReferenceImageRequest;
        private boolean mIsCancelled = false;

        public ImageLoaderTask(ImageRequest request) {
            mWeakReferenceImageRequest = new WeakReference<>(request);
        }

        public void cancel() {
            mIsCancelled = true;
            cancel(true);
            mWeakReferenceImageRequest.clear();
        }

        @Override
        protected String doInBackground(Void... voids) {
            return null;
        }
    }