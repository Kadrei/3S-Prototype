package de.thmgames.s3.Activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.camera.CropImageIntentBuilder;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pkmmte.view.CircularImageView;
import com.squareup.otto.Subscribe;

import org.altbeacon.beacon.Beacon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.thmgames.s3.Adapter.NavDrawerListAdapter;
import de.thmgames.s3.Adapter.SpinnerElementAdapter;
import de.thmgames.s3.Controller.Users;
import de.thmgames.s3.Controller.ViewLocalizer;
import de.thmgames.s3.Fragments.HighscoreFragment;
import de.thmgames.s3.Fragments.MapFragment;
import de.thmgames.s3.Fragments.PrefsFragment;
import de.thmgames.s3.Fragments.QuestListFragment;
import de.thmgames.s3.Model.ISpinnerElement;
import de.thmgames.s3.Model.NavDrawerItem;
import de.thmgames.s3.Model.ParseModels.Fraction;
import de.thmgames.s3.Model.ParseModels.LocationSystem.Location;
import de.thmgames.s3.Model.ParseModels.LocationSystem.MapElement;
import de.thmgames.s3.Model.ParseModels.LocationSystem.MapElementPart;
import de.thmgames.s3.Model.ParseModels.User;
import de.thmgames.s3.Otto.Events.LoadEvent;
import de.thmgames.s3.Otto.Events.MapElementSelectedEvent;
import de.thmgames.s3.Otto.Events.MapElementsLoadedEvent;
import de.thmgames.s3.Otto.OttoEventBusProvider;
import de.thmgames.s3.R;
import de.thmgames.s3.Utils.AndroidUtils;
import de.thmgames.s3.Utils.FileUtils;
import de.thmgames.s3.Utils.LayoutUtils;
import de.thmgames.s3.Utils.LogUtils;
import de.thmgames.s3.Utils.MediaStoreUtils;
import de.thmgames.s3.Views.FloatingLocationMenu;

