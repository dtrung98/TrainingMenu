package com.zalo.servicetraining.downloader.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zalo.servicetraining.App;
import com.zalo.servicetraining.downloader.model.PartialInfo;
import com.zalo.servicetraining.downloader.model.TaskInfo;
import com.zalo.servicetraining.fundamental.noteapp.data.NoteDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class DownloaderDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DownloaderDatabaseHelper";

    // Database Name
    public static final String DATABASE_NAME = "downloader_db";
    private static final int DATABASE_VERSION = 1;
    private static DownloaderDatabaseHelper sDownloaderDatabaseHelper;
    public static DownloaderDatabaseHelper getInstance() {
        if(sDownloaderDatabaseHelper==null) sDownloaderDatabaseHelper = new DownloaderDatabaseHelper(App.getInstance().getApplicationContext());
        return sDownloaderDatabaseHelper;
    }
    public static void destroy() {
        sDownloaderDatabaseHelper.close();
        sDownloaderDatabaseHelper = null;
    }

    private DownloaderDatabaseHelper(Context context) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(TaskInfo.CREATE_TABLE);
        sqLiteDatabase.execSQL(PartialInfo.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TaskInfo.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+PartialInfo.TABLE_NAME);
    }

    public List<TaskInfo> getTaskInfoList() {
        List<TaskInfo> taskInfoList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TaskInfo.TABLE_NAME,null,null,null,null,null,TaskInfo.EXTRA_ID);
        if(cursor!=null) {
            if(cursor.moveToFirst())
            do {
                try {
                    TaskInfo info = TaskInfo.restoreInstance(db,cursor);
                    taskInfoList.add(info);
                } catch (Exception ignored) {}
            } while (cursor.moveToNext());
        }
        return taskInfoList;
    }

}
