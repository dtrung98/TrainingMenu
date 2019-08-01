package com.zalo.servicetraining.downloader.task.ranges;

import android.os.Build;
import android.util.Log;
import android.webkit.URLUtil;

import com.zalo.servicetraining.downloader.base.BaseTask;
import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.downloader.task.simple.SimpleTaskManager;

import java.io.Closeable;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class FileDownloadTask extends BaseTask<SimpleTaskManager> {
    private static final String TAG = "FileDownloadTask";

    public FileDownloadTask(final int id, SimpleTaskManager manager, DownloadItem item, int numberConnection) {
        super(id, manager, item);
        mMaxConnectionNumber = numberConnection;
    }

    private final int mMaxConnectionNumber;
    private final ArrayList<PartialDownloadTask> mPartialDownloadTask = new ArrayList<>();

    @Override
    public void runTask() {
        downloadFile();
    }

    private void downloadFile() {
        if(isStopByUser()) return;
        setState(CONNECTING);
        notifyTaskChanged();

        switch (getMode()) {
            case EXECUTE_MODE_NEW_DOWNLOAD:
            case EXECUTE_MODE_RESTART:
                setDownloadedInBytes(0);
            case EXECUTE_MODE_RESUME:
                connectThenDownload();
                break;
        }
    }

    public static long getFileSize(URL url) {
        long size = -1;
        HttpURLConnection conn = null;
        try {
            if(URLUtil.isHttpsUrl(url.toString()))
                conn = (HttpsURLConnection)url.openConnection();
            else conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("HEAD");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                size = conn.getContentLengthLong();
            } else {

                String sizeString = conn.getHeaderField(CONTENT_LENGTH);
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


    private void connectThenDownload() {

        /*
            Tạo kết nối
        */

        URL url;
        try {
            url = new URL(getURLString());
        } catch (MalformedURLException e) {
            url = null;
        }

        if(url==null) {
            setState(FAILURE_TERMINATED, "URL is null");
            notifyTaskChanged();
            return;
        }

        /*
            Bắt đầu tải và ghi file
        */

        long fileSize = getFileSize(url);
        if(fileSize<=0) fileSize = -1;
        setFileContentLength(fileSize);
        Log.d(TAG, "start download file size = "+ fileSize);

        /*
            Kiểm tra action dismiss từ người dùng
         */

        if(isStopByUser()) {
            return;
        }


        /*
            Chuyển trạng thái sang đang chạy
         */
        initSpeed();
        setState(RUNNING);
        notifyTaskChanged();
        Log.d(TAG, "FileTask id"+getId()+" is running");

        // create new Partial Download Task
        // only the first running
        if(mPartialDownloadTask.isEmpty()) {
            mPartialDownloadTask.clear();
            if(!isProgressSupport()) {
                mPartialDownloadTask.add(new PartialDownloadTask(this,getId()));
                Log.d(TAG, "file task id "+getId()+" does not support progress");
            } else {
                long usualPartSize = fileSize / mMaxConnectionNumber;

                long startByte ;
                long endByte;

                for (int i = 0; i < mMaxConnectionNumber; i++) {
                    startByte = usualPartSize*i;
                    endByte = (i!=mMaxConnectionNumber-1) ? (i+1)*usualPartSize - 1 : fileSize - 1;

                    Log.d(TAG, "file task id "+ getId()+" is create new partial task "+i+" to download from "+ startByte+" to "+ endByte);
                    PartialDownloadTask partialTask = new PartialDownloadTask(this,  i + 1,startByte,endByte );
                    mPartialDownloadTask.add(partialTask);
                }
            }
        }

        Log.d(TAG, "executing "+mPartialDownloadTask.size()+" partial download task");

        for (int i = 0; i < mPartialDownloadTask.size(); i++) {
            mPartialDownloadTask.get(i).execute();
        }

        for (int i = 0; i < mPartialDownloadTask.size(); i++) {
            mPartialDownloadTask.get(i).waitMeFinish();
        }

        notifyTaskChanged();
        Log.d(TAG, "reach the end of file download task");
        Log.d(PartialDownloadTask.TAG, "reach the end of file download task");
    }

    private void releaseConnection(HttpURLConnection urlConnection, InputStream inputStream, DataOutput fileWriter) {
        if(fileWriter instanceof Closeable)
            releaseConnection(urlConnection,inputStream,(Closeable) fileWriter);
    }

    private void releaseConnection(HttpURLConnection urlConnection, InputStream inputStream, Closeable fileWriter) {
        if(urlConnection!=null) try {
            urlConnection.disconnect();
        } catch (Exception ignored) {}

        if(inputStream!=null)
            try {
                inputStream.close();
            } catch (Exception ignored) {}

        if(fileWriter!=null)
            try {
                fileWriter.close();
            } catch (IOException ignored) {}
    }

    public void notifyPartialTaskChanged(int id) {
        int position = id - 1;
        if(position<0 || position > mPartialDownloadTask.size() - 1) {
            Log.d(TAG, "notify id is not invalid");
            return;
        }
        PartialDownloadTask task = mPartialDownloadTask.get(position);
        switch (task.getState()) {
            case FAILURE_TERMINATED:
                Log.d(TAG,"partial task "+task.getId()+" is failure terminated");
                setState(BaseTask.FAILURE_TERMINATED);
                break;
            case SUCCESS:
                for (int i = 0; i < mPartialDownloadTask.size(); i++) {
                    if(mPartialDownloadTask.get(i).getState()!=BaseTask.SUCCESS) break;
                    if(i==mPartialDownloadTask.size()-1) {
                        setState(BaseTask.SUCCESS);
                    }
                }
        }
        StringBuilder progress = new StringBuilder();
        progress.append("log progress: ");
        for (int i = 0; i < mPartialDownloadTask.size(); i++) {
            progress.append(" task_").append(i + 1).append(" = ").append((int) (100*mPartialDownloadTask.get(i).getDownloadedInBytes() / mPartialDownloadTask.get(i).getDownloadLength()));
        }
        Log.d(TAG, progress.toString());
        notifyTaskChanged();
    }

    public int getMaxConnectionNumber() {
        return mMaxConnectionNumber;
    }
}
