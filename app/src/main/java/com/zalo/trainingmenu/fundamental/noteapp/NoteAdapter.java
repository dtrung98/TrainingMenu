package com.zalo.trainingmenu.fundamental.noteapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.model.Note;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.MenuItemHolder> {
    private static final String TAG = "NoteAdapter";
    private ArrayList<Note> mData = new ArrayList<>();

    public List<Note> getData() {
        return mData;
    }

    public interface OnItemClickListener {
        void onNoteItemClick(Note item, int position);
        void onNoteLongClick(Note item,int position);
    }
    private OnItemClickListener mListener;
    public void setListener(OnItemClickListener listener) {
        mListener = listener;
    }
    public void removeListener() {
        mListener = null;
    }


    public void setData(List<Note> data) {
        mData.clear();
        if (data !=null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public MenuItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

         return new MenuItemHolder(inflater.inflate(R.layout.item_note,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemHolder holder, int position) {
        holder.bind(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public class MenuItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView mTitle;

        @BindView(R.id.description)
        TextView mDescription;

        public MenuItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @OnClick(R.id.constraint_root)
        void clickPanel() {
            if(mListener!=null) mListener.onNoteItemClick(mData.get(getAdapterPosition()),getAdapterPosition());
        }

        @OnLongClick(R.id.constraint_root)
        void longClickPanel() {
            if(mListener !=null) mListener.onNoteLongClick(mData.get(getAdapterPosition()),getAdapterPosition());
        }

        public void bind(Note item) {
            mTitle.setText(item.getTitle());


            if(item.getContent()!=null&&!item.getContent().isEmpty()) {
                mDescription.setVisibility(View.VISIBLE);
                mDescription.setText(item.getContent());
            }
            else {
                mDescription.setVisibility(View.INVISIBLE);
            }

        }
    }
}
