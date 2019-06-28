package com.zalo.servicetraining.model;

public class Item {
    private String mTitle = "";
    private String mDescription = "";

    public Item() {
    }

    public Item(String mTitle, String mDescription) {
        this.mTitle = mTitle;
        this.mDescription = mDescription;
    }

    public String getTitle() {
        return mTitle;
    }

    public Item setTitle(String mTitle) {
        this.mTitle = mTitle;
        return this;
    }

    public String getDescription() {
        return mDescription;
    }

    public Item setDescription(String mDescription) {
        this.mDescription = mDescription;
        return this;
    }
}
