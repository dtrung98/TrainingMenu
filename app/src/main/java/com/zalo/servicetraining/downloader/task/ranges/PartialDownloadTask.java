package com.zalo.servicetraining.downloader.task.ranges;

import android.util.Log;
import android.webkit.URLUtil;

import com.zalo.servicetraining.downloader.base.BaseTask;

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

import static com.zalo.servicetraining.downloader.base.BaseTask.FAILURE_TERMINATED;
import static com.zalo.servicetraining.downloader.base.BaseTask.RUNNING;
import static com.zalo.servicetraining.downloader.task.simple.SimpleDownloadTask.RANGE_PROPERTY;

public class PartialDownloadTask implements Runnable {


    private static int sIdCounting =0;
    private synchronized static int getNextId() {
        int current = sIdCounting;
        sIdCounting++;

        return current;
    }
    public static final String TAG = "PartialDownloadTask";

    private final FileDownloadTask mTask;
    
    private final long mStartByte;

    public long getStartByte() {
        return mStartByte;
    }

    public long getEndByte() {
        return mEndByte;
    }

    private final long mEndByte;
    private final long mDownloadLength;
    private long mDownloadedInBytes = 0;

    private final int mId;

    private int mState = BaseTask.PENDING;

    protected Thread mThread;

    private boolean isPartialDownloadTask() {
        return mDownloadLength!=-1;
    }

    public PartialDownloadTask restoreInstance(FileDownloadTask fileTask, final int id, long startByte, long endByte, int state, long downloadedInBytes) {
    PartialDownloadTask task = new PartialDownloadTask(fileTask,id,startByte,endByte);
    task.mState = state;
    task.mDownloadedInBytes = downloadedInBytes;
    return task;

    }

    PartialDownloadTask(FileDownloadTask fileTask, final int id) {
        mStartByte = 0;
        mEndByte = -1;
        mTask = fileTask;
        mId = id;
        mDownloadLength = -1;
        mDownloadedInBytes = 0;
    }

    PartialDownloadTask(FileDownloadTask fileTask, final int id, long startByte, long endByte) {
        mStartByte = startByte;
        mEndByte = endByte;
        mTask = fileTask;
        mId = id;
        mDownloadLength = mEndByte - mStartByte;
    }

    @Override
    public void run() {
        download();
        release();
    }

    public final void execute() {
        mThread = new Thread(this);
        mThread.start();

    }

    public final void waitMeFinish() {
        Log.d(TAG, "Thread id "+ Thread.currentThread().getId()+" is waiting for thread "+mThread.getId()+" to finish");
        try {
            mThread.join();
        } catch (InterruptedException e) {
            Log.d(TAG, "could not join thread :"+e.getMessage());
        }
    }


    private void release() {

    }

