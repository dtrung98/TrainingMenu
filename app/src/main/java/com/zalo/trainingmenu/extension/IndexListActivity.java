package com.zalo.trainingmenu.extension;

import com.ldt.menulayout.model.Item;
import com.ldt.menulayout.ui.AbsMenuActivity;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.util.Navigator;

import java.util.ArrayList;
import java.util.List;

public class IndexListActivity extends AbsMenuActivity {
    @Override
    protected List<Item> onRefreshDataList() {
        ArrayList<Item> list = new ArrayList<>();
        ArrayList<Integer> ids = Navigator.getInstance().getAllActivityIds();
        for (int id :
                ids) {
            list.add(Item.with(this).setTitle(id).setDestinationActivityClass(Navigator.getInstance().findActivityById(id)).get());
        }

        return list;
    }

    @Override
    protected int title() {
        return R.string.index_list;
    }
}
