package com.zalo.servicetraining.ui.userdata;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zalo.servicetraining.R;
import com.zalo.servicetraining.model.UserData;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserDataAdapter extends RecyclerView.Adapter<UserDataAdapter.ViewHolder> {

    private ArrayList<UserData> mData = new ArrayList<>();

    private ItemClickListener mClickListener;

    // data is passed into the constructor
    UserDataAdapter() {}

    public void setData(List<UserData> userData) {
        mData.clear();
        if(userData!=null) mData.addAll(userData);
        notifyDataSetChanged();
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_data, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final UserData user = mData.get(position);
        holder.bind(user);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    private static Bitmap getBitmapFromURL(String imgUrl) {
        try {
            URL url = new URL(imgUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
         @BindView(R.id.user_name_text_view)
         TextView userName;
         @BindView(R.id.id_text_view)
         TextView id;
         @BindView(R.id.user_image_view)
         ImageView iconAvatar;
         @BindView(R.id.over_view)
         View overView;

         @BindView(R.id.number)
         TextView mNumber;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            overView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                int pos = getAdapterPosition();
                mClickListener.onItemClick(view,
                        pos,
                        ((BitmapDrawable) iconAvatar.getDrawable()).getBitmap(),
                        mData.get(pos).getUsername(),
                        mData.get(pos).getId());
            }
        }

        @SuppressLint("SetTextI18n")
        void bind(UserData userData) {
            mNumber.setText(String.valueOf(getAdapterPosition()+1));
            userName.setText(userData.getUsername());
            id.setText("ID "+ userData.getId());
            // new LoadBitmap().execute(this,userData.getAvatarPath());
            Glide.with(iconAvatar.getContext())
                    .load(userData.getAvatarPath())
                    .into(iconAvatar);


        }
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position, Bitmap avatar, String username, int id);
    }
    private static class LoadBitmap extends AsyncTask<Object, Void, Bitmap> {

        @Override
        protected void onPostExecute(Bitmap bitmap1) {
            holder.iconAvatar.setImageBitmap(bitmap1);
        }

       ViewHolder holder;

        @Override
        protected Bitmap doInBackground(Object... url) {
            holder = (ViewHolder) url[0];
            return getBitmapFromURL(((String)url[1]));
        }
    }
}