    private void download() {
        Log.d(TAG, "thread id "+mThread.getId()+" or "+ Thread.currentThread().getId()+" is start downloading from "+ mStartByte+" to "+mEndByte);

        if(mTask.isStopByUser()) return;

        // Partial Download does not have connecting state
        // so remove it
        //
        // setState(CONNECTING);
        // notifyTaskChanged();
        //

        File filePath = new File(mTask.getDirectory());
        File fileToWrite = new File(filePath,mTask.getFileTitle());

        RandomAccessFile fileWriter;

        try {
            fileWriter =new RandomAccessFile(fileToWrite, "rw");

        } catch (Exception e) {
            fileWriter = null;
            Log.d(TAG, "can't create new file writer");
        }

        if(fileWriter==null) {
            setState(FAILURE_TERMINATED,"Could n't not create file to write");
            notifyTaskChanged();
            return;
        }

        switch (mTask.getMode()) {
            case BaseTask.EXECUTE_MODE_NEW_DOWNLOAD:
            case BaseTask.EXECUTE_MODE_RESTART:
                setDownloadedInBytes(0);
                try {
                    fileWriter.seek(mStartByte);
                } catch (IOException e) {

                    setState(FAILURE_TERMINATED, "Can not override file");
                    notifyTaskChanged();
                    closeFileWriter(fileWriter);
                    return;
                }

                connectThenDownload(fileWriter);
                break;

            case BaseTask.EXECUTE_MODE_RESUME:
                long downloaded = getDownloadedInBytes();
                long fileSize = -1;

                try {
                    fileSize = fileWriter.length();
                } catch (IOException ignored) {}

                Log.d(TAG, "resuming with downloaded "+ downloaded+", fileSize "+ fileSize);

                if(fileSize<getRealPositionInFile(downloaded)) {
                    closeFileWriter(fileWriter);
                    setState(FAILURE_TERMINATED, "File had been modified");
                    notifyTaskChanged();
                    return;
                }

                try {
                    fileWriter.seek(getRealPositionInFile(downloaded));
                } catch (IOException e) {
                    setState(FAILURE_TERMINATED,"File is broken, couldn't resume");
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


    private void connectThenDownload(DataOutput fileWriter) {

        /*
            Tạo kết nối
        */

        URL url;
        try {
            url = new URL(mTask.getURLString());
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
            if(URLUtil.isHttpsUrl(mTask.getURLString()))
                urlConnection = (HttpsURLConnection)url.openConnection();
            else urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setReadTimeout(40000);


            if(isPartialDownloadTask()) {
                urlConnection.setRequestProperty(RANGE_PROPERTY, "bytes=" + getRealPositionInFile(getDownloadedInBytes()) + '-' + mEndByte);
                Log.d(TAG, "partial task id "+ getId()+" is request range from "+ getRealPositionInFile(getDownloadedInBytes())+" - "+ mEndByte);

                urlConnection.connect();
                Log.d(TAG, "partial task id "+getId()+" connected");
                if (urlConnection.getResponseCode() / 100 != 2) {
                    setState(FAILURE_TERMINATED, "Response code is invalid");
                    notifyTaskChanged();
                    return;
                } else Log.d(TAG, "partial task id "+ getId()+" receives response code "+urlConnection.getResponseCode());

                String rangeFields = urlConnection.getHeaderField("Content-Range");
                if (rangeFields == null) {
                    urlConnection.disconnect();
                    setState(FAILURE_TERMINATED, "Server does not accept single part download");
                    return;
                }
                Log.d(TAG, "partial task id "+getId()+" receives Content-Range : "+ rangeFields);

                String[] connectionRanges = rangeFields.substring("bytes=".length()).split("[-/]");
                long positionDownloadFromByServer = Long.valueOf(connectionRanges[0]);
                long positionDownloadToByServer = Long.valueOf(connectionRanges[1]);
                Log.d(TAG, "partial task id "+getId()+"server reply from "+positionDownloadFromByServer+" to " + positionDownloadToByServer );
                if (positionDownloadFromByServer > getRealPositionInFile(getDownloadedInBytes()) || positionDownloadToByServer != mEndByte) {
                    urlConnection.disconnect();
                    setState(FAILURE_TERMINATED, "Could not resume or download partially because server replied wrong values");
                    notifyTaskChanged();
                    return;
                }

                Log.d(TAG, "can resume or download partially with downloadedSize " + positionDownloadFromByServer + ", downloadedInByte " + getDownloadedInBytes());

            } else {
                setDownloadedInBytes(0);
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

        byte[] buffer = new byte[1024*4];

        long totalReadLength = 0;
        int bufferReadLength = 0;


        /*
            Kiểm tra action dismiss từ người dùng
         */

        if(mTask.isStopByUser()) {
            releaseConnection(urlConnection,inputStream,fileWriter);
            return;
        }


        /*
            Chuyển trạng thái sang đang chạy
         */


        /*
            Không cần khởi tạo speed
            vì đã khởi tạo trước đó
         */

        //  mTask.initSpeed();


        setState(RUNNING);
        notifyTaskChanged();
        Log.d(TAG, "thread id "+mThread.getId()+" with task id"+getId()+" is going to running state");

        try {
            while ((bufferReadLength = inputStream.read(buffer)) != -1) {
                fileWriter.write(buffer, 0, bufferReadLength);
                totalReadLength += bufferReadLength;
                appendDownloadedBytes(bufferReadLength);
                mTask.calculateSpeed();
                Log.d(TAG, "partial id "+getId()+" with downloadedInBytes " +getDownloadedInBytes());
                /*
                    Kiểm tra action dismiss từ người dùng
                 */

                if(mTask.isStopByUser()) {
                    releaseConnection(urlConnection,inputStream, fileWriter);
                    return;
                }
            }
            Log.d(TAG, "Success with downloaded "+getDownloadedInBytes());


            setState(BaseTask.SUCCESS);
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

    public int getState() {
        return mState;
    }

    private void setState(int state) {
        mState = state;
    }
    private synchronized void setState(int state, String message) {
        this.mState = state;
        setMessage(message);
        Log.d(TAG, "partial id "+getId()+" set state with message: "+ message);
    }

    private String mMessage = "";

    public synchronized String getMessage() {
        return mMessage;
    }

    private synchronized void setMessage(String message) {
        mMessage = message;
    }

    public synchronized long getRealPositionInFile(long positionInRanges) {
        return mStartByte + positionInRanges;
    }

    public synchronized long getDownloadedInBytes() {
        return mDownloadedInBytes;
    }



    private void notifyTaskChanged() {
        if(mTask!=null)
            mTask.notifyPartialTaskChanged(mId);
    }

    private synchronized void appendDownloadedBytes(long bytes) {
        if(bytes>0) {
            mDownloadedInBytes += bytes;
            mTask.appendDownloadedBytes(bytes);
            Log.d(TAG, "partial task id "+getId()+" is append with progress "+((int)(100*getDownloadedInBytes()/getDownloadLength())));
        }

    }

    private synchronized void setDownloadedInBytes(long downloadedInBytes) {
        mDownloadedInBytes = downloadedInBytes;
    }

    public int getId() {
        return mId;
    }

    public long getDownloadLength() {
        return mDownloadLength;
    }
}
