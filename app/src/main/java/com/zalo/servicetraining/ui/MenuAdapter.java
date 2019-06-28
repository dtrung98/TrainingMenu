package com.zalo.servicetraining.ui;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zalo.servicetraining.R;
import com.zalo.servicetraining.model.Item;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuItemHolder> {
    private static final String TAG = "CircleEventTypeAdapter";


    private ArrayList<Item> mData = new ArrayList<>();

    public List<Item> getData() {
        return mData;
    }

    public interface OnItemClickListener {
        void onEventItemClick(Item item);
    }

    private OnItemClickListener mListener;
    public void setListener(OnItemClickListener listener) {
        mListener = listener;
    }
    public void removeListener() {
        mListener = null;
    }


    public void setData(List<Item> data) {
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

         return new MenuItemHolder(inflater.inflate(R.layout.item_card,parent,false));

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

        @OnClick({R.id.constraint_root})
        void clickPanel() {
            if(mListener!=null) mListener.onEventItemClick(mData.get(getAdapterPosition()));
        }

        public void bind(Item item) {
            mTitle.setText(item.getTitle());


            if(item.getDescription()!=null&&!item.getDescription().isEmpty()) {
                mDescription.setVisibility(View.VISIBLE);
                mDescription.setText(item.getDescription());
            }
            else {
                mDescription.setVisibility(View.INVISIBLE);
            }

        }
    }
}
