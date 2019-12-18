package com.zalo.trainingmenu.downloader.ui.detail;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.ldt.menulayout.ui.MenuAdapter;
import com.zalo.trainingmenu.R;

public class DetailAdapter extends MenuAdapter {
    @NonNull
    @Override
    public MenuItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if(viewType==ICON_MENU_ITEM)
            return new IconMenuItemHolder(inflater.inflate(R.layout.item_card_small_with_icon,parent,false),this);
        else return new MenuItemHolder(inflater.inflate(R.layout.item_card_for_field,parent,false));

    }

    private GridLayoutManager.SpanSizeLookup mSpanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
        @Override
        public int getSpanSize(int position) {
            int type = getItemViewType(position);
            if(type == ICON_MENU_ITEM) return 2;
            else if(type== MENU_ITEM) return 6;
            else return 6;
        }
    };

    public GridLayoutManager.SpanSizeLookup getSpanSizeLookup() {
        return mSpanSizeLookup;
    }

    public int getSpanCount() {
        return 6;
    }
}
