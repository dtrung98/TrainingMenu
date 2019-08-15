package com.zalo.trainingmenu.imageloader;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public class ImageLoaderTask extends AsyncTask<Void, Void, String> {
        private static final String TAG = "ImageLoaderTask";
        private final WeakReference<ImageRequest> mWeakReferenceImageRequest;

    public ImageLoaderTask(ImageRequest request) {
            mWeakReferenceImageRequest = new WeakReference<>(request);
        }

        public void cancel() {
            boolean isCancelled = true;
            cancel(true);
            mWeakReferenceImageRequest.clear();
        }

        @Override
        protected String doInBackground(Void... voids) {
            return null;
        }
    }