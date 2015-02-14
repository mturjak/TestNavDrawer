package com.newtpond.testnavdrawer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.newtpond.testnavdrawer.fragments.EditUserActivity;
import com.newtpond.testnavdrawer.fragments.EditUserFragment;
import com.newtpond.testnavdrawer.fragments.MainFragment;
import com.newtpond.testnavdrawer.fragments.MainWithMapFragment;
import com.newtpond.testnavdrawer.fragments.PlaceholderFragment;
import com.newtpond.testnavdrawer.fragments.UsersListFragment;
import com.parse.ParseUser;

import static com.newtpond.testnavdrawer.utils.NetworkAvailable.isNetworkAvailable;
import static com.newtpond.testnavdrawer.utils.NetworkAvailable.noNetworkAlert;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, OnMapReadyCallback {

    private boolean mIsDrawerLocked = false;
    private DrawerLayout mDrawerLayout;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;


    /**
     * Section fragments
     */
    private int mCurrentSection = -1;
    private Fragment mMainFragment;
    private Fragment mGrabFragment;
    private Fragment mMomentFragment;
    private Fragment mFriendsFragment;

    private View mMapView;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.main_list_map) != null) {
            mMapView = findViewById(R.id.main_list_map);
            mMap = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.main_list_map))
                    .getMap();

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double lat =  location.getLatitude();
            double lng = location.getLongitude();
            LatLng coordinate = new LatLng(lat, lng);

            CameraUpdate center = CameraUpdateFactory.newLatLng(coordinate);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(13);

            mMap.moveCamera(center);
            mMap.animateCamera(zoom);
        }

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // do stuff with the user
            // Log.i(TAG, currentUser.getUsername());

            // check if network available
            if(!isNetworkAvailable(this)) {
                noNetworkAlert(this);
            }
        } else {
            navigateTo(LoginActivity.class, true);
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = getTitle();

        // drawer_content_padding is set to 0 on handsets and to 240 on tablets,
        // so we can use it as a switch for locking the drawer
        mIsDrawerLocked = getResources().getDimensionPixelSize(R.dimen.drawer_content_padding) > 0;

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                mDrawerLayout,
                mIsDrawerLocked);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void navigateTo(final Class<? extends Activity> activityClass, boolean clearTask) {
        Intent intent = new Intent(this, activityClass);
        if(clearTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        startActivity(intent);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if(mCurrentSection == -1 || position != mCurrentSection) {
            mCurrentSection = position;
            // update the main content by replacing fragments
            Fragment fragment = getFragment();

            // add position as argument
            Bundle arguments = new Bundle();
            arguments.putInt(MainFragment.ARG_SECTION_NUMBER, position);
            fragment.setArguments(arguments);

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    /**
     * getFragment
     * @return Fragment based on current section
     */
    private Fragment getFragment() { // TODO: make more DRY
        Fragment fragment;
        switch (mCurrentSection) {
            case 0:
                if(mMainFragment != null) {
                    return mMainFragment;
                } else {
                    if(getResources().getDimensionPixelSize(R.dimen.drawer_content_padding) == 0) {
                        fragment = new MainWithMapFragment();
                    } else {
                        fragment = new MainFragment();
                    }
                    mMainFragment = fragment;
                }
                break;
            case 1:
                if(mGrabFragment != null) {
                    return mGrabFragment;
                } else {
                    fragment = new MainFragment();
                    mGrabFragment = fragment;
                }
                break;
            case 2:
                if(mMomentFragment != null) {
                    return mMomentFragment;
                } else {
                    fragment = new MainFragment();
                    mMomentFragment = fragment;
                }
                break;
            case 3:
                if(mFriendsFragment != null) {
                    return mFriendsFragment;
                } else {
                    fragment = new UsersListFragment();
                    mFriendsFragment = fragment;
                }
                break;
            default:
                fragment = new PlaceholderFragment(); // TODO: better not to instantiate every time
                break;
        }
        return fragment;
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                mTitle = getString(R.string.app_name);
                break;
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                break;
            case 6:
                ParseUser.logOut();
                navigateTo(LoginActivity.class, true);
                break;
        }

        if(mIsDrawerLocked) {
            restoreActionBar();
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen() || mIsDrawerLocked) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (!mNavigationDrawerFragment.isDrawerOpen() && !mIsDrawerLocked && id == R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(!mNavigationDrawerFragment.isDrawerOpen() && !mIsDrawerLocked) {
            mDrawerLayout.openDrawer(Gravity.START);
        } else {
            MainActivity.super.onBackPressed();
        }
    }

    public void editUser() {
        if(mIsDrawerLocked) {

            // clear drawer selection
            mCurrentSection = 0;
            ListView drawerList = (ListView)mDrawerLayout.findViewById(android.R.id.list);
            drawerList.clearChoices();
            ((ProfileDrawerAdapter)drawerList.getAdapter()).notifyDataSetChanged();

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new EditUserFragment()) // TODO: use instance instead of instantiating again
                    .commit();
        } else {
            navigateTo(EditUserActivity.class, false);
        }
    }

    public int getCurrentSection() {
        return mCurrentSection;
    }

    public void setMapVisibility(int visibility) {
        if(mMapView != null && mMapView.getVisibility() != visibility) {
            mMapView.setVisibility(visibility);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}