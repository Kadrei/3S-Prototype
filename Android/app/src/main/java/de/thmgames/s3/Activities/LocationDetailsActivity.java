package de.thmgames.s3.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.altbeacon.beacon.Beacon;

import java.util.Collection;

import de.thmgames.s3.Controller.BeaconController;
import de.thmgames.s3.Controller.Locations;
import de.thmgames.s3.Controller.ViewLocalizer;
import de.thmgames.s3.Fragments.DialogFragments.LocationActionFragment;
import de.thmgames.s3.Fragments.DialogFragments.ParameterLoadingFragment;
import de.thmgames.s3.Model.ParseModels.Fraction;
import de.thmgames.s3.Model.ParseModels.LocationSystem.Location;
import de.thmgames.s3.Model.ParseModels.LocationSystem.LocationCaptureData;
import de.thmgames.s3.Model.ParseModels.User;
import de.thmgames.s3.Otto.Events.ActionReceivedEvent;
import de.thmgames.s3.Otto.Events.BeaconInRangeEvent;
import de.thmgames.s3.Otto.Events.ShowSnackBarEvent;
import de.thmgames.s3.R;
import de.thmgames.s3.Receiver.Broadcast.BluetoothStateReceiver;
import de.thmgames.s3.Transformer.ColorFilterTransformer;
import de.thmgames.s3.Transformer.ImageOverlayTransformer;
import de.thmgames.s3.Transformer.PaletteTransformer;
import de.thmgames.s3.Utils.AndroidUtils;
import de.thmgames.s3.Utils.LogUtils;
import de.thmgames.s3.Views.Widgets.LoadingImageView;
import de.thmgames.s3.Views.Widgets.SlidingTabLayout;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class LocationDetailsActivity extends AbstractBaseActivity implements View.OnClickListener, BluetoothStateReceiver.BluetoothStateChangeListener, LoadingImageView.ImageLoadingCallback, ViewTreeObserver.OnGlobalLayoutListener {

    public final static String TAG = LocationDetailsActivity.class.getName();
    private LoadingImageView mLocationPhotoView;
    private TextView mLocationName;
    private TextView mLocationAddress;
    private FloatingActionButton mActionButton;
    private ViewPager mViewPager;
    private SlidingTabLayout mSlidingTabLayout;

    private LinearLayout mLocationInfoWrapper;
    private SmoothProgressBar mProgressbar;
    private Palette mPalette;

    private Location mLocation;
    private LocationCaptureData mCaptureData;
    private Fraction mOwnerFraction;

    private static final String INTENT_LOCATION_ID = "location.id";

    public static Intent getIntent(Context ctx, String locationId) {
        Intent i = new Intent(ctx, LocationDetailsActivity.class);
        i.putExtra(INTENT_LOCATION_ID, locationId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        lookForBeacons(true);
        needInternet(true);
        setNeededUserState(User.USERSTATE.LOGGEDIN);
        checkUserState(true);
        askForPushEnablingIfNotAskedBefore(false);
        setContentView(R.layout.activity_location_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mActionBarBackgroundDrawable = mToolbar.getBackground();
        mActionBarBackgroundDrawable.setAlpha(0);
        setSupportActionBar(mToolbar);
        mBeaconController = BeaconController.getInstance(this.getApplicationContext());
        mProgressbar = (SmoothProgressBar) findViewById(R.id.loading_progress_linear);
        mLocationInfoWrapper = (LinearLayout) findViewById(R.id.locationInfoWrapper);
        mLocationPhotoView = (LoadingImageView) findViewById(R.id.locationImageHeader);
        mLocationPhotoView.setLoadingListener(this);
        mLocationPhotoView.setPlaceholderResource(R.drawable.placeholder_location);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mLocationName = (TextView) findViewById(R.id.locationName);
        mLocationAddress = (TextView) findViewById(R.id.locationAdress);
        mActionButton = (FloatingActionButton) findViewById(R.id.actionButton);
        mActionButton.setEnabled(false);
        mActionButton.setOnClickListener(this);
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);

        ViewTreeObserver vto = mLocationPhotoView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(this);
        }

        super.onCreate(savedInstanceState);

        if (getIntent() != null && getIntent().hasExtra(INTENT_LOCATION_ID)) {
            showLoadingProgress(true);
            String locationId = getIntent().getStringExtra(INTENT_LOCATION_ID);
            Locations.findLocationForLocationID(locationId, new GetCallback<Location>() {
                public void done(Location location, ParseException e) {
                    showLoadingProgress(false);
                    if (e == null) {
                        initiateViews(location);
                    } else {
                        showSnackBar(getString(R.string.error_loading_locationdata));
                        LogUtils.e(TAG, "Error while retrieving Locationdata", e);
                    }
                }
            });
        } else {
            this.finish();
        }
    }

    @Override
    public void onResume() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        super.onResume();
    }

    @Override
    protected void onCorrectUserState() {

    }


    public boolean isLocInRange() {
        for (Beacon bcn : mBeacons) {
            if (mLocation != null && mLocation.isLocationForBeacon(bcn)) {
                return true;
            }
        }
        return false;
    }

    public void refreshActionButtonState() {
        if (!AndroidUtils.isBluetoothAvailable(this) || mLocation == null || !isLocInRange() || !mLocation.isCapturePoint() || mCurrentUser.getEnergy()<1) {
            mActionButton.setVisibility(View.GONE);
            return;
        }
        mActionButton.setVisibility(View.VISIBLE);
        mActionButton.setColorNormal((mPalette != null) ? mPalette.getVibrantColor(R.color.primaryDark) : getResources().getColor(R.color.primaryDark));
        mActionButton.setEnabled(true);
    }

    ViewLocalizer localizer;
    private void initiateViews(Location location) {
        mLocation = location;
        if(localizer!=null) localizer.cancel();
        localizer = new ViewLocalizer(this);
        localizer.setLocalizedStringOnTextView(location.getName(), mLocationName);
        localizer.setLocalizedStringOnTextView(location.getAddress(),mLocationAddress);
        LocationPagerAdapter mSectionsPagerAdapter = new LocationPagerAdapter(this.getFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(1);
        mSlidingTabLayout.setViewPager(mViewPager);
        loadLocationImage();

        if (mLocation.isCapturePoint()) {
            showLoadingProgress(true);
            mLocation.getCaptureData().refresh(new GetCallback<LocationCaptureData>() {
                @Override
                public void done(LocationCaptureData captureData, ParseException e) {
                    showLoadingProgress(false);
                    if(e==null){
                        setCaptureData(captureData);
                    }
                }
            });
        }
    }

    public void setCaptureData(LocationCaptureData captureData){
        mCaptureData = captureData;
        if(mCaptureData.hasOwnerFraction() && mCaptureData.getOwnerFraction()!=null){
            mCaptureData.getOwnerFraction().fetchWhereAvailable(new GetCallback<Fraction>() {
                @Override
                public void done(Fraction ownerFraction, ParseException e) {
                    mOwnerFraction = ownerFraction;
                    mOwnerFraction.pinInBackground();
                    loadCapturedLocationImage();
                }
            });
        }else{
            loadCapturedLocationImage();
        }
        refreshActionButtonState();
    }

    public void loadLocationImage() {
        if (mLocation == null || !mLocation.hasHeaderImage()) return;
        mPicasso.cancelRequest(mLocationPhotoView);
        mLocationPhotoView.setLoadingListener(this);
        mPicasso.load(mLocation.getHeaderImage().getUrl())
                .tag(TAG)
                .transform(PaletteTransformer.instance())
                .into(mLocationPhotoView);
    }

    @Override
    public void onDismissFragmentDialog(String fragmentTag){
        super.onDismissFragmentDialog(fragmentTag);
        if(mCaptureData==null || !fragmentTag.contains(LocationActionFragment.class.getName())) return;
        showLoadingProgress(true);
        mCaptureData.refresh(new GetCallback<LocationCaptureData>() {
            @Override
            public void done(LocationCaptureData captureData, ParseException e) {
                showLoadingProgress(false);
                if(e!=null){
                    LogUtils.e(TAG,"Error while refreshing Capture Data" ,e);
                }else{
                    setCaptureData(captureData);
                    captureData.pinInBackground();
                }
            }
        });
    }

    private boolean hasOwnerFraction=false;
    private final static double fractionLogoSizeFactor = 3.0;
    public void loadCapturedLocationImage() {
        if (mLocation == null || !mLocation.hasHeaderImage()) return;
        mPicasso.cancelRequest(mLocationPhotoView);
        mLocationPhotoView.setLoadingListener(null);
        RequestCreator mPicassoLoadTmp = mPicasso.load(mLocation.getHeaderImage().getUrl());
        if(mOwnerFraction!=null) {
            hasOwnerFraction=true;
            int fractionColor = mOwnerFraction.getMainColor();
            int fractionTextColor = mOwnerFraction.getTextColor();
            mLocationInfoWrapper.setBackgroundColor(fractionColor);
            mSlidingTabLayout.setBackgroundColor(fractionColor);
            mLocationName.setTextColor(fractionTextColor);
            mLocationAddress.setTextColor(fractionTextColor);
            mSlidingTabLayout.setTextColor(fractionTextColor);
            mPicassoLoadTmp = mPicassoLoadTmp.transform(new ColorFilterTransformer(fractionColor)).tag(TAG);
            if (mOwnerFraction.hasLogo())
                mPicassoLoadTmp.transform(new ImageOverlayTransformer(mOwnerFraction.getLogo().getUrl(), mPicasso, (int) (mLocationPhotoView.getHeight() / fractionLogoSizeFactor)));
        }else{
            mPicassoLoadTmp.transform(PaletteTransformer.instance());
        }
        mPicassoLoadTmp.into(mLocationPhotoView);
    }

    @Override
    public void onStartLoading(Drawable placeHolderDrawable) {}

    @Override
    public void onSuccessfullyFinishedLoading(Bitmap bmp, Picasso.LoadedFrom from) {
        mPalette = PaletteTransformer.getPalette(bmp);
        if(mPalette==null) return;
        Palette.Swatch swatch = mPalette.getVibrantSwatch() == null? mPalette.getMutedSwatch(): mPalette.getVibrantSwatch();

        if (swatch != null && !hasOwnerFraction) {
            mLocationInfoWrapper.setBackgroundColor(swatch.getRgb());
            mSlidingTabLayout.setBackgroundColor(swatch.getRgb());
            mLocationName.setTextColor(swatch.getTitleTextColor());
            mLocationAddress.setTextColor(swatch.getBodyTextColor());
            mSlidingTabLayout.setTextColor(swatch.getTitleTextColor());
        }
        refreshActionButtonState();
    }

    @Override
    public void onFailure(Drawable errorDrawable) {}

    @Override
    public void onSnackbarShown(int height) {}

    @Override
    public void onSnackbarDismissed(int height) {}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }


    @Override
    protected void showLoadingAnimation() {
        mProgressbar.setVisibility(View.VISIBLE);
        mProgressbar.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressbar.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void hideLoadingAnimation() {
        mProgressbar.setVisibility(View.GONE);
        mProgressbar.animate().setDuration(shortAnimTime).alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressbar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == mActionButton) {
            LocationActionFragment newFragment = LocationActionFragment.newInstance(mLocation.getObjectId(), isLocInRange(), LocationActionFragment.FRAGMENT_ATTACKMODE);
            newFragment.show(getFragmentManager(), "dialog");
        }
    }


    @Override
    public void onOn() {
        super.startBeaconListener();
        refreshActionButtonState();
    }


    @Override
    public void onOff() {
        super.stopBeaconListener();
        refreshActionButtonState();
    }


    @Override
    protected void onBeaconInRange(Collection<Beacon> beacons) {
        super.onBeaconInRange(beacons);

        LocationDetailsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshActionButtonState();
            }
        });
    }

    @Override
    public void onGlobalLayout() {
        mLocationPhotoView.setImageRatio(HEADER_ASPECT_RATIO);
    }


    public class LocationPagerAdapter extends FragmentPagerAdapter {

        public LocationPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return (position == 0 ?  getString(R.string.info):getString(R.string.story));
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            String infoId = (mLocation.getInfo()!=null? mLocation.getInfo().getObjectId():"");
            String loreID = (mLocation.getLore()!=null? mLocation.getLore().getObjectId():"");
            String objectID = (position==0? infoId : loreID);
            return ParameterLoadingFragment.newInstanceForWebElement( objectID, false);
        }
    }

}

