package com.zalo.servicetraining.ui.contentprovider;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.zalo.servicetraining.R;
import com.zalo.servicetraining.data.NoteDatabaseHelper;
import com.zalo.servicetraining.model.Note;

import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DemoNoteApp extends AppCompatActivity implements NoteAdapter.OnItemClickListener {
    private static final String TAG = "DemoNoteApp";
    public static final int DETAIL_REQUEST_CODE = 1;


    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;

    @BindView(R.id.title)
    TextView mTitle;

    NoteAdapter mAdapter;

    @BindView(R.id.add_button)
    View mAddButton;

    @OnClick(R.id.back_button)
    void back() {
        finish();
    }

    @OnClick(R.id.add_button)
    void addNewNote() {
        Intent intent = new Intent(this,NoteDetailActivity.class);
        intent.setAction(NoteDetailActivity.ACTION_NEW_NOTE);
        startActivityForResult(intent,DETAIL_REQUEST_CODE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);
        ButterKnife.bind(this);
        init();

       // NoteDatabaseHelper.getInstance(this).dropAndRecreateNoteTable();
        refreshData();
    }
    private void init() {
        mTitle.setText(R.string.notes);
        mAddButton.setVisibility(View.VISIBLE);
        mAdapter = new NoteAdapter();
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,1,RecyclerView.VERTICAL,false));
        mSwipeRefresh.setOnRefreshListener(this::refreshData);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == DETAIL_REQUEST_CODE) {
            if(resultCode==RESULT_OK) {
                if(data!=null&&data.getAction()!=null) {
                    switch (data.getAction()) {
                        case NoteDetailActivity.ACTION_NEW_NOTE: refreshData();
                            Log.d(TAG, "onActivityResult: u "+data.getAction()+" #" + data.getLongExtra(Note._ID, 0));
                            break;
                        case NoteDetailActivity.UPDATE_OR_SAVE :
                            Log.d(TAG, "onActivityResult: u "+data.getAction()+" #" + data.getIntExtra(Note._ID, 0));
                            refreshData();
                            break;
                        case NoteDetailActivity.DELETE_NOTE :
                            Log.d(TAG, "onActivityResult: u "+data.getAction()+" #" + data.getIntExtra(Note._ID, 0));
                            refreshData();
                            break;
                    }

                }
                else
                    Log.d(TAG, "onActivityResult: sorry no data or action returned");
            }
        }
    }

    private void refreshData() {
        List<Note> list = NoteDatabaseHelper.getInstance(this).getAllNotes();
       mAdapter.setData(list);
       mSwipeRefresh.setRefreshing(false);
    }

    @Override
    public void onNoteItemClick(Note item, int position) {
        Intent intent = new Intent(this,NoteDetailActivity.class);
        intent.putExtra(Note.TAG,item);
        startActivityForResult(intent,DETAIL_REQUEST_CODE);
    }

    @Override
    public void onNoteLongClick(Note item, int position) {
        NoteOptionDialogFragment.newInstance(item).show(getSupportFragmentManager(), NoteOptionDialogFragment.TAG);
    }


}
