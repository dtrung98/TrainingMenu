package com.zalo.trainingmenu.extension;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ldt.menulayout.model.Item;
import com.ldt.menulayout.ui.AbsListActivity;
import com.ldt.menulayout.ui.OnItemClickListener;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.util.Navigator;

import java.util.ArrayList;

public class SetLauncherActivity extends AbsListActivity implements OnItemClickListener {

    private ImageView mIconImageView;
    private SetLauncherAdapter mAdapter;

    @Override
    protected int title() {
        return R.string.set_launcher_actitivy;
    }

    @Override
    protected int contentLayout() {
        return com.ldt.menulayout.R.layout.motion_list_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSwipeRefreshLayout().setEnabled(false);
    }

    @Override
    protected void onInitRecyclerView() {
        mIconImageView = findViewById(R.id.icon);
        mIconImageView.setVisibility(View.VISIBLE);
        mIconImageView.setImageResource(R.drawable.tick);
        mAdapter = new SetLauncherAdapter();
        mAdapter.setListener(this);
        getRecyclerView().setAdapter(mAdapter);
        getRecyclerView().setLayoutManager(new LinearLayoutManager(this));
        getSwipeRefreshLayout().setRefreshing(false);
    }

    @Override
    protected void refreshData() {
        ArrayList<Item> list = new ArrayList<>();
        ArrayList<Integer> ids = Navigator.getInstance().getAllActivityIds();
        Class<? extends AppCompatActivity> cls = Navigator.getInstance().getLaunchingActivityCls(this);
        Class<? extends AppCompatActivity> c;
        int tick = -1;
        for (int i = 0; i< ids.size(); i++) {
            c = Navigator.getInstance().findActivityById(ids.get(i));
            list.add(Item.with(this).setTitle(ids.get(i)).setDescription("ID : "+ids.get(i)).setDestinationActivityClass(c).setTag(ids.get(i)).get());
            if(c.equals(cls)) tick = i;
        }
        if(tick>0) {
            list.add(0, list.remove(tick));
            tick = 0;
        }

        mAdapter.setData(list);
        mAdapter.setCurrentTickValue(tick);
        super.refreshData();
    }

    @Override
    public void onEventItemClick(Item item, int position) {
        if(item.getTag() instanceof Integer)
        Navigator.getInstance().setLaunchingActivityCls(this, (Integer) item.getTag());
        mAdapter.setCurrentTickValue(position);

    }
}
