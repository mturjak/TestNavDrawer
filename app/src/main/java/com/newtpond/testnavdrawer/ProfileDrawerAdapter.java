package com.newtpond.testnavdrawer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.newtpond.testnavdrawer.widget.ProfileMenuItem;

import java.util.Collections;
import java.util.List;

/**
 * ProfileDrawerAdapter helps us construct the side drawer profile list
 */
final class ProfileDrawerAdapter extends BaseAdapter {

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private List<ProfileMenuItem> mItems = Collections.emptyList();

    public ProfileDrawerAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public ProfileDrawerAdapter(Context context, List<ProfileMenuItem> items) {
        mContext = context;
        mItems = items;
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
    public View getView(int position, View convertView, ViewGroup parent) {

        ProfileMenuItem item = getItem(position);

        if (convertView == null) {
            if(position == 0) {
                convertView = mLayoutInflater.inflate(R.layout.profile_drawer_head, null);
            } else if(item.getItemName() == "item") {
                convertView = mLayoutInflater.inflate(R.layout.profile_drawer_item, null);
            }
        }

        /*String gravatarUrl = Gravatar.init()
                .with(user.getEmail())
                .force404()
                .size(mAvatarImageViewPixelSize)
                .build();

        Transformation transformation = new RoundedTransformationBuilder()
                .cornerRadiusDp(mAvatarImageViewPixelSize/2)
                .oval(false)
                .build();

        Picasso.with(mContext)
                .load(gravatarUrl)
                .placeholder(R.drawable.ic_contact_picture)
                .error(R.drawable.ic_contact_picture)
                .transform(transformation)
                .into((ImageView) convertView.findViewById(R.id.user_avatar));

        ((TextView) convertView.findViewById(R.id.user_name)).setText(user.getUsername());*/

        return convertView;
    }
}
