package com.zalo.servicetraining.fundamental.noteapp.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zalo.servicetraining.model.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "NoteDatabaseHelper";

    // Database Name
    private static final String NOTE_DATABASE_NAME = "note_db";

    // Database Version
    private static final int DATABASE_VERSION = 1;
    private static NoteDatabaseHelper sNoteDatabaseHelper;
    public static NoteDatabaseHelper getInstance(Context context) {
        if(sNoteDatabaseHelper==null) sNoteDatabaseHelper = new NoteDatabaseHelper(context.getApplicationContext());
        return sNoteDatabaseHelper;
    }


    public NoteDatabaseHelper(Context context) {
        super(context,NOTE_DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        //create notes table if not exists
        sqLiteDatabase.execSQL(Note.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+Note.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public long addNewNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
       long id =  db.insert(Note.TABLE_NAME,null,note.getValues());
       db.close();
       return id;
    }

    public Note getNote(long id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(Note.TABLE_NAME,
                Note.NOTE_PROJECTIONS,
                Note._ID + " =? ",new String[] {String.valueOf(id)},null,null,null,null);
        if(cursor!=null) {
            cursor.moveToFirst();
            Note note = new Note(cursor);
                   /* .setmId(cursor.getInt(cursor.getColumnIndex(Note.COLUMN_ID)))
                    .setTitle(cursor.getString(cursor.getColumnIndex(Note.COLUMN_TITLE)))
                    .setContent(cursor.getString(cursor.getColumnIndex(Note.COLUMN_CONTENT)));*/
            cursor.close();
            return note;
        }
        return null;
    }

    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(Note.TABLE_NAME,Note.NOTE_PROJECTIONS,null,null,null,null,Note.COLUMN_TIME_STAMP +" DESC");
        if(cursor!=null) {
            if(cursor.moveToFirst())
                do {
                    try {
                        Note note = new Note(cursor);
                        notes.add(note);
                    }catch (Exception ignored) {}

                } while (cursor.moveToNext());

            cursor.close();
        }
        db.close();
        return notes;
    }

    public void dropAndRecreateNoteTable() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS "+Note.TABLE_NAME);
        onCreate(db);
    }

    public int saveNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.update(Note.TABLE_NAME,note.getValues(),Note._ID+" = ?",new String[]{String.valueOf(note.getId())});
        db.close();
        return result;
    }

    public int deleteNote(Note mNote) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(Note.TABLE_NAME,Note._ID +" =?",new String[]{String.valueOf(mNote.getId())});
        db.close();
        return result;
    }
}
