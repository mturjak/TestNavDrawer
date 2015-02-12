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
public class MomentFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "section_id";

    private boolean mTwoPane = false;

    public MomentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_main_list, container, false);

        ListView mainList = (ListView)rootView.findViewById(android.R.id.list);
        MomentListAdapter adapter = new MomentListAdapter<DummyContent.DummyItem>(getActivity());
        adapter.updateItems(DummyContent.ITEMS);
        mainList.setAdapter(adapter);

        mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if(mTwoPane) {
                    Fragment fragment = getFragment(position);

                    // add position as argument
                    Bundle arguments = new Bundle();
                    arguments.putInt(MomentFragment.ARG_SECTION_NUMBER, position);
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

        if (rootView.findViewById(R.id.blogpost_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
           /*((MainListFragment) getChildFragmentManager()
                    .findFragmentById(R.id.blogpost_list))
                    .setActivateOnItemClick(true);*/
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

    /*
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(MainDetailFragment.ARG_ITEM_ID, id);
            MainDetailFragment fragment = new MainDetailFragment();
            fragment.setArguments(arguments);
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.blogpost_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, BlogPostDetailActivity.class);
            detailIntent.putExtra(MainDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
    */
}
