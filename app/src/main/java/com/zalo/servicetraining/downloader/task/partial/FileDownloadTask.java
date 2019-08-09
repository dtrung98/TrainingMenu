package com.zalo.servicetraining.downloader.task.partial;

import android.os.Build;
import android.util.Log;
import android.webkit.URLUtil;

import com.zalo.servicetraining.downloader.base.BaseTask;
import com.zalo.servicetraining.downloader.base.BaseTaskManager;
import com.zalo.servicetraining.downloader.database.DownloadDBHelper;
import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.downloader.model.PartialInfo;
import com.zalo.servicetraining.downloader.model.TaskInfo;

import java.io.Closeable;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class FileDownloadTask extends BaseTask<PartialTaskManager> {
    private static final String TAG = "FileDownloadTask";

    public FileDownloadTask(final int id, PartialTaskManager manager, DownloadItem item) {
        super(id, manager, item);
    }

    private FileDownloadTask(int id, PartialTaskManager taskManager, String directory, String url, long createdTime, String fileTitle) {
        super(id, taskManager, directory, url, createdTime, fileTitle);
    }

    public ArrayList<PartialDownloadTask> getPartialDownloadTasks() {
        return mPartialDownloadTasks;
    }

    private final ArrayList<PartialDownloadTask> mPartialDownloadTasks = new ArrayList<>();

    public static FileDownloadTask restoreInstance(PartialTaskManager taskManager, TaskInfo info) {
        FileDownloadTask task = new FileDownloadTask(info.getId(), taskManager,info.getDirectory(),info.getURLString(),info.getCreatedTime(),info.getFileTitle());

        int state = info.getState();
        if(state==BaseTask.RUNNING) state = PAUSED;
        task.setState(state);
        task.setDownloadedInBytes(info.getDownloadedInBytes());
        task.setFileContentLength(info.getFileContentLength());
        task.restoreProgress(info.getProgress());
        task.setMessage(info.getMessage());
        task.setFinishedTime(info.getFinishedTime());
        task.restoreFirstExecutedTime(info.getFirstExecutedTime());
        task.restoreLastExecutedTime(info.getLastExecutedTime());
        task.setRunningTime(info.getRunningTime());

        task.mPartialDownloadTasks.clear();
        List<PartialInfo> partialInfoList = info.getPartialInfoList();
        for (PartialInfo partialInfo:
        partialInfoList) {
            PartialDownloadTask partialTask = PartialDownloadTask.restoreInstance(task, partialInfo.getId(),partialInfo.getStartByte(),partialInfo.getEndByte(),partialInfo.getState(),partialInfo.getDownloadedInBytes());
            task.mPartialDownloadTasks.add(partialTask);
        }

        return task;
    }

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
                mPartialDownloadTasks.clear();
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
        if(mPartialDownloadTasks.isEmpty()) {
            mPartialDownloadTasks.clear();
            if(!isProgressSupport()) {
                mPartialDownloadTasks.add(new PartialDownloadTask(this, (int)DownloadDBHelper.getInstance().generateNewPartialTaskId(0,-1)));
                Log.d(TAG, "file task id "+getId()+" does not support progress");
            } else {
                int numberConnections = getConnectionNumber();
                long usualPartSize = fileSize / numberConnections;

                long startByte ;
                long endByte;

                for (int i = 0; i < numberConnections; i++) {
                    startByte = usualPartSize*i;
                    endByte = (i!= numberConnections -1) ? (i+1)*usualPartSize - 1 : fileSize - 1;

                    Log.d(TAG, "file task id "+ getId()+" is creating new partial task "+i+" to download with "+ startByte+" to "+ endByte);
                    PartialDownloadTask partialTask = new PartialDownloadTask(this,  (int)DownloadDBHelper.getInstance().generateNewPartialTaskId(startByte,endByte),startByte,endByte );
                    mPartialDownloadTasks.add(partialTask);
                }
            }
        }

        Log.d(TAG, "executing "+ mPartialDownloadTasks.size()+" partial download task");

        for (int i = 0; i < mPartialDownloadTasks.size(); i++) {
            mPartialDownloadTasks.get(i).execute();
        }

        for (int i = 0; i < mPartialDownloadTasks.size(); i++) {
            mPartialDownloadTasks.get(i).waitMeFinish();
        }

        // Check if state is running and all partial tasks were successful
        boolean success = true;
        for (PartialDownloadTask task :
                mPartialDownloadTasks) {
            if(task.getState()!=BaseTask.SUCCESS) {
                Log.d(TAG, "check success: partial task id "+task.getId() +" is "+BaseTask.getStateName(task.getState()));
                success = false;
                break;
            }
        }

        // if success, set success state and notify it
        if(success) {
            setState(BaseTask.SUCCESS);
        } else switch (getState()) {
            case BaseTask.CONNECTING:
            case BaseTask.PENDING:
            case BaseTask.RUNNING:
                setState(BaseTask.FAILURE_TERMINATED);
                break;
            default:
                Log.d(TAG, "check success: failed and task state now is "+ BaseTask.getStateName(getState()));
                break;
        }
        notifyTaskChanged();

        // else do nothing

        Log.d(TAG, "reach the end of file download task with flag success is "+ success);
        Log.d(PartialDownloadTask.TAG, "reach the end of file download task with flag success is "+success);
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
    public boolean isTaskFailed() {
        return getState()==FAILURE_TERMINATED;
    }

    public synchronized void notifyPartialTaskChanged(PartialDownloadTask task) {

        switch (task.getState()) {
            case PENDING:
                // do nothing
                break;
            case RUNNING:
                // just notify progress
                // do nothing
                break;
            case SUCCESS:
                // one partial task is success
                // we still do nothing
                break;
            case FAILURE_TERMINATED:
                // one failed
                switch (getState()) {
                    case RUNNING:
                      // one failed means task failed
                      setState(FAILURE_TERMINATED, task.getMessage());
                      Log.d(TAG,"partial task "+task.getId()+" is failure terminated with message: "+task.getMessage());
                        break;
                    case FAILURE_TERMINATED:
                    default:
                        // do nothing
                }
                break;
        }
 /*       StringBuilder progress = new StringBuilder();
        progress.append("log progress: ");
        for (int i = 0; i < mPartialDownloadTasks.size(); i++) {
            progress.append(" task_").append(i + 1).append(" = ").append((int) (100* mPartialDownloadTasks.get(i).getDownloadedInBytes() / mPartialDownloadTasks.get(i).getDownloadLength()));
        }
        Log.d(TAG, progress.toString());*/
        notifyTaskChanged();
    }

    public int getConnectionNumber() {
        if(getTaskManager()!=null)
        return getTaskManager().getConnectionsPerTask();
        return BaseTaskManager.getRecommendConnectionPerTask();
    }
}
