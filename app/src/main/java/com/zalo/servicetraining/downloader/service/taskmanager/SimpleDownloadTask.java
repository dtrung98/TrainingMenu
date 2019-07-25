package com.zalo.servicetraining.downloader.service.taskmanager;

import android.os.Build;
import android.util.Log;

import com.zalo.servicetraining.downloader.base.AbsTask;
import com.zalo.servicetraining.downloader.model.DownloadItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SimpleDownloadTask extends AbsTask<SimpleTaskManager> {
    private static final String TAG = "SimpleDownloadTask";

    private static final int MODE_DOWNLOAD_FROM_START = 5;
    private static final int MODE_DOWNLOAD_TRY_TO_APPEND = 6;

    private static final int MODE_CONTINUE_RESTART = 7;
    private static final int MODE_CONTINUE_RESUME = 8;

    private int mMode = MODE_DOWNLOAD_FROM_START;

    public DownloadItem getDownloadItem() {
        return mDownloadItem;
    }

    private final DownloadItem mDownloadItem;

    public SimpleDownloadTask(final int id, SimpleTaskManager manager, DownloadItem item) {
        super(id, manager);
        mDownloadItem = item;
    }

    @Override
    public void run() {
        super.run();
        downloadFileTask();
    }

    private void longRunningTask() {
        setState(RUNNING);
        notifyProgressChanged();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 1; i <= 400; i++) {
            try {
                Thread.sleep(10);
                Log.d(TAG, "task "+getId()+" run: sleep "+i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setProgress(i/400f);
            notifyProgressChanged();
        }
        Log.d(TAG, "run: finish");
    }

    private void downloadFileTask() {
        setState(PENDING);
        notifyProgressChanged();
        File filePath = new File(mDownloadItem.getDirectoryPath());
        File fileToWrite = new File(filePath,mDownloadItem.getTitle());

        FileOutputStream fileWriter;
        try {

            fileWriter = new FileOutputStream(fileToWrite,mDownloadItem.isAppendIfExist());
        } catch (FileNotFoundException e) {
            fileWriter = null;
            Log.d(TAG, "can't create fileWriter");
        }

        if(fileWriter!=null) {
            setState(RUNNING);
            notifyProgressChanged();

            startDownload(fileWriter);


        } else {
            setState(FAILURE_TERMINATED,"fail to create output file \""+ fileToWrite.toString());
            notifyProgressChanged();
        }

    }

    public long getFileSize(URL url) {
        long size = -1;
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                size = conn.getContentLengthLong();
            } else {

                String sizeString = conn.getHeaderField("content-length");
                if (!sizeString.isEmpty())
                try {
                    size = Long.parseLong(sizeString);
                } catch (NumberFormatException ignored) {};
            }

        } catch (IOException ignored) {} finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return size;
    }

    private void startDownload(FileOutputStream fileWriter) {
        URL url;
        try {
           url = new URL(mDownloadItem.getUrlString());
        } catch (MalformedURLException e) {
            url = null;
        }

        if(url==null) {
            setState(FAILURE_TERMINATED, "URL is invalid");
            notifyProgressChanged();
            return;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            //urlConnection.setDoOutput(true);
           // urlConnection.setReadTimeout(10000);
           // urlConnection.setConnectTimeout(15000);
            urlConnection.connect();
            // if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
          //  }

        } catch (Exception e) {
            if (urlConnection != null) urlConnection.disconnect();
        }

        if(urlConnection==null||inputStream==null) {
            setState(FAILURE_TERMINATED, "Fail to establish connection, urlConnection is "+ urlConnection+", inputStream is "+ inputStream);
            notifyProgressChanged();
            return;
        }

        long fileSize = getFileSize(url);
        if(fileSize>0) setProgressSupport(true);
        Log.d(TAG, "start download file size = "+ fileSize);

        byte[] buffer = new byte[1024*4];

        long fileReadLength = 0;
        int bufferReadLength = 0;

        try {
            while ((bufferReadLength = inputStream.read(buffer)) != -1) {
                fileWriter.write(buffer, 0, bufferReadLength);
                fileReadLength += bufferReadLength;
                if(isProgressSupport()) {

                    setProgressAndNotify((fileReadLength + 0f) / fileSize);
                }
                else notifyProgressChanged();
            }

            setState(SUCCESS);
            notifyProgressChanged();
        } catch (IOException ignored) {
            setState(FAILURE_TERMINATED, "Error throw");
            notifyProgressChanged();
        } finally {
            urlConnection.disconnect();

            try {
                inputStream.close();
            } catch (IOException ignored) {}

            try {
                fileWriter.close();
            } catch (IOException ignored) {}
        }


    }

}
