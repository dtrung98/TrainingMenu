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
import com.zalo.trainingmenu.R;

import java.util.ArrayList;
import java.util.List;

public class VRListAdapter extends RecyclerView.Adapter<VRListAdapter.VRListHolder> {
    public VRListAdapter() {

    }
    private static final String TAG = "VRListAdapter";
    private ArrayList<Object> mData = new ArrayList<>();
    public void setData(List<Object> data) {
        mData.clear();
        if(data!=null) mData.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VRListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new VRListHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_vr,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull VRListHolder vrListHolder, int i) {
        vrListHolder.bind();
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class VRListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        VRView mView;
        VRListHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView.findViewById(R.id.vr_view);
            mView.setOnClickListener(this);
           mView.onResume();
        }

        public void bind() {
        }

        @Override
        public void onClick(View v) {
            mView.recalibrate();
        }
    }
}
