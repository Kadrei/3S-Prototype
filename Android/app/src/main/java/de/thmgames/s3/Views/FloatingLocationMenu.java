package de.thmgames.s3.Views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.squareup.picasso.Picasso;

import org.altbeacon.beacon.Beacon;

import java.util.Collection;
import java.util.List;

import de.thmgames.s3.Controller.Locations;
import de.thmgames.s3.Model.ParseModels.LocationSystem.Location;
import de.thmgames.s3.R;
import de.thmgames.s3.Utils.ColorUtils;
import de.thmgames.s3.Utils.LayoutUtils;
import de.thmgames.s3.Utils.LogUtils;

/**
 * Created by Benedikt on 03.12.2014.
 */
public class FloatingLocationMenu extends RelativeLayout implements View.OnClickListener {
    private static final String TAG = FloatingLocationMenu.class.getName();
    private final int MAX_BUTTONS=3;
    private Context mContext;
    private boolean isShown = false;
    private boolean isOpened = false;
    private List<Beacon> mLastRangedBeacons;

    private Beacon[] mBeacons = new Beacon[MAX_BUTTONS];
    private Location[] mLocations = new Location[MAX_BUTTONS];

    private FloatingActionButton mLocationMenuFAB;
    private FloatingActionButton[] mFABs = new FloatingActionButton[MAX_BUTTONS];

    private Picasso mPicasso;
     private Animation mRotationAnimation;
     private Drawable mSyncDrawable;


    public FloatingLocationMenu(Context context) {
        super(context);
        setUp(context);
    }

    public FloatingLocationMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp(context);
    }

    public FloatingLocationMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUp(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FloatingLocationMenu(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setUp(context);
    }

    public void setUp(Context ctx) {
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = ctx;
        inflater.inflate(R.layout.floating_location_menu, this);

        mLocationMenuFAB = (FloatingActionButton) findViewById(R.id.fabstart);
        mFABs[0] = (FloatingActionButton) findViewById(R.id.fabbottom);
        mFABs[1] = (FloatingActionButton) findViewById(R.id.fabcenter);
        mFABs[2] = (FloatingActionButton) findViewById(R.id.fabtop);
        mLocationMenuFAB.hide();
        mLocationMenuFAB.setOnClickListener(this);
        for(int i=0;i<MAX_BUTTONS;i++){
            mFABs[i].setVisibility(View.GONE);
            mFABs[i].setOnClickListener(this);
        }

        mRotationAnimation = AnimationUtils.loadAnimation(mContext, R.anim.clockwise_infinite_rotation);

        mSyncDrawable=getResources().getDrawable(R.drawable.ic_sync_white_24dp);
        mPicasso = Picasso.with(mContext);

        this.setPadding(0, 0, 0, LayoutUtils.getPaddingBottom(mContext));
    }



    public void refreshBeacons(Collection<Beacon> beacons) {
        mLastRangedBeacons = (List<Beacon>) beacons;
        if (beacons.size() > 0) {
            mLocationMenuFAB.show(!isShown);
        } else {
            mLocationMenuFAB.hide(isShown);
            for(FloatingActionButton fab:mFABs){
                fab.setVisibility(View.GONE);
            }
        }
        isShown = (beacons.size() > 0);
        if (isOpened) {
            for(int i=0; i<MAX_BUTTONS;i++){
                ColorUtils.setSaturationOnImageView(mFABs[i], (beacons.contains(mBeacons[i])) ? 1 : 0);
            }
        }
    }

    private void setLocations(List<Location> locations) {
        if (!isOpened) return;
        for(FloatingActionButton fab:mFABs){
            mPicasso.cancelRequest(fab);
            fab.clearAnimation();
        }
        if(locations==null) return;
        for (Location loc : locations) {
            for(int i=0;i<MAX_BUTTONS;i++){
                if (loc.isLocationForBeacon(mBeacons[i])) {
                    mLocations[i] = loc;
                    if (mLocations[i].hasMiniImage())
                        mPicasso.load(mLocations[i].getMiniImage().getUrl()).placeholder(R.drawable.ic_local_play_white_24dp).into(mFABs[i]);
                }
            }
        }
        for(int i=0;i<mBeacons.length;i++){
            if(i>locations.size()-1) mFABs[i].hide(true);
        }
    }

private OnLocationFABClickListener mLocationFABClickListener;

    public void setOnLocationFABClickListener(OnLocationFABClickListener onFabClickListener) {
        mLocationFABClickListener = onFabClickListener;
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mLocationMenuFAB)) {
            this.toggle();
            return;
        }
        for(int i=0;i<MAX_BUTTONS;i++){
            if (v.equals(mFABs[i]) && mLocationFABClickListener != null) {
                mLocationFABClickListener.onLocationFABClickListener(v,  mLocations[i]);
                return;
            }
        }
    }

    private void toggle() {
        mBeacons = new Beacon[MAX_BUTTONS];
        mLocations = new Location[MAX_BUTTONS];
        if (isOpened) {
            hideMenu();
        } else {
            showMenu();
            loadLocation();
        }
    }

    private void loadLocation(){
        Locations.findAllLocationsWithBeaconsInBackground(mLastRangedBeacons, new FindCallback<Location>() {
            @Override
            public void done(List<Location> locations, ParseException e) {
                if (e != null || locations==null || locations.isEmpty()){
                    LogUtils.e(TAG, "Error while downloading Locationdata for beacons", e);
                    FloatingLocationMenu.this.hideMenu();
                }else {
                    setLocations(locations);
                }
            }
        });
    }

    public void hideMenu(){
        if (!isOpened) return;
        mLocationMenuFAB.animate().setDuration(500).rotation(0f).start();
        for(FloatingActionButton fab:mFABs){
            mPicasso.cancelRequest(fab);
            fab.clearAnimation();
            fab.setColorNormalResId(R.color.primary);
            fab.setImageDrawable(mSyncDrawable);
            fab.hide();
            fab.setVisibility(View.GONE);
        }
        isOpened = false;
        loadLocation();
    }

    public void showMenu(){
        if (isOpened) return;
        mLocationMenuFAB.animate().setDuration(500).rotation(90f).start();
        for(int i=0;i<mLastRangedBeacons.size()&&i<MAX_BUTTONS;i++){
            mFABs[i].show();
            mFABs[i].setVisibility(View.VISIBLE);
            mFABs[i].startAnimation(mRotationAnimation);
            mBeacons[i] = mLastRangedBeacons.get(i);
        }
        isOpened = true;
    }

public interface OnLocationFABClickListener {
    public void onLocationFABClickListener(View v, Location l);
}

}
