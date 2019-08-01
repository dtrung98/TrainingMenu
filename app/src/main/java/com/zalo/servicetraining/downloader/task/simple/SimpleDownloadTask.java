package com.zalo.servicetraining.downloader.task.simple;

import android.os.Build;
import android.util.Log;
import android.webkit.URLUtil;

import com.zalo.servicetraining.downloader.base.BaseTask;
import com.zalo.servicetraining.downloader.model.DownloadItem;

import java.io.Closeable;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class SimpleDownloadTask extends BaseTask<SimpleTaskManager> {
    private static final String TAG = "SimpleDownloadTask";

    public SimpleDownloadTask(final int id, SimpleTaskManager manager, DownloadItem item) {
        super(id, manager, item);
    }

    @Override
    public void runTask() {
        downloadFile();
    }

    private void downloadFile() {
        if(isStopByUser()) return;
        setState(CONNECTING);
        notifyTaskChanged();

        File filePath = new File(getDirectory());
        File fileToWrite = new File(filePath,getFileTitle());

        RandomAccessFile fileWriter;

        try {
            fileWriter =new RandomAccessFile(fileToWrite, "rw");

        } catch (Exception e) {
            fileWriter = null;
            Log.d(TAG, "can't create fileWriter");
        }

        if(fileWriter==null) {
            setState(FAILURE_TERMINATED,"Could n't not create file to write");
            notifyTaskChanged();
            return;
        }

        switch (getMode()) {
            case EXECUTE_MODE_NEW_DOWNLOAD:
            case EXECUTE_MODE_RESTART:
                setDownloadedInBytes(0);
                try {
                    fileWriter.seek(0);
                } catch (IOException e) {

                    setState(FAILURE_TERMINATED, "Can n't override file");
                    notifyTaskChanged();
                    closeFileWriter(fileWriter);
                    return;
                }

                connectThenDownload(fileWriter);
                break;

            case EXECUTE_MODE_RESUME:
                long downloaded = getDownloadedInBytes();
                long fileSize = -1;
                try {
                    fileSize = fileWriter.length();
                } catch (IOException ignored) {}

                Log.d(TAG, "resuming with downloaded "+ downloaded+", fileSize "+ fileSize);

                if(fileSize<downloaded) {
                    closeFileWriter(fileWriter);
                    setState(FAILURE_TERMINATED, "File had been modified");
                    notifyTaskChanged();
                    return;
                }

                try {
                    fileWriter.seek(downloaded);
                } catch (IOException e) {
                    setState(FAILURE_TERMINATED,"File is broken, can not be resumed");
                    notifyTaskChanged();
                    closeFileWriter(fileWriter);
                    return;
                }

                connectThenDownload(fileWriter);
                break;
        }
    }


    private void closeFileWriter(RandomAccessFile fileWriter) {
        if(fileWriter!=null)
        try {
            fileWriter.close();
        } catch (IOException ignored) {}
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


    private void connectThenDownload(DataOutput fileWriter) {

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

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            if(URLUtil.isHttpsUrl(getURLString()))
                urlConnection = (HttpsURLConnection)url.openConnection();
            else urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setReadTimeout(40000);


            if(getMode()==EXECUTE_MODE_RESUME) {
                urlConnection.setRequestProperty(RANGE_PROPERTY,"bytes=" + getDownloadedInBytes() +'-');

                urlConnection.connect();
                if(urlConnection.getResponseCode()/100 !=2) {
                    setState(FAILURE_TERMINATED,"Response code is invalid");
                    notifyTaskChanged();
                    return;
                }
                String rangeFields = urlConnection.getHeaderField("Content-Range");
                if(rangeFields== null) {
                    urlConnection.disconnect();
                    setState(FAILURE_TERMINATED,"Server does not accept single part download");
                    return;
                }
                String[] connectionRanges = rangeFields.substring("bytes=".length()).split("-");
                long downloadedSize = Long.valueOf(connectionRanges[0]);
                if(downloadedSize > getDownloadedInBytes()) {
                    urlConnection.disconnect();
                    setState(FAILURE_TERMINATED,"Could n't resume because server does not reply true value");
                    notifyTaskChanged();
                    return;
                }

                Log.d(TAG, "can resume with downloadedSize "+downloadedSize+", downloadedInByte "+getDownloadedInBytes());
            } else {
                urlConnection.connect();
                if(urlConnection.getResponseCode()/100 !=2) {
                    setState(FAILURE_TERMINATED,"Response code is invalid");
                    notifyTaskChanged();
                    return;
                }
            }


            inputStream = urlConnection.getInputStream();
            //  inputStream  = new BufferedInputStream(url.openStream(),8192);

        } catch (Exception e) {
            if (urlConnection != null) urlConnection.disconnect();
            Log.d(TAG, "exception: "+e.getMessage());
        }

        if(urlConnection==null||inputStream==null) {
            setState(FAILURE_TERMINATED, "Failed to establish the connection to server");
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

        byte[] buffer = new byte[1024*4];

        long totalReadLength = 0;
        int bufferReadLength = 0;


        /*
            Kiểm tra action dismiss từ người dùng
         */

        if(isStopByUser()) {
            releaseConnection(urlConnection,inputStream,fileWriter);
            return;
        }


        /*
            Chuyển trạng thái sang đang chạy
         */
        initSpeed();
        setState(RUNNING);
        notifyTaskChanged();
        Log.d(TAG, "running");

        try {
            while ((bufferReadLength = inputStream.read(buffer)) != -1) {
                fileWriter.write(buffer, 0, bufferReadLength);
                totalReadLength += bufferReadLength;
                appendDownloadedBytes(bufferReadLength);
                calculateSpeed();
                Log.d(TAG, "downloadedInBytes " +getDownloadedInBytes());
                /*
                    Kiểm tra action dismiss từ người dùng
                 */

                if(isStopByUser()) {
                    releaseConnection(urlConnection,inputStream, fileWriter);
                    return;
                }
            }
            Log.d(TAG, "Success with downloaded "+getDownloadedInBytes());

            if(fileWriter instanceof RandomAccessFile) {
                ((RandomAccessFile)fileWriter).setLength(getDownloadedInBytes());
            }

            setState(SUCCESS);
            notifyTaskChanged();
        } catch (Exception ignored) {
            setState(FAILURE_TERMINATED, "Error throw");
            notifyTaskChanged();
        } finally {
            releaseConnection(urlConnection, inputStream,fileWriter);
        }

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

}
