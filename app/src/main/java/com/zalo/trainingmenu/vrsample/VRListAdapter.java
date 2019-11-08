package com.zalo.trainingmenu.vrsample;

import android.opengl.GLSurfaceView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ldt.vrview.GLTextureView;
import com.ldt.vrview.VRControlView;
import com.ldt.vrview.VRView;
import com.ldt.vrview.model.VRPhoto;
import com.zalo.trainingmenu.R;

import java.util.ArrayList;
import java.util.List;

public class VRListAdapter extends RecyclerView.Adapter<VRListAdapter.VRListHolder> {
    public VRListAdapter() {

    }
    private static final String TAG = "VRListAdapter";
    private ArrayList<VRPhoto> mData = new ArrayList<>();
    public void setData(List<VRPhoto> data) {
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
        if(i<mData.size())
        vrListHolder.bind(mData.get(i));
        else vrListHolder.bind(null);
    }

    @Override
    public void onViewRecycled(@NonNull VRListHolder holder) {
        holder.mView.setVRPhoto(null);
    }

    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }

    public class VRListHolder extends RecyclerView.ViewHolder {
        VRView mView;
        VRListHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView.findViewById(R.id.vr_view);

        }

        public void bind(VRPhoto photo) {
            mView.setViewID(getAdapterPosition());
          mView.mSampleVRPhotos.clear();
            mView.mSampleVRPhotos.addAll(mData);
            mView.curSamplePos = getAdapterPosition();
            mView.setVRPhoto(photo);
        }
    }
}
