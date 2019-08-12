package com.zalo.trainingmenu.fundamental.noteapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zalo.trainingmenu.App;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.fundamental.noteapp.data.NoteDatabaseHelper;
import com.zalo.trainingmenu.model.Note;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class NoteDetailActivity extends AppCompatActivity {
    public static final String TAG = "NoteDetailActivity";

    public static final String ACTION_NOTE_DETAIL = "action_note_detail";
    public static final String ACTION_NEW_NOTE = "action_new_note";

    public static final String DELETE_NOTE = "delete_note";
    public static final String UPDATE_OR_SAVE = "update_or_save";

    private Note mNote;

    @BindView(R.id.time_created)
    TextView mTimeCreatedTextView;

    @BindView(R.id.name_edit)
    TextView mNameEditView;

    @BindView(R.id.content_edit)
    TextView mContentEditView;

    @BindView(R.id.save)
    View mSaveView;

    @BindView(R.id.delete)
    View mDeleteView;

    @OnClick(R.id.save)
    void save() {
        mNote.setTitle(mNameEditView.getText().toString());
        mNote.setContent(mContentEditView.getText().toString());
        if(mAction.equals(ACTION_NEW_NOTE)) {
            long result = NoteDatabaseHelper.getInstance(this).addNewNote(mNote);
            Toasty.success(App.getInstance().getApplicationContext(), "Add new note successfully, #return " + result).show();

            Intent intent = new Intent();
            intent.setAction(ACTION_NEW_NOTE);
            intent.putExtra(Note._ID, result);
            setResult(RESULT_OK, intent);
            finish();
        } else {

            int result = NoteDatabaseHelper.getInstance(this).saveNote(mNote);
            Toasty.success(App.getInstance().getApplicationContext(), "Save note successfully, #return " + result).show();

            Intent intent = new Intent();
            intent.setAction(UPDATE_OR_SAVE);
            intent.putExtra(Note._ID, mNote.getId());
            setResult(RESULT_OK, intent);
           finish();
        }
    }

    @OnClick(R.id.delete)
    void delete() {
        if(mAction.equals(ACTION_NOTE_DETAIL)) {
            int result = NoteDatabaseHelper.getInstance(this).deleteNote(mNote);
            Toasty.success(App.getInstance().getApplicationContext(), "Delete note successfully, #return " + result).show();
            Intent intent = new Intent();
            intent.setAction(DELETE_NOTE);
            intent.putExtra(Note._ID, mNote.getId());
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @OnClick(R.id.back_button)
    void back() {
        finish();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_detail_activity);
        ButterKnife.bind(this);
        getParameters();
        refreshData();
    }
    private String mAction;

    public void getParameters() {
        if(getIntent()!=null) {

            if(getIntent().getAction()!=null&&getIntent().getAction().equals(ACTION_NEW_NOTE)) {
                mNote = new Note();
                mAction = ACTION_NEW_NOTE;
            } else {
                mNote = getIntent().getParcelableExtra(Note.TAG);
                mAction = ACTION_NOTE_DETAIL;
            }
        }

    }

    private void refreshData() {
        if(mAction.equals(ACTION_NOTE_DETAIL)&&mNote!=null) {
            mTimeCreatedTextView.setText(mNote.getTimeStamp() + " | #"+mNote.getId());
            mNameEditView.setText(mNote.getTitle());
            mContentEditView.setText(mNote.getContent());
        } else {
            mTimeCreatedTextView.setVisibility(View.GONE);
            mDeleteView.setVisibility(View.GONE);
        }
    }
}
