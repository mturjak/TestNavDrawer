package com.newtpond.testnavdrawer.widget;

/**
 * Profile Drawer menu item
 */
public class ProfileMenuItem {
    private String mItemName;
    private String mItemValue;
    private String mItemType;
    private int mItemNum = 0;

    public ProfileMenuItem(String itemName, String itemValue, int itemNum) {
        mItemName = itemName;
        mItemValue = itemValue;
        mItemNum = itemNum;
        mItemType = "item";
    }

    public int getItemNum() {
        return mItemNum;
    }

    public String getItemName() {
        return mItemName;
    }

    public String getItemValue() {
        return mItemValue;
    }

    public void setItemType(String type) {
        mItemType = type;
    }

    public String getItemType() {
        return mItemType;
    }
}
