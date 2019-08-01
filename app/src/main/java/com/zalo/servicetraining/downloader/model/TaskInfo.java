package com.zalo.servicetraining.downloader.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zalo.servicetraining.downloader.base.BaseTask;

import java.util.ArrayList;

public class TaskInfo {
    private static final String TAG = "TaskInfo";

    // Used by Adapter
    public static final int SECTION_DOWNLOADING = 0;
    public static final int SECTION_DOWNLOADED = 1;

    // Task Property
    private final int mId;
    private int mState = BaseTask.PENDING;

    private final long mCreatedTime;
    private final String mFileTitle ;
    private final String mDirectory;
    private final String mURLString;

    private float mSpeedInBytes=0;

    private String mMessage;

    private float mProgress = 0;
    private boolean mIsProgressSupport;

    private long mDownloadedInBytes;
    private long mFileContentLength = -1;

    private long mFirstExecutedTime = -1;
    private long mLastExecutedTime = -1;
    private long mFinishedTime = -1;
    private long mRunningTime = 0;

    // Table Name
    public static final String TABLE_NAME = TAG+"s";

    private ArrayList<PartialInfo> mPartialInfoList = new ArrayList<>();

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_STATE ="state";

    public static final String EXTRA_CREATED_TIME ="created_time";
    public static final String EXTRA_FILE_TITLE ="file_title";
    public static final String EXTRA_DIRECTORY ="directory";
    public static final String EXTRA_URL_STRING ="url_string";

    public static final String EXTRA_SPEED_IN_BYTES ="speed_in_bytes";

    public static final String EXTRA_MESSAGE ="message";

    public static final String EXTRA_PROGRESS ="progress";
    public static final String EXTRA_IS_PROGRESS_SUPPORT = "is_progress_support";

    public static final String EXTRA_DOWNLOADED_IN_BYTES = "downloaded_in_bytes";
    public static final String EXTRA_FILE_CONTENT_LENGTH = "file_content_length";

    public static final String EXTRA_FIRST_EXECUTED_TIME = "first_executed_time";
    public static final String EXTRA_LAST_EXECUTED_TIME = "last_executed_time";
    public static final String EXTRA_FINISHED_TIME ="finished_time";
    public static final String EXTRA_RUNNING_TIME ="running_time";

    public static final String EXTRA_PARTIAL_INFO_LIST = "partial_info_list";

    // Create table SQL Query
    public static final String CREATE_TABLE =
            "CREATE TABLE "+TABLE_NAME + "("
                     +EXTRA_ID +" INTEGER PRIMARY KEY, "
                    +EXTRA_STATE +" INTEGER, "
                    +EXTRA_CREATED_TIME +" INTEGER, "
                    +EXTRA_FILE_TITLE +" TEXT, "
                    +EXTRA_DIRECTORY +" TEXT, "
                    +EXTRA_URL_STRING +" TEXT, "

                    +EXTRA_SPEED_IN_BYTES +" FLOAT, "
                    +EXTRA_MESSAGE +" TEXT, "
                    +EXTRA_PROGRESS +" FLOAT, "
                    +EXTRA_IS_PROGRESS_SUPPORT +" INTEGER DEFAULT 0, "
                    +EXTRA_DOWNLOADED_IN_BYTES +" INTEGER DEFAULT 0, "
                    +EXTRA_FILE_CONTENT_LENGTH +" INTEGER DEFAULT -1, "
                    +EXTRA_FIRST_EXECUTED_TIME +" INTEGER, "
                    +EXTRA_LAST_EXECUTED_TIME +" INTEGER, "
                    +EXTRA_FINISHED_TIME +" INTEGER, "
                    +EXTRA_RUNNING_TIME +" INTEGER, "
                    + EXTRA_PARTIAL_INFO_LIST +" TEXT, "
                    +")";

