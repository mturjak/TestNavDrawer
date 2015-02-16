package com.newtpond.testnavdrawer.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.newtpond.testnavdrawer.MainActivity;
import com.newtpond.testnavdrawer.R;
import com.parse.ParseUser;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    ParseUser mCurrentUser;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_id";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     *
     public static PlaceholderFragment newInstance(int sectionNumber) {
     PlaceholderFragment fragment = new PlaceholderFragment();
     Bundle args = new Bundle();
     args.putInt(ARG_SECTION_NUMBER, sectionNumber);
     fragment.setArguments(args);
     return fragment;
     }*/

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_placeholder, container, false);

        ((TextView) rootView.findViewById(R.id.section_label)).setText("Section " + getArguments().getInt(ARG_SECTION_NUMBER));

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onResume() {
        super.onStart();

        // hide map and divider
        if (((MainActivity)getActivity()).dividerVisible()) {
            ((MainActivity) getActivity()).switchView(1, false);
        }
    }

}