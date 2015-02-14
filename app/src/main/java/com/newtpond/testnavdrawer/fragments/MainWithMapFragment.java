package com.newtpond.testnavdrawer.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.newtpond.testnavdrawer.MainActivity;
import com.newtpond.testnavdrawer.R;

/**
 * A list fragment representing a list of BlogPosts. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link /BlogPostDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link /Callbacks}
 * interface.
 */
public class MainWithMapFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "section_id";

    private boolean mTwoPane = false;

    public MainWithMapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_main_list_w_map, container, false);

        if (rootView.findViewById(R.id.blogpost_detail_container) != null) {
            mTwoPane = true;
        }

        return rootView;
    }

    private Fragment getFragment(int position) {
        Fragment fragment;

        fragment = new MainDetailFragment(); // TODO: better not to instantiate every time

        return fragment;
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
        ((MainActivity) getActivity()).setMapVisibility(View.VISIBLE);

        View rootView = getView();

        ListView mainList = (ListView)rootView.findViewById(android.R.id.list);

        MainListAdapter adapter = new MainListAdapter(getActivity(),true);

        adapter.updateItems(DummyContent.ITEMS);

        mainList.setAdapter(adapter);

        mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if(mTwoPane) {
                    Fragment fragment = getFragment(position);

                    // add position as argument
                    Bundle arguments = new Bundle();
                    arguments.putInt(MainWithMapFragment.ARG_SECTION_NUMBER, position);
                    fragment.setArguments(arguments);

                    FragmentManager fragmentManager = getChildFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.blogpost_detail_container, fragment)
                            .commit();
                } else {
                    Intent detailIntent = new Intent(parent.getContext(), BlogPostDetailActivity.class);
                    detailIntent.putExtra(MainDetailFragment.ARG_ITEM_ID, position);
                    startActivity(detailIntent);
                }
            }
        });
    }
}
