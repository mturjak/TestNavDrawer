package com.newtpond.testnavdrawer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.newtpond.testnavdrawer.fragments.EditProfileActivity;
import com.newtpond.testnavdrawer.fragments.EditProfileFragment;
import com.newtpond.testnavdrawer.fragments.MainFragment;
import com.newtpond.testnavdrawer.fragments.PlaceholderFragment;
import com.newtpond.testnavdrawer.fragments.UsersListFragment;
import com.parse.ParseUser;

import static com.newtpond.testnavdrawer.utils.NetworkAvailable.isNetworkAvailable;
import static com.newtpond.testnavdrawer.utils.NetworkAvailable.noNetworkAlert;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, View.OnTouchListener {

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

    private MapView mMapView;
    private GoogleMap mMap;

    private FrameLayout mContainer;
    private RelativeLayout mDivider;
    private FrameLayout mMapSegment;
    private float mWeightFactor = 0;
    private float mBottomWeight = 0.7f;
    private int mDividerTop;
    private int mHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.main_list_map) != null) {
            mMapView = (MapView) findViewById(R.id.main_list_map);
            mMapView.onCreate(savedInstanceState);

            mContainer = (FrameLayout)findViewById(R.id.container);
            mDivider = (RelativeLayout)findViewById(R.id.layout_draggable);
            mMapSegment= (FrameLayout)findViewById(R.id.map_segment);

            findViewById(R.id.layout_draggable).setOnTouchListener(this);

            if(mMap == null) {
                MapsInitializer.initialize(this);
            }

            //mMap = mMapView.getMap();

            /*mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.main_list_map);

            mMapFragment.getMapAsync(this);*/
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

        mTitle = getTitle();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

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

    @Override
    protected void onResume() {
        super.onResume();

        if(mMapView != null)
            mMapView.onResume();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mHeight = displaymetrics.heightPixels;

        Log.e("height", "" + mHeight);

        setUpMapIfNeeded();

        /*if(mDrawerLayout != null) {
            // refresh adapter on resume activity
            ((ProfileDrawerAdapter) ((ListView) mDrawerLayout.findViewById(android.R.id.list))
                    .getAdapter()).notifyDataSetChanged();
        }*/
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
                    fragment = new MainFragment();
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
        if(mCurrentSection != 0) {
            ((ListView)mDrawerLayout.findViewById(android.R.id.list)).setItemChecked(0, true);
            onNavigationDrawerItemSelected(0);
            mCurrentSection = 0;
        } else if (mNavigationDrawerFragment.isDrawerOpen() && !mIsDrawerLocked) {
            mDrawerLayout.closeDrawer(Gravity.START);
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
                    .replace(R.id.container, new EditProfileFragment()) // TODO: use instance instead of instantiating again
                    .commit();
        } else {
            navigateTo(EditProfileActivity.class, false);
        }
    }

    public int getCurrentSection() {
        return mCurrentSection;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mMapView != null)
            mMapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if(mMapView != null)
            mMapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mMapView != null)
            mMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);

        if(mMapView != null)
            mMapView.onSaveInstanceState(b);

        // Do your save instance
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMapView!= null && mMap == null) {

            mMap = mMapView.getMap();

            if(mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
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

    /**
     * Draggable screen divider touch listener
     */
    @Override
    public boolean onTouch(View view, MotionEvent event) {

        final int Y = (int) event.getRawY();
        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) mContainer.getLayoutParams();
        LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) mMapSegment.getLayoutParams();

        int delta = 0;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                view.setBackgroundColor(0xff555555);
                // save starting position
                mDividerTop = Y;

                // calculate weight factor
                if (params1.weight > 0) {
                    mWeightFactor = params1.weight / (mHeight - Y);
                }
                break;
            case MotionEvent.ACTION_UP:
                view.setBackgroundColor(0xffeeeeee);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                // delta against previous position
                delta = Y - mDividerTop;

                if (delta > 0 && (params1.weight / mWeightFactor <= 40 ||
                    params1.weight / mWeightFactor <= delta)) {
                    switchView(0, true);
                } else if(delta < 0 && (params2.weight < 0.09 ||
                        params2.weight <= -1 * delta * mWeightFactor)) {
                    switchView(1, true);
                } else {
                    // adjust the weights
                    params1.weight = params1.weight - delta * mWeightFactor;
                    params2.weight = 1 - params1.weight;
                    mContainer.setLayoutParams(params1);
                    mMapSegment.setLayoutParams(params2);

                    // save new position
                    mDividerTop = Y;
                }
                break;
        }

        mBottomWeight = params1.weight;
        return true;
    }

    public float getBottomWeight() {
        return mBottomWeight;
    }

    public boolean dividerVisible() {
        return mDivider.getHeight() > 0;
    }

    public void makeDividerVisibile(boolean v) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mDivider.getLayoutParams();
        if(v) {
            params.height = 20;
        } else {
            params.height = 0;
        }
        mDivider.setLayoutParams(params);
    }

    public void switchView(float bottomWeight, boolean showDivider) {
        if(showDivider && !dividerVisible()) {
            makeDividerVisibile(true);
        } else if(!showDivider && dividerVisible()) {
            makeDividerVisibile(false);
        }

        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) mContainer.getLayoutParams();
        LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) mMapSegment.getLayoutParams();

        params1.weight = bottomWeight;
        params2.weight = 1 - bottomWeight;

        mWeightFactor = 0.003f;
        mContainer.setLayoutParams(params1);
        mMapSegment.setLayoutParams(params2);
    }
}