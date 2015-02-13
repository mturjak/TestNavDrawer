package com.newtpond.testnavdrawer.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.RoundedTransformationBuilder;
import com.newtpond.testnavdrawer.R;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Collections;
import java.util.List;

import fr.tkeunebr.gravatar.Gravatar;

final class GravatarAdapter extends BaseAdapter {
  private final String mStyle;
  private final Context mContext;
  private final LayoutInflater mLayoutInflater;
  private final int mAvatarImageViewPixelSize;
  private List<ParseUser> mUsers = Collections.emptyList();

  public GravatarAdapter(Context context, String style) {
    mContext = context;
    mStyle = style;
    mLayoutInflater = LayoutInflater.from(context);
    mAvatarImageViewPixelSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_image_view_size);
  }

  public void updateUsers(List<ParseUser> users) {
    mUsers = users;
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return mUsers.size();
  }

  @Override
  public ParseUser getItem(int position) {
    return mUsers.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
        if(mStyle == "item") {
            convertView = mLayoutInflater.inflate(R.layout.user_list_item, null);
        } else if(mStyle == "item_checkbox") {
            convertView = mLayoutInflater.inflate(R.layout.user_list_item_checkbox, null);
        }
    }

    ParseUser user = getItem(position);

    String gravatarUrl = Gravatar.init()
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

    ((TextView) convertView.findViewById(R.id.user_name)).setText(user.getUsername());

    return convertView;
  }
}
