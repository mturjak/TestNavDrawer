package com.newtpond.testnavdrawer.widget;

/**
 * Profile Drawer menu item
 */
public class ProfileMenuItem {
    private String mItemName;
    private String mItemValue;
    private int mItemType;
    private int mItemNum = 0;

    public ProfileMenuItem(String itemName, String itemValue, int itemNum, int type) {
        mItemName = itemName;
        mItemValue = itemValue;
        mItemNum = itemNum;
        mItemType = type;

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

    public void setItemType(int type) {
        mItemType = type;
    }

    public int getItemType() {
        return mItemType;
    }
}
