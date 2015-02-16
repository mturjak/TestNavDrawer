package com.newtpond.testnavdrawer.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.newtpond.testnavdrawer.MainActivity;
import com.newtpond.testnavdrawer.R;
import com.newtpond.testnavdrawer.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class UsersListFragment extends ListFragment {

    public static final String TAG = UsersListFragment.class.getSimpleName();

    protected List<ParseUser> mFriends;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;

    private static final String ARG_SECTION_NUMBER = "section_id";

    public UsersListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // hide map and divider
        if (((MainActivity)getActivity()).dividerVisible()) {
            ((MainActivity) getActivity()).switchView(1, false);
        }

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        //((ActionBarActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(true);

        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                //((ActionBarActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(false);

                if (e == null) {
                    mFriends = friends;

                    GravatarAdapter adapter = new GravatarAdapter(getListView().getContext(), "item");
                    adapter.updateUsers(mFriends);

                    setListAdapter(adapter);


                } else {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(getListView().getContext());
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.edit_friends_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent intent = new Intent(getListView().getContext(), BlogPostDetailActivity.class);
        intent.putExtra(ParseConstants.KEY_USERNAME, mFriends.get(position).getUsername());
        startActivity(intent);
    }
}
