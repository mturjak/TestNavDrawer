package com.newtpond.testnavdrawer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.newtpond.testnavdrawer.fragments.EditProfileActivity;
import com.newtpond.testnavdrawer.fragments.EditProfileFragment;
import com.newtpond.testnavdrawer.fragments.MainFragment;
import com.newtpond.testnavdrawer.fragments.PlaceholderFragment;
import com.newtpond.testnavdrawer.fragments.UsersListFragment;
import com.newtpond.testnavdrawer.utils.LocationProvider;
import com.newtpond.testnavdrawer.utils.marker.clusterer.Moment;
import com.newtpond.testnavdrawer.utils.marker.clusterer.MyClusterRenderer;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;

import java.util.List;

import static android.support.v4.view.ViewCompat.canScrollVertically;
import static com.newtpond.testnavdrawer.utils.NetworkAvailable.isNetworkAvailable;
import static com.newtpond.testnavdrawer.utils.NetworkAvailable.noNetworkAlert;
import static com.parse.ParseObject.pinAllInBackground;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, View.OnTouchListener, LocationProvider.LocationCallback {


    public static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Navigation drawer properties
     */
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
     * Section fragments and current section position
     */
    private int mCurrentSection = -1;
    private Fragment mMainFragment;
    private Fragment mGrabFragment;
    private Fragment mMomentFragment;
    private Fragment mFriendsFragment;

    /**
     * Google API Client properties
     */
    private Location mLastLocation;
    private String mLastUpdateTime;

    // keys
    private final static String LOCATION_KEY = "last_location";
    private final static String LAST_UPDATED_TIME_STRING_KEY = "last_updated_time";
    private final static String BOTTOM_WEIGHT_STRING_KEY = "bottom_weight";
    private final static String CURRENT_SECTION_KEY = "current_section";

    /**
     * Google Map properties
     */
    private MapView mMapView;
    private GoogleMap mMap;
    private LocationProvider mLocationProvider;
    private ClusterManager<Moment> mClusterManager;
    private boolean mRepositionMap = true;

    /**
     * properties related to Map/List view
     * with draggable divider
     */
    private FrameLayout mContainer;
    private FrameLayout mMapSegment;
    private RelativeLayout mDivider;
    private float mWeightFactor = 0;
    private float mBottomWeight = 0.7f;
    private int mDividerTop;
    private int mHeight;

    private RelativeLayout mLoaderView;
    private LinearLayout mMainView;

    /**
     * Parse data objects
     */
    protected ParseUser mCurrentUser;
    protected ParseObject mCurrentProfile;

    protected List<ParseObject> mFriends;
    protected List<ParseObject> mClosestUsers;
    protected List<Moment> mClosestPoints;
    protected List<ParseObject> mClosestVendors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get current user
        mCurrentUser = ParseUser.getCurrentUser();
        if (mCurrentUser != null) {
            // do stuff with the user
            // Log.i(TAG, currentUser.getUsername());

            // check if network available
            if(!isNetworkAvailable(this)) {
                noNetworkAlert(this);
            }
        } else {
            navigateTo(LoginActivity.class, true);
        }

        // loads saved values
        updateValuesFromBundle(savedInstanceState);

        // checking if layout includes a map view
        if (findViewById(R.id.main_list_map) != null) {
            mMapView = (MapView) findViewById(R.id.main_list_map);
            mMapView.onCreate(savedInstanceState);

            // loader and actual content views
            mMainView = (LinearLayout)findViewById(R.id.layout_divided);
            mLoaderView = (RelativeLayout)findViewById(R.id.loadingMain);

            // get elements of the divided layout
            mContainer = (FrameLayout)findViewById(R.id.container);
            mDivider = (RelativeLayout)findViewById(R.id.layout_draggable);
            mMapSegment= (FrameLayout)findViewById(R.id.map_segment);

            // make divider draggable
            mDivider.setOnTouchListener(this);

            // because using MapView instead of fragment we may need to initialize maps manually
            if(mMap == null) {
                MapsInitializer.initialize(this);
            }

            // set up map if location from saved data
            // setUpMapIfNeeded();
            if(mMapView != null)
                mLocationProvider = new LocationProvider(this, this);

            ActionBar actionBar = getSupportActionBar();
            BitmapDrawable background = new BitmapDrawable (BitmapFactory.decodeResource(getResources(), R.drawable.topbg));
            background.setTileModeX(Shader.TileMode.REPEAT);
            actionBar.setBackgroundDrawable(background);
            actionBar.setLogo(R.drawable.ic_vikler);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayUseLogoEnabled(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.vikler_material_dark));
            }
        }

        // action bar title
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

        if(mBottomWeight != 0.7f) {
            // this means the map was retrieved from saved state, so we will not reposition it
            mRepositionMap = false;

            if(mCurrentSection < 3)
                switchView(mBottomWeight, true);
        }

        if(mDivider != null) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

            // display height in dp
            mHeight = (int)(displaymetrics.heightPixels / Resources.getSystem().getDisplayMetrics().density);
        }

        // connect to the google location client & set up map if required and
        if(mMapView != null)
            mLocationProvider.connect();

        setUpMapIfNeeded();

        // TODO: update the drawer values
        /*if(mDrawerLayout != null) {
            // refresh adapter on resume activity
            ((ProfileDrawerAdapter) ((ListView) mDrawerLayout.findViewById(android.R.id.list))
                    .getAdapter()).notifyDataSetChanged();
        }*/
    }

    private void navigateTo(final Class<? extends Activity> activityClass, boolean clearTask) {
        Intent intent = new Intent(this, activityClass);
        if(clearTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK | // supported only on api 11+
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS); // supposedly this should work on older apis
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
                mTitle = "";//getString(R.string.app_name);
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
                ParsePush.unsubscribeInBackground("UsersChannel");
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

        mLocationProvider.disconnect();
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
        // google API client location values
        b.putParcelable(LOCATION_KEY, mLastLocation);
        b.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);

        b.putFloat(BOTTOM_WEIGHT_STRING_KEY, mBottomWeight);
        b.putInt(CURRENT_SECTION_KEY, mCurrentSection);

        // call super
        super.onSaveInstanceState(b);

        // attach MapView onSaveInstance method
        if(mMapView != null)
            mMapView.onSaveInstanceState(b);
    }

    /**
     * Draggable screen divider touch listener
     */
    @Override
    public boolean onTouch(View view, MotionEvent event) {

        final int Y = (int) event.getRawY();
        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) mContainer.getLayoutParams();
        LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) mMapSegment.getLayoutParams();

        int delta;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                view.setBackgroundColor(0xff555555);
                // save starting position
                mDividerTop = Y;

                // calculate weight factor
                if (params1.weight > 0) {
                    mWeightFactor = params1.weight / (mHeight - Y);
                } else {
                    mWeightFactor = 1.5f / mHeight;
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
        return mDivider == null || mDivider.getHeight() > 0;
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
        if(mMap == null) {
            bottomWeight = 1;
            showDivider = false;
        }
        if(mDivider != null) {
            if (showDivider && !dividerVisible()) {
                makeDividerVisibile(true);
            } else if (!showDivider) {
                makeDividerVisibile(false);
            }

            LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) mContainer.getLayoutParams();
            LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) mMapSegment.getLayoutParams();

            params1.weight = bottomWeight;
            params2.weight = 1 - bottomWeight;

            mWeightFactor = 1.5f / mHeight;
            mContainer.setLayoutParams(params1);
            mMapSegment.setLayoutParams(params2);
        }
    }

    /**
     * Smooth scrolling to top with bug fix taken from
     * http://stackoverflow.com/questions/14479078/smoothscrolltopositionfromtop-is-not-always-working-like-it-should
     * @param view
     * @param position
     */
    public static void smoothScrollToPositionFromTop(final AbsListView view, final int position) {
        View child = getChildAtPosition(view, position);
        // There's no need to scroll if child is already at top or view is already scrolled to its end
        if ((child != null) && ((child.getTop() == 0) || ((child.getTop() > 0) && !canScrollVertically(view, 1)))) {
            return;
        }

        view.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    view.setOnScrollListener(null);

                    // Fix for scrolling bug
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            view.setSelection(position);
                        }
                    });
                }
            }

            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                                 final int totalItemCount) { }
        });

        // Perform scrolling to position
        new Handler().post(new Runnable() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void run() {
                view.smoothScrollToPositionFromTop(position, 0);
            }
        });
    }

    public static View getChildAtPosition(final AdapterView view, final int position) {
        final int index = position - view.getFirstVisiblePosition();
        if ((index >= 0) && (index < view.getChildCount())) {
            return view.getChildAt(index);
        } else {
            return null;
        }
    }

    /**************** Parse data retrieval ************************************************/

    /**
     * retrieves closest moment and offer points
     */
    private void getParsePoints(final boolean fromLocal) {
        // download closest points
        ParseQuery<Moment> pointsQuery = ParseQuery.getQuery(Moment.class);
        pointsQuery.whereWithinKilometers(
                "location",
                new ParseGeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                50
        );
        pointsQuery.setLimit(10);

        if(fromLocal)
            pointsQuery.fromLocalDatastore();

        pointsQuery.findInBackground(new FindCallback<Moment>() {
            public void done(final List<Moment> pointsList, ParseException e) {
                if (e == null) {
                    Log.d("score_points", "Retrieved " + pointsList.size() + " scores; local=" + String.valueOf(fromLocal) + ";");

                    if(!fromLocal) {
                        pinAllInBackground("closest_points", pointsList);
                    }

                    // assign to member variable
                    mClosestPoints = pointsList;

                    setUpClusterer(mRepositionMap);

                } else {
                    Log.e("score_points", "Error: " + e.getMessage());
                }
            }
        });
    }

    /*************** Map and location *****************************************************/

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link /setUpMap()} once when {@link /mMap} is not null.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMapView != null && mMap == null) {

            mMap = mMapView.getMap();

            if(mMap != null) {
                setUpMap();
            } else {
                switchView(1, false);
            }
        }
    }

    private void setUpMap() {
        /*if (mLastLocation != null) {
            double lat = mLastLocation.getLatitude();
            double lng = mLastLocation.getLongitude();
            LatLng coordinate = new LatLng(lat, lng);

            // marker options for current position
            MarkerOptions options = new MarkerOptions()
                    .position(coordinate)
                    .title("I am here!");

            //CameraUpdate center = CameraUpdateFactory.newLatLng(coordinate);
            //CameraUpdate zoom = CameraUpdateFactory.zoomTo(13);

            // set up map
            //mMap.addMarker(options);
            //mMap.moveCamera(center);
            //mMap.animateCamera(zoom);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 12));
            getParsePoints();*/

        if(mLastLocation != null)
            getParsePoints(true);
    }

    /**
     * Map Marker Clusterer
     */
    private void setUpClusterer(boolean reposition) {
        if (mMap != null && mClusterManager == null) { // TODO: only add cluster manager if required

            //mMap.setMyLocationEnabled(true);

            // Position the map.
            if(reposition && mLastLocation != null)
                mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(
                                new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                                12));

            // Initialize the manager with the context and the map.
            // (Activity extends context, so we can pass 'this' in the constructor.)
            mClusterManager = new ClusterManager<Moment>(this, mMapView.getMap());
            mClusterManager.setRenderer(new MyClusterRenderer(this.getApplicationContext(), mMapView.getMap(), mClusterManager));

            // Point the map's listeners at the listeners implemented by the cluster
            // manager.
            mMap.setOnCameraChangeListener(mClusterManager);
            mMap.setOnMarkerClickListener(mClusterManager);

            mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Moment>() {
                @Override
                public boolean onClusterItemClick(Moment mapClusterItem) {
                    //mMapView.getMap().moveCamera(CameraUpdateFactory.newLatLng(mapClusterItem.getPosition()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(mapClusterItem.getPosition()), 300, null);
                    Toast.makeText(MainActivity.this, mapClusterItem.getTitle(), Toast.LENGTH_SHORT).show();// display toast
                    ListView itemList = (ListView) mContainer.findViewById(android.R.id.list);

                    itemList.setItemChecked(4, true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        smoothScrollToPositionFromTop(itemList, 4);
                    } else {
                        itemList.setSelection(4);
                    }
                    return true;
                }
            });

            // Add cluster items (markers) to the cluster manager.
            try {
                readItems();
            } catch (JSONException e) {
                Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void readItems() throws JSONException {
        //InputStream inputStream = getResources().openRawResource(R.raw.radar_search);
        //List<MapClusterItem> items = mClosestPoints; //new MapItemReader().read(inputStream);
        mClusterManager.addItems(mClosestPoints);
    }

    public void centerMap(double lat, double lon) {
        if(mMap != null)
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat,lon)), 380, null);
    }


    /**
     * handleNewLocation - Callback function
     * @param location Location passed in by the locationProvider
     */
    @Override
    public void handleNewLocation(Location location) {
        mLoaderView.setVisibility(View.GONE);
        mMainView.setVisibility(View.VISIBLE);

        mLastLocation = location;
        if(mRepositionMap) {
            getParsePoints(mClusterManager != null);
        }
        Log.d("new_loc", String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()));
    }

    /*************** restoring saved data *****************************************************/

    /**
     * Stored data retrieval
     * @param savedInstanceState Bundle data from onSaveInstanceState
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {

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

            /**
             * I added this for to retain the divider state
             */
            if (savedInstanceState.keySet().contains(BOTTOM_WEIGHT_STRING_KEY)) {
                mBottomWeight = savedInstanceState.getFloat(
                        BOTTOM_WEIGHT_STRING_KEY);
            }
            if (savedInstanceState.keySet().contains(CURRENT_SECTION_KEY)) {
                mCurrentSection = savedInstanceState.getInt(
                        CURRENT_SECTION_KEY);
            }
            // TODO: use local storage also for markers
        }
    }
}