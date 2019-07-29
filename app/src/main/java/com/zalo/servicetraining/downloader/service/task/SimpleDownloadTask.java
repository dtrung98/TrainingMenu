package com.zalo.servicetraining.downloader.service.task;

import android.os.Build;
import android.util.Log;
import android.webkit.URLUtil;

import com.zalo.servicetraining.downloader.base.AbsTask;
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

public class SimpleDownloadTask extends AbsTask<SimpleTaskManager> {
    private static final String TAG = "SimpleDownloadTask";
    private static final String RANGE_PROPERTY = "Range";

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
        downloadFileUsingFileChannel();
    }

    private void downloadFileUsingFileChannel() {
        if(!shouldContinueRunning()) return;
        setState(CONNECTING);
        notifyTaskChanged();

        File filePath = new File(mDownloadItem.getDirectoryPath());
        File fileToWrite = new File(filePath,mDownloadItem.getTitle());

        RandomAccessFile fileWriter;

        try {
            fileWriter =new RandomAccessFile(fileToWrite, "rw");

        } catch (Exception e) {
            fileWriter = null;
            Log.d(TAG, "can't create fileWriter");
        }

        if(fileWriter==null) {
            setState(FAILURE_TERMINATED,"can't not create file to write");
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

                    setState(FAILURE_TERMINATED, "Can't not seek to zero");
                    notifyTaskChanged();
                    closeFileWriter(fileWriter);
                    return;
                }

                connectAndDownload(fileWriter);
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
                    setState(FAILURE_TERMINATED, "File is invalid");
                    notifyTaskChanged();
                    return;
                }

                try {
                    fileWriter.seek(downloaded);
                } catch (IOException e) {
                    setState(FAILURE_TERMINATED,"Can't not seek to " +downloaded);
                    notifyTaskChanged();
                    closeFileWriter(fileWriter);
                    return;
                }

                connectAndDownload(fileWriter);
                break;
        }
    }


    private void closeFileWriter(RandomAccessFile fileWriter) {
        if(fileWriter!=null)
        try {
            fileWriter.close();
        } catch (IOException ignored) {}
    }

