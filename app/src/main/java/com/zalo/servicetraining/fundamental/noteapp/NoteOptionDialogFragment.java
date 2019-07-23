package com.zalo.servicetraining.fundamental.noteapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.zalo.servicetraining.R;
import com.zalo.servicetraining.model.Note;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class NoteOptionDialogFragment extends DialogFragment {
    public static final String TAG = "NoteOptionDialogFragment";
    private Note mNote;

    public static NoteOptionDialogFragment newInstance(Note note) {


        NoteOptionDialogFragment fragment = new NoteOptionDialogFragment();
        fragment.mNote = note;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.note_menu_dialog,container,false);
    }

    @Override
    public int getTheme() {
        return R.style.DialogDimDisabled;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        setCancelable(true);
    }

    @OnClick(R.id.detail)
    void detail() {
        if(mNote!=null) Log.d(TAG, "detail: "+ mNote.toString());
    }

    @OnClick(R.id.edit)
    void edit() {

    }

    @OnClick(R.id.delete)
    void delete(){

    }

}
