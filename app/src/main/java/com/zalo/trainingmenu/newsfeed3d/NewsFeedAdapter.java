package com.zalo.trainingmenu.newsfeed3d;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ldt.parallaximageview.ParallaxImageView;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.newsfeed3d.model.NewsFeedObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsFeedAdapter extends RecyclerView.Adapter<NewsFeedAdapter.BindableHolder> {
    private ArrayList<NewsFeedObject> mData = new ArrayList<>();

    public void setData(List<NewsFeedObject> data) {
        mData.clear();
        if(data!=null)
            mData.addAll(data);
        notifyDataSetChanged();
    }

    public NewsFeedAdapter() {
        super();
    }

    @NonNull
    @Override
    public BindableHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Photo3DHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_feed_3d_square,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindableHolder holder, int position) {
        holder.bind(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public abstract static class BindableHolder<T> extends RecyclerView.ViewHolder {
        public BindableHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(T t) {

        }
    }

    public void onResume() {

    }

    public void onPause() {

    }

    public static class NewsFeedHolder extends BindableHolder<NewsFeedObject> {

        public NewsFeedHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class Photo3DHolder extends NewsFeedHolder implements View.OnAttachStateChangeListener {
        @BindView(R.id.photo_3d_view)
        ParallaxImageView mParallaxImageView;

        @BindView(R.id.content_text_view)
        TextView mContentTextView;

        public Photo3DHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.addOnAttachStateChangeListener(this);
//            mParallaxImageView.setOpaque(false);
            mParallaxImageView.initRenderer();
            mParallaxImageView.onResume();
        }

        @Override
        public void bind(NewsFeedObject o) {
            super.bind(o);
                mParallaxImageView.load(o.getImageObject());
                mContentTextView.setText(o.getContentText());
                mParallaxImageView.setName(getAdapterPosition()+" - "+o.getContentText());
           /* Bitmap b = null;
            try {
                String original = PreferenceUtil.getInstance().getSavedOriginal3DPhoto();
                if (original != null && !original.isEmpty()) b = BitmapFactory.decodeFile(original);
                if (b == null)
                    b = BitmapFactory.decodeFile("/storage/emulated/0/Download/ball.jpg");
                if (b == null)
                    b = BitmapFactory.decodeFile("/storage/emulated/0/download/poro.jpg");
                if (b != null)
                    mParallaxImageView.setOriginalPhoto(b);

            } catch (Exception ignored) {
            }

            try {
                b = null;
                String depth = PreferenceUtil.getInstance().getSavedDepthPhoto();
                if (depth != null && !depth.isEmpty()) b = BitmapFactory.decodeFile(depth);
                if (b == null)
                    b = BitmapFactory.decodeFile("/storage/emulated/0/Download/ball_depth.jpg");
                if (b == null)
                    b = BitmapFactory.decodeFile("/storage/emulated/0/download/poro_depth.jpg");
                if (b != null)
                    mParallaxImageView.setDepthPhoto(b);
            } catch (Exception ignored) {
            }*/
        }

        @Override
        public void onViewAttachedToWindow(View view) {
        }

        @Override
        public void onViewDetachedFromWindow(View view) {
        }
    }
}
