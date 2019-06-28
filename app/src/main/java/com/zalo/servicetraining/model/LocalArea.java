package com.zalo.servicetraining.model;

public class LocalArea {
private String ID = "";
private String LocalizedName = "";
private String EnglishName = "";
private int Level = 0;
private String LocalizedType ="";
private String EnglishType = "";
private String CountryID = "";



    public String getEnglishType() {
        return EnglishType;
    }

    public void setEnglishType(String englishType) {
        EnglishType = englishType;
    }


    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getLocalizedName() {
        return LocalizedName;
    }

    public void setLocalizedName(String localizedName) {
        LocalizedName = localizedName;
    }

    public String getEnglishName() {
        return EnglishName;
    }

    public void setEnglishName(String englishName) {
        EnglishName = englishName;
    }

    public int getLevel() {
        return Level;
    }

    public void setLevel(int level) {
        Level = level;
    }

    public String getLocalizedType() {
        return LocalizedType;
    }

    public void setLocalizedType(String localizedType) {
        LocalizedType = localizedType;
    }

    public String getCountryID() {
        return CountryID;
    }

    public void setCountryID(String countryID) {
        CountryID = countryID;
    }
}
