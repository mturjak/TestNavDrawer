package com.newtpond.testnavdrawer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.RoundedTransformationBuilder;
import com.newtpond.testnavdrawer.widget.ProfileMenuItem;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Collections;
import java.util.List;

import fr.tkeunebr.gravatar.Gravatar;

/**
 * ProfileDrawerAdapter helps us construct the side drawer profile list
 */
final class ProfileDrawerAdapter extends BaseAdapter {

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private List<ProfileMenuItem> mItems = Collections.emptyList();
    private int mAvatarImageViewPixelSize;

    public ProfileDrawerAdapter(Context context) {
        mContext = context;
        mAvatarImageViewPixelSize = context.getResources().getDimensionPixelSize(R.dimen.user_profile_img_size);
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void updateItems(List<ProfileMenuItem> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public ProfileMenuItem getItem(int position) {
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

        ProfileMenuItem item = getItem(position);
        String itemText = item.getItemValue();
        itemText = itemText.toUpperCase();

        if(position == 0) {
            if (convertView == null) { convertView = mLayoutInflater.inflate(R.layout.profile_drawer_head, null); }
            String gravatarUrl = Gravatar.init()
                    .with(item.getEmail())
                    .force404()
                    .size(mAvatarImageViewPixelSize)
                    .build();

            Transformation transformation = new RoundedTransformationBuilder()
                    .cornerRadiusDp(mAvatarImageViewPixelSize/2)
                    .oval(false)
                    .build();

            Picasso.with(mContext)
                    .load(gravatarUrl)
                    .placeholder(R.drawable.ic_launcher)
                    .error(R.drawable.ic_launcher)
                    .transform(transformation)
                    .into((ImageView) convertView.findViewById(R.id.user_profile_img));

        } else {
            if (convertView == null) { convertView = mLayoutInflater.inflate(R.layout.profile_drawer_item, null); }
            if (position % 2 == 1) { convertView.setBackgroundResource(R.drawable.list_selector_odd); }
        }

        ((TextView) convertView.findViewById(R.id.user_name)).setText(itemText);

        /*((TextView) convertView.findViewById(R.id.user_name)).setText(user.getUsername());*/

        return convertView;
    }
}