public class MainActivity extends AbstractBaseActivity implements FloatingLocationMenu.OnLocationFABClickListener, View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = MainActivity.class.getName();

    private String[] navMenuTitles;
    private FrameLayout mainFragment;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private LinearLayout mDrawerWrapper;
    private CircularImageView mUserPictureCIV;
    private TextView mProfileNameTV;
    private TextView mPointTextTV;
    private ActionBarDrawerToggle mDrawerToggle;
    private FloatingLocationMenu mFloatingLocationMenu;
    private Spinner mLocationSpinner;
    private boolean mSpinnerDataLoaded;
    private final static int STANDARD_FRAGMENT=0;

    private static final String KEY_FRAGMENT_STATE = "fragment.state";

    public static Intent getIntent(Context ctx) {
        return new Intent(ctx, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        lookForBeacons(true);
        needInternet(true);
        setNeededUserState(User.USERSTATE.LOGGEDIN);
        checkUserState(true);
        askForPushEnablingIfNotAskedBefore(true);
        instantiateViews();
        setUpToolbar();
        setUpDrawer();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        OttoEventBusProvider.getInstance().register(this);
        OttoEventBusProvider.getInstanceForUIThread().register(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        OttoEventBusProvider.getInstance().unregister(this);
        OttoEventBusProvider.getInstanceForUIThread().unregister(this);
        super.onPause();
    }

    private void showViews() {
        mFloatingLocationMenu.hideMenu();
        setLayoutBehaviourForFragment(mSlideMenuClickListener.mCurrentFragment);
        showProfileData();
        setLayoutBehaviourForFragment(mSlideMenuClickListener.mCurrentFragment);
        mFloatingLocationMenu.setOnLocationFABClickListener(this);
        if (mSlideMenuClickListener.mCurrentFragment == null)
            mSlideMenuClickListener.displayFragment(STANDARD_FRAGMENT);
    }

    @Override
    protected void onCorrectUserState() {
        showViews();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (mSlideMenuClickListener.mCurrentFragment != null)
            getFragmentManager().putFragment(savedInstanceState, KEY_FRAGMENT_STATE, mSlideMenuClickListener.mCurrentFragment);
    }

    @Override
    public void onSnackbarShown(int height) {
        mFloatingLocationMenu.setPadding(0, 0, 0, LayoutUtils.dpToPx(height, this) + LayoutUtils.getPaddingBottom(this));
    }

    @Override
    public void onSnackbarDismissed(int height) {
        mFloatingLocationMenu.setPadding(0, 0, 0, LayoutUtils.getPaddingBottom(this));
    }


    @Override
    protected void restoreSavedInstanceState(Bundle savedInstanceState) {
        super.restoreSavedInstanceState(savedInstanceState);
        if (savedInstanceState != null && mSlideMenuClickListener!=null) {
            mSlideMenuClickListener.mCurrentFragment = getFragmentManager().getFragment(savedInstanceState, KEY_FRAGMENT_STATE);
        }
    }

    private void instantiateViews() {
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerWrapper = (LinearLayout) findViewById(R.id.left_drawer);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
        mLocationSpinner = (Spinner) findViewById(R.id.spinner_locations);
        mUserPictureCIV = (CircularImageView) findViewById(R.id.profileimage);
        mPointTextTV = (TextView) findViewById(R.id.pointstextview);
        mProfileNameTV = (TextView) findViewById(R.id.usernametextview);
        mainFragment = (FrameLayout) findViewById(R.id.main_fragment);
        mFloatingLocationMenu = (FloatingLocationMenu) findViewById(R.id.floating_loc_menu);
    }

    private void setUpToolbar() {
        setSupportActionBar(mToolbar);
        mActionBarBackgroundDrawable = mToolbar.getBackground();
    }

    private SlideMenuClickListener mSlideMenuClickListener;

    private void setUpDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,  /* DrawerLayout object */
                mToolbar,
                R.string.abc_action_bar_home_description,  /* "open drawer" description */
                R.string.abc_action_bar_home_description  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        TypedArray navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<NavDrawerItem>();
        // adding nav drawer items to array
        for (int i = 0; i < navMenuTitles.length; i++) {
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[i], navMenuIcons.getResourceId(i, -1)));
        }
        // Recycle the typed array
        navMenuIcons.recycle();
        // setting the nav drawer list adapter
        NavDrawerListAdapter adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(adapter);
        mSlideMenuClickListener = new SlideMenuClickListener();
        mDrawerList.setOnItemClickListener(mSlideMenuClickListener);

        if (!AndroidUtils.isLollipopOrHigher()) mDrawerWrapper.setPadding(0, LayoutUtils.getPaddingTopWithoutActionBar(this), 0, 0);
        mDrawerList.setPadding(0, 0, 0, LayoutUtils.getPaddingBottom(this));
        mDrawerList.setClipToPadding(false);
    }

    public void setLayoutBehaviourForFragment(Fragment fragment) {
        if (fragment != null && fragment instanceof MapFragment) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            mActionBarBackgroundDrawable.setAlpha(0);
            mainFragment.setPadding(0, 0, 0, 0);
            mToolbar.setTitleTextColor(getResources().getColor(R.color.black));
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            mLocationSpinner.setVisibility(View.VISIBLE);
            if (!mSpinnerDataLoaded) {
                ArrayList<String> itemList = new ArrayList<>();
                itemList.add(getString(R.string.loading));
                ArrayAdapter<String> aAdpt = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1 , android.R.id.text1, itemList);
                mLocationSpinner.setAdapter(aAdpt);
            }
        } else {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            mLocationSpinner.setVisibility(View.GONE);
            mActionBarBackgroundDrawable.setAlpha(255);
            mainFragment.setPadding(0, LayoutUtils.getPaddingTopWithActionBar(this), 0, 0);
            mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        }
    }

    @Override
    public void onLocationFABClickListener(View v, final Location l) {
        showLocationDialog(l);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mUserPictureCIV)) {
            startActivityForResult(MediaStoreUtils.getPickImageIntent(this), REQUEST_PICTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        File croppedImageFile = new File(getFilesDir(), mCurrentUser.getUsername()+"_Profile_Picture_tmp.png");
        if ((requestCode == REQUEST_PICTURE) && (resultCode == RESULT_OK)) {
            Uri croppedImage = Uri.fromFile(croppedImageFile);
            CropImageIntentBuilder cropImage = new CropImageIntentBuilder(300, 300, croppedImage).setScaleUpIfNeeded(false).setOutputFormat("PNG");
            cropImage.setSourceImage(data.getData());
            startActivityForResult(cropImage.getIntent(this), REQUEST_CROP_PICTURE);
        } else if ((requestCode == REQUEST_CROP_PICTURE) && (resultCode == RESULT_OK)) {
             saveProfileImage(croppedImageFile);

        }
    }

    public void saveProfileImage(File croppedImageFile){
        if(Users.hasCurrentUser()){
            showLoadingProgress(true);
            try {
                Users.setProfileImage(Users.getCurrentS3User(),FileUtils.fileToBytes(croppedImageFile), new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        showLoadingProgress(false);
                        if (e != null) {
                            LogUtils.e(TAG, "Error while saving Profile Image", e);
                            showSnackBar(getString(R.string.error_saving_image));
                            return;
                        }
                        refreshProfileData(mCurrentUser);
                    }
                });
            } catch (IOException e) {
                showLoadingProgress(false);
                LogUtils.e(TAG, "Fehler beim Hochladen des Bildes", e);
                showSnackBar(getString(R.string.error_saving_image));
            }
        }
    }

    private static int REQUEST_PICTURE = 1;
    private static int REQUEST_CROP_PICTURE = 2;


    /**
     * Slide menu item click listener
     */
    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            displayFragment(position);
        }

        public Fragment mCurrentFragment;
        private PrefsFragment mPrefsFragment;
        private MapFragment mMapFragment;
        private QuestListFragment mQuestListFragment;
        private HighscoreFragment mHighScoreFragment;
        public void displayFragment(int position) {
            switch (position) {
                case 0:
                    if(mMapFragment==null) mMapFragment= new MapFragment();
                    mCurrentFragment = mMapFragment;
                    break;
                case 1:
                    if(mQuestListFragment==null) mQuestListFragment= new QuestListFragment();
                    setTitle(getString(R.string.questlist_title));
                    mCurrentFragment = mQuestListFragment;
                    break;
                case 2:
                    if(mHighScoreFragment==null) mHighScoreFragment= new HighscoreFragment();
                    mCurrentFragment = mHighScoreFragment;
                    setTitle(getString(R.string.highscore_title));
                    break;
                case 3:
                    if(mPrefsFragment==null) mPrefsFragment= new PrefsFragment();
                    mCurrentFragment = mPrefsFragment;
                    setTitle(getString(R.string.settings));
                    break;
                case 4:
                    ParseUser.logOut();
                    startActivity(SignUpOrInActivity.getIntent(MainActivity.this, true));
                    break;
                default:
                    break;
            }
            String mTitle = navMenuTitles[position];
            getSupportActionBar().setTitle(mTitle);
            setLayoutBehaviourForFragment(mCurrentFragment);
            if (mCurrentFragment != null) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.main_fragment, mCurrentFragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                // Add to backstack
                ft.addToBackStack("fragment"+position);
                ft.commit();
                // update selected item and title, then close the drawer
                mDrawerList.setItemChecked(position, true);
                mDrawerList.setSelection(position);
                setTitle(mTitle);
                mDrawerLayout.closeDrawer(mDrawerWrapper);
            } else {
                // error in creating fragment
                showSnackBar("Error in creating fragment");
                LogUtils.e(TAG, "Error in creating fragment");
            }
        }
    }

    private List<MapElement> mS3MapElements;

    @Subscribe
    public void onMapElementsLoaded(MapElementsLoadedEvent e) {
        mS3MapElements = e.mS3MapElements;
        setLocationSpinnerItems(mS3MapElements);
    }

    private void setLocationSpinnerItems(List<MapElement> mapElements) {
        if(mSpinnerDataLoaded) return;
        ArrayList<ISpinnerElement> itemList = new ArrayList<>();
        for (MapElement mapElement : mapElements) {
            itemList.add(mapElement);
            for (MapElementPart part : mapElement.getParts()) {
                part.setParent(mapElement);
                itemList.add(part);
            }
        }
        SpinnerElementAdapter aAdpt = new SpinnerElementAdapter(this, android.R.layout.simple_spinner_item, itemList);
        mLocationSpinner.setAdapter(aAdpt);
        aAdpt.setDropDownViewResource(R.layout.spinner_list_item);
        mLocationSpinner.setOnItemSelectedListener(this);
        mSpinnerDataLoaded = true;
        OttoEventBusProvider.getInstanceForUIThread().post(new MapElementSelectedEvent(0, 0));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int mapElementID = 0;
        int mapElementPartID = 0;
        int counter = 0;
        for (MapElement mapElement : mS3MapElements) {
            counter ++;
            for (MapElementPart part : mapElement.getParts()) {
                if (counter == position) {
                    OttoEventBusProvider.getInstanceForUIThread().post(new MapElementSelectedEvent(mapElementID, mapElementPartID));
                    return;
                }
                counter++;
                mapElementPartID++;
            }
            mapElementPartID = 0;
            mapElementID++;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        if (mS3MapElements == null || mS3MapElements.size() == 0 || mS3MapElements.get(0).getParts() == null || mS3MapElements.get(0).getParts().size() != 0)
            return;
        OttoEventBusProvider.getInstanceForUIThread().post(new MapElementSelectedEvent(0, 0));
    }

    @Override
    protected void onUserUpdated(User user){
        super.onUserUpdated(user);
        refreshProfileData(user);
    }

    public void showProfileData() {
        mUserPictureCIV.setOnClickListener(MainActivity.this);
        refreshProfileData(mCurrentUser);
    }

    ViewLocalizer profileLocalizer;
    public void refreshProfileData(final User user) {
        if(user==null) return;
        mPointTextTV.setText("" + user.getUserPoints() + getString(R.string.points)+" // " + mCurrentUser.getEnergy() + getString(R.string.energy));
        if (user.hasImage()) {
            mPicasso.load(user.getImage().getUrl()).placeholder(R.drawable.ic_account_circle_grey600_48dp).into(mUserPictureCIV);
        }

        mProfileNameTV.setText(getString(R.string.loading)+" // " + user.getUsername());

        if(user.hasFraction()){
            showLoadingProgress(true);
            user.getFraction().fetchIfNeededInBackground(new GetCallback<Fraction>() {
                @Override
                public void done(Fraction fraction, ParseException e) {
                    showLoadingProgress(false);
                    if (e != null) {
                        LogUtils.e(TAG, "Fehler beim Laden der Fraktionsinformationen", e);
                        showSnackBar(getString(R.string.error_loading_fractions));
                        mProfileNameTV.setText(getString(R.string.unknown)+" // " + user.getUsername());
                        return;
                    }
                    if(profileLocalizer!=null) profileLocalizer.cancel();
                    profileLocalizer = new ViewLocalizer(MainActivity.this);
                    profileLocalizer.setLocalizedStringOnTextView(fraction.getName(), mProfileNameTV,""," // " + user.getUsername());

                }
            });
        }
    }

    /**
     * OPTIONSMENU
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerWrapper);
        //menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item);
    }

    @Override
    public void onBeaconInRange(final Collection<Beacon> beacons) {
        super.onBeaconInRange(beacons);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFloatingLocationMenu.refreshBeacons(beacons);
            }
        });

    }

    @Override
    protected void showLoadingAnimation() {
//        OttoEventBusProvider.getInstanceForUIThread().post(new LoadEvent(true, "mainactivity"));
    }

    @Override
    protected void hideLoadingAnimation() {
//        OttoEventBusProvider.getInstanceForUIThread().post(new LoadEvent(false, "mainactivity"));
    }


}