/*    private void downloadFileTask() {

        if(!shouldContinueRunning()) return;
        setState(CONNECTING);
        notifyTaskChanged();
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

            connectAndDownload(fileWriter);


        } else {
            setState(FAILURE_TERMINATED,"fail to create output file \""+ fileToWrite.toString());
            notifyTaskChanged();
        }

    }*/

    public long getFileSize(URL url) {
        long size = -1;
        HttpURLConnection conn = null;
        try {
            if(URLUtil.isHttpsUrl(mDownloadItem.getUrlString()))
                conn = (HttpsURLConnection)url.openConnection();
            else conn = (HttpURLConnection)url.openConnection();
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


    private void connectAndDownload(DataOutput fileWriter) {

        /*
            Tạo kết nối
        */

        URL url;
        try {
            url = new URL(mDownloadItem.getUrlString());
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
            if(URLUtil.isHttpsUrl(mDownloadItem.getUrlString()))
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
                    setState(FAILURE_TERMINATED,"not accept single part download");
                    return;
                }
                String[] connectionRanges = rangeFields.substring("bytes=".length()).split("-");
                long downloadedSize = Long.valueOf(connectionRanges[0]);
                if(downloadedSize > getDownloadedInBytes()) {
                    urlConnection.disconnect();
                    setState(FAILURE_TERMINATED,"downloadedSize is invalid");
                    notifyTaskChanged();
                    return;
                }

                Log.d(TAG, "can resume with downloadedSize "+downloadedSize+", downloadedInByte "+getDownloadedInBytes());
                setDownloadedInBytes(downloadedSize);
            } else urlConnection.connect();


            inputStream = urlConnection.getInputStream();
            //  inputStream  = new BufferedInputStream(url.openStream(),8192);

        } catch (Exception e) {
            if (urlConnection != null) urlConnection.disconnect();
            Log.d(TAG, "exception: "+e.getMessage());
        }

        if(urlConnection==null||inputStream==null) {
            setState(FAILURE_TERMINATED, "Failure to establish connection, urlConnection is "+ urlConnection+", inputStream is "+ inputStream);
            notifyTaskChanged();
            return;
        }

        /*
            Bắt đầu tải và ghi file
        */

        long fileSize = getFileSize(url);
        setProgressSupport(fileSize>0);
        if(fileSize<=0) fileSize = -1;
        setFileContentLength(fileSize);
        Log.d(TAG, "start download file size = "+ fileSize);

        byte[] buffer = new byte[1024*4];

        long fileReadLength = 0;
        int bufferReadLength = 0;


        /*
            Kiểm tra action dismiss từ người dùng
         */

        if(!shouldContinueRunning()) {
            releaseConnection(urlConnection,inputStream,fileWriter);
            return;
        }


        /*
            Chuyển trạng thái sang đang chạy
         */

        setState(RUNNING);
        notifyTaskChanged();
        Log.d(TAG, "running");


        try {
            while ((bufferReadLength = inputStream.read(buffer)) != -1) {
                fileWriter.write(buffer, 0, bufferReadLength);
                fileReadLength += bufferReadLength;
                setDownloadedInBytes(getDownloadedInBytes()+bufferReadLength);
                Log.d(TAG, "downloadedInBytes " +getDownloadedInBytes());
                /*
                    Kiểm tra action dismiss từ người dùng
                 */

                if(!shouldContinueRunning()) {
                    releaseConnection(urlConnection,inputStream, fileWriter);
                    return;
                }

                /*
                    Thông báo tiến trình
                 */
                if(isProgressSupport()) {

                    setProgressAndNotify((getDownloadedInBytes() + 0f) / getFileContentLength());
                }
                else notifyTaskChanged();
            }
            Log.d(TAG, "success with downloaded "+getDownloadedInBytes());
            setState(SUCCESS);
            notifyTaskChanged();
        } catch (Exception ignored) {
            setState(FAILURE_TERMINATED, "Error throw");
            notifyTaskChanged();
        } finally {
            releaseConnection(urlConnection, inputStream,fileWriter);
        }

    }


   /* private void connectAndDownload(FileOutputStream fileWriter) {


            Tạo kết nối


        URL url;
        try {
           url = new URL(mDownloadItem.getUrlString());
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
            if(URLUtil.isHttpsUrl(mDownloadItem.getUrlString()))
            urlConnection = (HttpsURLConnection)url.openConnection();
            else urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setReadTimeout(40000);
            urlConnection.connect();

            inputStream = urlConnection.getInputStream();
          //  inputStream  = new BufferedInputStream(url.openStream(),8192);

        } catch (Exception e) {
            if (urlConnection != null) urlConnection.disconnect();
            Log.d(TAG, "exception: "+e.getMessage());
        }

        if(urlConnection==null||inputStream==null) {
            setState(FAILURE_TERMINATED, "Failure to establish connection, urlConnection is "+ urlConnection+", inputStream is "+ inputStream);
            notifyTaskChanged();
            return;
        }




            Bắt đầu tải và ghi file


        long fileSize = getFileSize(url);
        if(fileSize>0) setProgressSupport(true);
        setFileContentLength(fileSize);
        Log.d(TAG, "start download file size = "+ fileSize);

        byte[] buffer = new byte[1024*4];

        long fileReadLength = 0;
        int bufferReadLength = 0;



            Kiểm tra action dismiss từ người dùng


        if(!shouldContinueRunning()) {
            releaseConnection(urlConnection,inputStream,fileWriter);
            return;
        }



            Chuyển trạng thái sang đang chạy


        setState(RUNNING);
        notifyTaskChanged();


        try {
            while ((bufferReadLength = inputStream.read(buffer)) != -1) {
                fileWriter.write(buffer, 0, bufferReadLength);
                fileReadLength += bufferReadLength;


                    Kiểm tra action dismiss từ người dùng


                if(!shouldContinueRunning()) {
                   releaseConnection(urlConnection,inputStream, fileWriter);
                    return;
                }


                    Thông báo tiến trình

                if(isProgressSupport()) {

                    setProgressAndNotify((fileReadLength + 0f) / fileSize);
                }
                else notifyTaskChanged();
            }

            setState(SUCCESS);
            notifyTaskChanged();
        } catch (Exception ignored) {
            setState(FAILURE_TERMINATED, "Error throw");
            notifyTaskChanged();
        } finally {
            urlConnection.disconnect();

            try {
                inputStream.close();
            } catch (IOException ignored) {}

            try {
                fileWriter.close();
            } catch (IOException ignored) {}
        }


    }*/

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
