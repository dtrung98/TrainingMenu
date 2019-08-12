package com.zalo.trainingmenu.downloader.model;

import java.util.ArrayList;

public class TaskList {
    public ArrayList<TaskInfo> getList() {
        return mList;
    }

    public TaskList setList(ArrayList<TaskInfo> list) {
        mList = list;
        return this;
    }

    private ArrayList<TaskInfo> mList;
}
