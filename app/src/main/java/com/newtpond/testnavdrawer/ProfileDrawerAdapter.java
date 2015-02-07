package com.newtpond.testnavdrawer;

import android.content.Context;
import android.graphics.Typeface;
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


    // this needs to be set to the number of different row layout types
    @Override
    public int getViewTypeCount() {
        return 3;
    }

    // TODO: this is just set to accomodate teh dummy list ... acjust for real data
    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getItemType();
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
        String itemText = item.getItemName();
        itemText = itemText.toUpperCase();
        String itemValue = item.getItemValue();
        int type = getItemViewType(position);

        if(type == 0) {
            if (convertView == null)
                convertView = mLayoutInflater.inflate(R.layout.profile_drawer_head, null);

            Transformation transformation = new RoundedTransformationBuilder()
                    .cornerRadiusDp(mAvatarImageViewPixelSize/2)
                    .oval(false)
                    .build();

            Picasso.with(mContext)
                    .load(itemValue)
                    .placeholder(R.drawable.ic_contact_picture)
                    .error(R.drawable.ic_contact_picture)
                    .resize(mAvatarImageViewPixelSize, mAvatarImageViewPixelSize)
                    .transform(transformation)
                    .into((ImageView) convertView.findViewById(R.id.user_profile_img));

        } else {
            if (type == 1) {
                if (convertView == null)
                    convertView = mLayoutInflater.inflate(R.layout.profile_drawer_logout, null);

            } else {
                if (convertView == null)
                    convertView = mLayoutInflater.inflate(R.layout.profile_drawer_item, null);

                int drawableResourceId = mContext.getResources().getIdentifier(itemValue, "drawable", mContext.getPackageName());

                ImageView icon = (ImageView) convertView.findViewById(R.id.drawer_item_logo);

                icon.setImageResource(drawableResourceId);


                TextView counter = (TextView) convertView.findViewById(R.id.drawer_item_num);
                counter.setText(item.getItemNum() + "");

                // TODO: if timestamp newer add unread identifier (make bold or add badge):
                if (position == 4) {
                    counter.setTypeface(counter.getTypeface(), Typeface.BOLD);
                    counter.setTextColor(0xeeee3333);
                }
            }
            if (position % 2 == 1)
                convertView.setBackgroundResource(R.drawable.list_selector_odd);
        }

        ((TextView) convertView.findViewById(R.id.drawer_item_name)).setText(itemText);

        /*((TextView) convertView.findViewById(R.id.user_name)).setText(user.getUsername());*/

        return convertView;
    }
}
