package com.zalo.trainingmenu.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

public class Note implements BaseColumns, Parcelable {
    public static final String TAG = "Note";

    // Table Name
    public static final String TABLE_NAME ="notes";

    // Column Name

    public static final String COLUMN_TIME_STAMP = "timestamp";

    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";


    // Variable
    private int mId;
    private String mTimeStamp;

    private String mTitle;
    private String mContent;



    // Create table SQL Query
    public static final String CREATE_TABLE =
            "CREATE TABLE "+TABLE_NAME+ "("
            +_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_TITLE +" TEXT, "
            + COLUMN_CONTENT+ " TEXT, "
            +COLUMN_TIME_STAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            +")";

    public static final String[] NOTE_PROJECTIONS = new String[]{Note._ID,Note.COLUMN_TITLE,Note.COLUMN_CONTENT,Note.COLUMN_TIME_STAMP};

    public Note() {
    }

    public Note(Cursor cursor) {
        this.setId(cursor.getInt(cursor.getColumnIndex(Note._ID)))
                .setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)))
                .setContent(cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT)))
                .setTimeStamp(cursor.getString(cursor.getColumnIndex(COLUMN_TIME_STAMP)));
    }

    public Note(String mTitle, String mContent) {
        this.mTitle = mTitle;
        this.mContent = mContent;
    }

    protected Note(Parcel in) {
        mId = in.readInt();
        mTimeStamp = in.readString();
        mTitle = in.readString();
        mContent = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mTimeStamp);
        dest.writeString(mTitle);
        dest.writeString(mContent);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    public int getId() {
        return mId;
    }

    public Note setId(int mId) {
        this.mId = mId;
        return this;
    }

    public String getTimeStamp() {
        return mTimeStamp;
    }

    public Note setTimeStamp(String mTimeStamp) {
        this.mTimeStamp = mTimeStamp;
        return this;
    }

    public String getTitle() {
        return mTitle;
    }

    public Note setTitle(String mTitle) {
        this.mTitle = mTitle;
        return this;
    }

    public String getContent() {
        return mContent;
    }

    public Note setContent(String mContent) {
        this.mContent = mContent;
        return this;
    }

    public ContentValues getValues() {
        ContentValues values = new ContentValues();

        values.put(COLUMN_TITLE,getTitle());
        values.put(COLUMN_CONTENT,getContent());
        return values;
    }

    @Override
    public String toString() {
        return "Note{" +
                "mId=" + mId +
                ", mTimeStamp='" + mTimeStamp + '\'' +
                ", mTitle='" + mTitle + '\'' +
                ", mContent='" + mContent + '\'' +
                '}';
    }
}
