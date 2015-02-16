package com.newtpond.testnavdrawer.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.newtpond.testnavdrawer.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GrabListAdapter displays items in the offers' list
 */
final class MainListAdapter extends BaseAdapter implements Filterable {

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private List<DummyContent.DummyItem> mItems = Collections.emptyList();
    private int mAvatarImageViewPixelSize;

    public MainListAdapter(Context context) {
        mContext = context;
        mAvatarImageViewPixelSize = context.getResources().getDimensionPixelSize(R.dimen.user_profile_img_size);
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void updateItems(List<DummyContent.DummyItem> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public DummyContent.DummyItem getItem(int position) {
        return mItems.get(position);

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // this needs to be set to the number of different row layout types
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    // this must return int from 0 to getViewTypeCount-1
    @Override
    public int getItemViewType(int position) {
        DummyContent.DummyItem item = mItems.get(position);
        if(item.getType() == 1) {
            return 0;
        }
        return 1;
    }

    @Override
    public boolean isEnabled(int position) {
        /*if(position == 0) {
            return false;
        }*/
        return true;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                mItems = (List<DummyContent.DummyItem>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                ArrayList<DummyContent.DummyItem> FilteredArray = new ArrayList<DummyContent.DummyItem>();

                // perform your search here using the searchConstraint String.

                constraint = constraint.toString().toLowerCase();
                for (int i = 0; i < getCount(); i++) {
                    DummyContent.DummyItem item = getItem(i);
                    if (item.toString().toLowerCase().startsWith(constraint.toString()))  {
                        FilteredArray.add(item);
                    }
                }

                results.count = FilteredArray.size();
                results.values = FilteredArray;

                return results;
            }
        };

        return filter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);
        DummyContent.DummyItem item = getItem(position);

        if (convertView == null) {
            switch (type) {
                case 1:
                    convertView = mLayoutInflater.inflate(R.layout.moment_list_item, null);
                    break;
                default:
                    convertView = mLayoutInflater.inflate(R.layout.grab_list_item, null);
                    break;
            }
        }

        return convertView;
    }
}
