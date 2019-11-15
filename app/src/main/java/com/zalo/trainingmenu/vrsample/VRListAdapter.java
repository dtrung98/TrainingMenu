package com.zalo.trainingmenu.vrsample;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ldt.vrview.VRView;
import com.zalo.trainingmenu.R;

import java.util.ArrayList;
import java.util.List;

import static com.zalo.trainingmenu.vrsample.VrSampleActivity.EXTRA_VR_NEWS_FEED;

public class VRListAdapter extends RecyclerView.Adapter<VRListAdapter.VRListHolder> {
    public VRListAdapter() {

    }

    private static final String TAG = "VRListAdapter";
    private ArrayList<VRNewsFeed> mData = new ArrayList<>();
    public void setData(List<VRNewsFeed> data) {
        mData.clear();
        if(data!=null) mData.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VRListHolder holder) {
        holder.mView.onResume();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VRListHolder holder) {
        holder.mView.onPause();
    }

    @NonNull
    @Override
    public VRListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new VRListHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_vr,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull VRListHolder vrListHolder, int i) {
        vrListHolder.bind(mData.get(i));
    }

    @Override
    public void onViewRecycled(@NonNull VRListHolder holder) {
        holder.mView.setVRPhoto(null);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class VRListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        VRView mView;
        TextView mAuthor;
        TextView mDescription;
        TextView mContent;
        VRListHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView.findViewById(R.id.vr_view);
            mAuthor = itemView.findViewById(R.id.author_text_view);
            mDescription = itemView.findViewById(R.id.description_text_view);
            mContent = itemView.findViewById(R.id.content_text_view);
        }

        public void bind(VRNewsFeed newsFeed) {
            mAuthor.setText(newsFeed.mAuthor);
            mContent.setText(newsFeed.mDescription);
            mView.setViewID(getAdapterPosition());
            mView.setVRPhoto(newsFeed.getVRPhoto());
            mView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(),VrSampleActivity.class);
            intent.setAction(VrSampleActivity.ACTION_VIEW_NEWS_FEED);
            intent.putExtra(EXTRA_VR_NEWS_FEED,mData.get(getAdapterPosition()));
            v.getContext().startActivity(intent);
        }
    }
}
