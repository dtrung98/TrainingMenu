package com.zalo.trainingmenu.mainui.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.model.Item;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuItemHolder> {
    private static final String TAG = "CircleEventTypeAdapter";

    public static final int ICON_MENU_ITEM = 1;
    public static final int MENU_ITEM = 0;


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

        if(viewType==ICON_MENU_ITEM)
         return new IconMenuItemHolder(inflater.inflate(R.layout.item_card_with_icon,parent,false));
        else return new MenuItemHolder(inflater.inflate(R.layout.item_card,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemHolder holder, int position) {
        holder.bind(mData.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        if(getData().get(position).getDrawableRes()==null) return MENU_ITEM;
        return ICON_MENU_ITEM;
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

    public class IconMenuItemHolder extends MenuItemHolder {

        @BindView(R.id.icon)
        ImageView mIcon;

        public IconMenuItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }


        public void bind(Item item) {
            mTitle.setText(item.getTitle());
            mDescription.setVisibility(View.GONE);
            int padding = (int)(mIcon.getContext().getResources().getDimension(R.dimen.oneDp)*item.getDrawablePadding());
            mIcon.setPadding(padding,padding,padding,padding);
            mIcon.setImageResource(item.getDrawableRes());

        }
    }
}
