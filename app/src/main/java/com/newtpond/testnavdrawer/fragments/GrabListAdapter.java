package com.newtpond.testnavdrawer.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.RoundedTransformationBuilder;
import com.newtpond.testnavdrawer.R;
import com.newtpond.testnavdrawer.widget.ProfileMenuItem;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Collections;
import java.util.List;

/**
 * GrabListAdapter displays items in the offers' list
 */
final class GrabListAdapter<T> extends BaseAdapter {

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private List<T> mItems = Collections.emptyList();
    private int mAvatarImageViewPixelSize;

    public GrabListAdapter(Context context) {
        mContext = context;
        mAvatarImageViewPixelSize = context.getResources().getDimensionPixelSize(R.dimen.user_profile_img_size);
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void updateItems(List<T> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        /*if(position == 0) {
            return false;
        }*/
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.grab_list_item, null);
        }

        if (position % 2 == 0) {
            convertView.setBackgroundColor(0x00ffffff);
        } else {
            convertView.setBackgroundColor(0x99ffffff);
        }

        return convertView;
    }
}