    public static TaskInfo restoreInstance(SQLiteDatabase readableDb, Cursor taskInfoCursor) {

        TaskInfo info = new TaskInfo(
                taskInfoCursor.getInt(taskInfoCursor.getColumnIndex(EXTRA_ID)),
                taskInfoCursor.getLong(taskInfoCursor.getColumnIndex(EXTRA_CREATED_TIME)),
                taskInfoCursor.getString(taskInfoCursor.getColumnIndex(EXTRA_FILE_TITLE)),
                taskInfoCursor.getString(taskInfoCursor.getColumnIndex(EXTRA_DIRECTORY)),
                taskInfoCursor.getString(taskInfoCursor.getColumnIndex(EXTRA_URL_STRING)));
        info.mSpeedInBytes = taskInfoCursor.getFloat(taskInfoCursor.getColumnIndex(EXTRA_SPEED_IN_BYTES));
        info.mMessage = taskInfoCursor.getString(taskInfoCursor.getColumnIndex(EXTRA_MESSAGE));
        info.mProgress = taskInfoCursor.getFloat(taskInfoCursor.getColumnIndex(EXTRA_PROGRESS));
        info.mIsProgressSupport = taskInfoCursor.getInt(taskInfoCursor.getColumnIndex(EXTRA_IS_PROGRESS_SUPPORT)) != 0;
        info.mDownloadedInBytes = taskInfoCursor.getLong(taskInfoCursor.getColumnIndex(EXTRA_DOWNLOADED_IN_BYTES));
        info.mFileContentLength = taskInfoCursor.getLong(taskInfoCursor.getColumnIndex(EXTRA_DOWNLOADED_IN_BYTES));
        info.mFirstExecutedTime = taskInfoCursor.getLong(taskInfoCursor.getColumnIndex(EXTRA_FIRST_EXECUTED_TIME));
        info.mLastExecutedTime = taskInfoCursor.getLong(taskInfoCursor.getColumnIndex(EXTRA_LAST_EXECUTED_TIME));
        info.mFinishedTime = taskInfoCursor.getLong(taskInfoCursor.getColumnIndex(EXTRA_FIRST_EXECUTED_TIME));


        String partialListString = taskInfoCursor.getString(taskInfoCursor.getColumnIndex(EXTRA_PARTIAL_INFO_LIST));
        Cursor partialCursor = readableDb.query(PartialInfo.TABLE_NAME, null,PartialInfo._ID+" IN ("+partialListString+")",null,null,null,PartialInfo.EXTRA_ID);
        info.mPartialInfoList.clear();
        if(partialCursor!=null) {
            if(partialCursor.moveToFirst()) {
                do {
                    try {
                        PartialInfo partialInfo = PartialInfo.restoreInstance(partialCursor);
                        info.mPartialInfoList.add(partialInfo);
                    } catch (Exception ignored) {}
                } while (partialCursor.moveToNext());
            }
            partialCursor.close();
        }

        return info;
    }

    public int getId() {
        return mId;
    }

    public long getCreatedTime() {
        return mCreatedTime;
    }

    public String getFileTitle() {
        return mFileTitle;
    }

    public String getDirectory() {
        return mDirectory;
    }

    public String getURLString() {
        return mURLString;
    }


    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public long getDownloadedInBytes() {
        return mDownloadedInBytes;
    }

    public void setDownloadedInBytes(long downloadedInBytes) {
        mDownloadedInBytes = downloadedInBytes;
    }

    public long getFileContentLength() {
        return mFileContentLength;
    }

    public void setFileContentLength(long fileContentLength) {
        mFileContentLength = fileContentLength;
    }

    public long getFirstExecutedTime() {
        return mFirstExecutedTime;
    }

    public long getFinishedTime() {
        return mFinishedTime;
    }

    public void setFinishedTime(long finishedTime) {
        mFinishedTime = finishedTime;
    }

    public long getRunningTime() {
        return mRunningTime;
    }

    public void setRunningTime(long runningTime) {
        mRunningTime = runningTime;
    }

    public TaskInfo(int id, long createdTime, String fileTitle, String directory, String URLString) {
        mId = id;
        mCreatedTime = createdTime;
        mFileTitle = fileTitle;
        mDirectory = directory;
        mURLString = URLString;
    }

    public static TaskInfo newInstance(BaseTask task) {
        TaskInfo info = new TaskInfo(task.getId(),task.getCreatedTime(),task.getFileTitle(),task.getDirectory(),task.getURLString());
        info.setState(task.getState());
        info.setProgress(task.getProgress());
        info.setMessage(task.getMessage());
        info.setProgressSupport(task.isProgressSupport());
        info.setSpeedInBytes(task.getSpeedInBytes());
        info.mFirstExecutedTime = task.getFirstExecutedTime();
        info.mLastExecutedTime = task.getLastExecutedTime();
        info.mFinishedTime = task.getFinishedTime();
        info.mRunningTime = task.getRunningTime();
        info.mFileContentLength = task.getFileContentLength();
        info.mDownloadedInBytes = task.getDownloadedInBytes();
        return info;
    }

    public int getSectionState() {
        return mState== BaseTask.SUCCESS? SECTION_DOWNLOADED : SECTION_DOWNLOADING;
    }

    public int getState() {
        return mState;
    }

    public TaskInfo setState(int state) {
        mState = state;
        return this;

    }

    public boolean isProgressSupport() {
        return mIsProgressSupport;
    }

    public TaskInfo setProgressSupport(boolean progressSupport) {
        mIsProgressSupport = progressSupport;
        return this;
    }

    public float getProgress() {
        return mProgress;
    }

    public TaskInfo setProgress(float progress) {
        mProgress = progress;
        return this;

    }

    public float getSpeedInBytes() {
        return mSpeedInBytes;
    }

    public void setSpeedInBytes(float speedInBytes) {
        mSpeedInBytes = speedInBytes;
    }

    public long getLastExecutedTime() {
        return mLastExecutedTime;
    }

    public void setLastExecutedTime(long lastExecutedTime) {
        mLastExecutedTime = lastExecutedTime;
    }
}
