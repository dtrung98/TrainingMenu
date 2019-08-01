package com.zalo.servicetraining.downloader.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.zalo.servicetraining.downloader.base.BaseTask;
import com.zalo.servicetraining.downloader.task.ranges.PartialDownloadTask;

public class PartialInfo implements BaseColumns {
    public static final String TAG = "PartialInfo";

    public static final String TABLE_NAME = TAG +'s';

    // Column Name

    public static final String EXTRA_START_BYTE = "start_byte";
    public static final String EXTRA_END_BYTE ="end_byte";
    public static final String EXTRA_ID ="id";
    public static final String EXTRA_DOWNLOADED_IN_BYTE ="downloaded_in_bytes";
    public static final String EXTRA_STATE ="state";

    // CREATE TABLE SQL QUERY
    public static final String CREATE_TABLE =
            "CREATE TABLE "+TABLE_NAME +"("
            + _ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "
            + EXTRA_ID + " INTEGER, "
            + EXTRA_STATE+" INTEGER, "
            + EXTRA_START_BYTE+ " INTEGER, "
            +EXTRA_END_BYTE + " INTEGER, "
            +EXTRA_DOWNLOADED_IN_BYTE +" INTEGER"
            +")";
    public static final String[] _PROJECTIONS = new String[] {_ID,EXTRA_ID, EXTRA_STATE, EXTRA_START_BYTE, EXTRA_END_BYTE, EXTRA_DOWNLOADED_IN_BYTE};

    public static PartialInfo restoreInstance(Cursor cursor) {
        PartialInfo info = new PartialInfo(cursor.getInt(cursor.getColumnIndex(EXTRA_START_BYTE)),cursor.getInt(cursor.getColumnIndex(EXTRA_END_BYTE)),cursor.getInt(cursor.getColumnIndex(EXTRA_ID)));
        info._IdKey = cursor.getInt(cursor.getColumnIndex(_ID));
        info.mDownloadedInBytes = cursor.getInt(cursor.getColumnIndex(EXTRA_DOWNLOADED_IN_BYTE));
        info.mState = cursor.getInt(cursor.getColumnIndex(EXTRA_STATE));
        return info;
    }

    private final long mStartByte;
    private final long mEndByte;
    private int _IdKey = 0;
    private final int mId;
    private long mDownloadedInBytes;
    private int mState = BaseTask.PENDING;



    public long getStartByte() {
        return mStartByte;
    }

    public long getEndByte() {
        return mEndByte;
    }


    public int getId() {
        return mId;
    }

    public long getDownloadedInBytes() {
        return mDownloadedInBytes;
    }

    public void setDownloadedInBytes(long downloadedInBytes) {
        mDownloadedInBytes = downloadedInBytes;
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        mState = state;
    }

    public PartialInfo newInstance(PartialDownloadTask task) {
        PartialInfo info = new PartialInfo(task.getStartByte(),task.getEndByte(),task.getId());
        info.setState(task.getState());
        info.setDownloadedInBytes(task.getDownloadedInBytes());
        return info;
    }

    public PartialInfo(long startByte, long endByte, int id) {
        mStartByte = startByte;
        mEndByte = endByte;

        mId = id;
    }

    public ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put(_ID, _IdKey);
        values.put(EXTRA_ID, getId());
        values.put(EXTRA_STATE,getState());
        values.put(EXTRA_START_BYTE,getStartByte());
        values.put(EXTRA_END_BYTE,getEndByte());
        values.put(EXTRA_DOWNLOADED_IN_BYTE,getDownloadedInBytes());
        return values;
    }

}
