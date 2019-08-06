package com.zalo.servicetraining.fundamental.noteapp;


import android.content.Intent;
import android.util.Log;
import android.view.Gravity;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zalo.servicetraining.R;
import com.zalo.servicetraining.fundamental.noteapp.data.NoteDatabaseHelper;
import com.zalo.servicetraining.model.Note;
import com.zalo.servicetraining.mainui.base.AbsListActivity;

import java.util.List;

import butterknife.BindView;

public class DemoNoteActivity extends AbsListActivity implements NoteAdapter.OnItemClickListener {
    private static final String TAG = "DemoNoteActivity";
    public static final int DETAIL_REQUEST_CODE = 1;

    @BindView(R.id.root)
    CoordinatorLayout mRoot;


    NoteAdapter mAdapter;

    FloatingActionButton mAddButton;


    void addNewNote() {
        Intent intent = new Intent(this,NoteDetailActivity.class);
        intent.setAction(NoteDetailActivity.ACTION_NEW_NOTE);
        startActivityForResult(intent,DETAIL_REQUEST_CODE);
    }

    @Override
    protected void onInitRecyclerView() {
        addPlusButton();
        getRecyclerView().setLayoutManager(new GridLayoutManager(this,1,RecyclerView.VERTICAL,false));
        mAdapter = new NoteAdapter();
        mAdapter.setListener(this);
        getRecyclerView().setAdapter(mAdapter);

    }

    private void addPlusButton() {
        mAddButton = new FloatingActionButton(this);
        mAddButton.setImageResource(R.drawable.ic_add_black_24dp);
        float oneDP = getResources().getDimension(R.dimen.oneDp);
        mAddButton.setCustomSize((int) (60*oneDP));
       CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams((int)(60*oneDP),(int)(60*oneDP));
       params.gravity = Gravity.BOTTOM|Gravity.END;
       params.bottomMargin = (int)(oneDP*16);
       params.setMarginEnd(params.bottomMargin);
       mAddButton.setOnClickListener(view -> addNewNote());
       mRoot.addView(mAddButton,params);
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

    @Override
    protected void refreshData() {
        List<Note> list = NoteDatabaseHelper.getInstance(this).getAllNotes();
       mAdapter.setData(list);
       getSwipeRefreshLayout().setRefreshing(false);
    }

    @Override
    public void onNoteItemClick(Note item, int position) {
        Intent intent = new Intent(this, NoteDetailActivity.class);
        intent.putExtra(Note.TAG,item);
        startActivityForResult(intent,DETAIL_REQUEST_CODE);
    }

    @Override
    public void onNoteLongClick(Note item, int position) {
        NoteOptionDialogFragment.newInstance(item).show(getSupportFragmentManager(), NoteOptionDialogFragment.TAG);
    }

}
