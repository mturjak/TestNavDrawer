package com.newtpond.testnavdrawer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.newtpond.testnavdrawer.fragments.EditProfileActivity;
import com.newtpond.testnavdrawer.fragments.EditProfileFragment;
import com.newtpond.testnavdrawer.fragments.MainFragment;
import com.newtpond.testnavdrawer.fragments.PlaceholderFragment;
import com.newtpond.testnavdrawer.fragments.UsersListFragment;
import com.parse.ParseUser;

import java.text.DateFormat;
import java.util.Date;

import static com.newtpond.testnavdrawer.utils.NetworkAvailable.isNetworkAvailable;
import static com.newtpond.testnavdrawer.utils.NetworkAvailable.noNetworkAlert;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, View.OnTouchListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

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

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private String mLastUpdateTime;

    private final String REQUESTING_LOCATION_UPDATES_KEY = "request_location_updates";
    private final String LOCATION_KEY = "last_location";
    private final String LAST_UPDATED_TIME_STRING_KEY = "last_updated_time";

    private boolean mRequestingLocationUpdates = true;

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

        updateValuesFromBundle(savedInstanceState);

        if(mGoogleApiClient == null) {
            buildGoogleApiClient();
        }

        createLocationRequest();

        if (findViewById(R.id.main_list_map) != null) {
            mMapView = (MapView) findViewById(R.id.main_list_map);
            mMapView.onCreate(savedInstanceState);

            mContainer = (FrameLayout)findViewById(R.id.container);
            mDivider = (RelativeLayout)findViewById(R.id.layout_draggable);
            mMapSegment= (FrameLayout)findViewById(R.id.map_segment);

            mDivider.setOnTouchListener(this);

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

        if(!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();

        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        if(mMapView != null)
            mMapView.onResume();

        if(mDivider != null) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            mHeight = displaymetrics.heightPixels;

            Log.e("height", "" + mHeight);
        }

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

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
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

        if(mMapView != null)
            mMapView.onSaveInstanceState(b);

        b.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
            mRequestingLocationUpdates);

        b.putParcelable(LOCATION_KEY, mLastLocation);
        b.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);

        super.onSaveInstanceState(b);

    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMapView != null && mMap == null) {

            mMap = mMapView.getMap();

            if(mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        /*LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);*/

        if (mLastLocation != null) {
            double lat = mLastLocation.getLatitude();
            double lng = mLastLocation.getLongitude();
            LatLng coordinate = new LatLng(lat, lng);

            MarkerOptions options = new MarkerOptions()
                    .position(coordinate)
                    .title("I am here!");

            CameraUpdate center = CameraUpdateFactory.newLatLng(coordinate);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(13);

            mMap.addMarker(options);

            mMap.moveCamera(center);
            mMap.animateCamera(zoom);
        }
    }


    /**
     *
     * Next part includes mostly google api client and location methods
     *
     *
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        Log.e("googleApi", mGoogleApiClient.toString());
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if(mLastLocation == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            setUpMap();
        }

    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    // called from onPause method
    /*protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }*/
    // we disconected the client instead onPause

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and
            // make sure that the Start Updates and Stop Updates buttons are
            // correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }

            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocationis not null.
                mLastLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(
                        LAST_UPDATED_TIME_STRING_KEY);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("GoogleApi","connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("GoogleApi","connection failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        setUpMap();
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
        if(mDivider != null)
            return mDivider.getHeight() > 0;

        return true;
    }

    public void makeDividerVisibile(boolean v) {
        if(mDivider != null) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mDivider.getLayoutParams();
            if (v) {
                params.height = 20;
            } else {
                params.height = 0;
            }
            mDivider.setLayoutParams(params);
        }
    }

    public void switchView(float bottomWeight, boolean showDivider) {
        if(mDivider != null) {
            if (showDivider && !dividerVisible()) {
                makeDividerVisibile(true);
            } else if (!showDivider && dividerVisible()) {
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
}