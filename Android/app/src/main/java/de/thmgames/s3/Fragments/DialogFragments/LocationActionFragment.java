package de.thmgames.s3.Fragments.DialogFragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.thmgames.s3.Activities.LocationDetailsActivity;
import de.thmgames.s3.Controller.Locations;
import de.thmgames.s3.Controller.Users;
import de.thmgames.s3.Controller.ViewLocalizer;
import de.thmgames.s3.Listener.DialogFragmentDismissListener;
import de.thmgames.s3.Model.ParseModels.Cloud.Responses.AttackDefendResponse;
import de.thmgames.s3.Model.ParseModels.Fraction;
import de.thmgames.s3.Model.ParseModels.LocationSystem.Location;
import de.thmgames.s3.Model.ParseModels.LocationSystem.LocationCaptureData;
import de.thmgames.s3.Otto.Events.ActionReceivedEvent;
import de.thmgames.s3.Otto.OttoEventBusProvider;
import de.thmgames.s3.R;
import de.thmgames.s3.Transformer.ColorFilterTransformer;
import de.thmgames.s3.Transformer.ImageOverlayTransformer;
import de.thmgames.s3.Transformer.PaletteTransformer;
import de.thmgames.s3.Utils.LogUtils;
import de.thmgames.s3.Views.Widgets.LoadingImageView;

/**
 * Created by Benedikt on 29.01.2015.
 */
public class LocationActionFragment extends BaseDialogFragment implements LoadingImageView.ImageLoadingCallback, DiscreteSeekBar.OnProgressChangeListener, ViewTreeObserver.OnGlobalLayoutListener {

    public final static String TAG = LocationActionFragment.class.getName();
    private LoadingImageView mLocationImage;
    private TextView mLocationName;
    private LocationCaptureData mCaptureData;
    private Location mLocation;
    private RelativeLayout mTextWrapper;
    private RelativeLayout mImageWrapper;
    private String locationId;

    private DiscreteSeekBar mAttackValueBar;
    private TextView mCurEnergyValueText;
    public final static String FRAGMENT_LOCATION_ID = "location_id";
    public final static String FRAGMENT_LOCATION_INRANGE = "location.inrange";
    public final static String FRAGMENT_STARTMODE = "fragment.mode";
    public final static int FRAGMENT_CHOOSEMODE = 0;
    public final static int FRAGMENT_ATTACKMODE = 1;
    private Fraction mOwnerFraction;
    private Button mSecondButton;
    private Button mMainButton;
    private RelativeLayout buttonWrapper;
    private boolean inRange;
    private int startMode;
    private int curMode;
    private int white;
    protected static final float HEADER_ASPECT_RATIO = 1.7777777f;//16:9

    /**
     * Create a new instance of MyDialogFragment,
     */
    public static LocationActionFragment newInstance(String locId, boolean inRange, int mode) {
        LocationActionFragment f = new LocationActionFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(FRAGMENT_LOCATION_ID, locId);
        args.putBoolean(FRAGMENT_LOCATION_INRANGE, inRange);
        args.putInt(FRAGMENT_STARTMODE, mode);
        f.setArguments(args);

        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationId = getArguments().getString(FRAGMENT_LOCATION_ID, "");
        inRange = getArguments().getBoolean(FRAGMENT_LOCATION_INRANGE, false);
        startMode = getArguments().getInt(FRAGMENT_STARTMODE, FRAGMENT_CHOOSEMODE);
        if (startMode != FRAGMENT_CHOOSEMODE && startMode != FRAGMENT_ATTACKMODE)
            startMode = FRAGMENT_CHOOSEMODE;
        curMode = startMode;
        userFractionColor = Color.parseColor("#000000");
        white = Color.parseColor("#000000");

    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity != null && activity instanceof DialogFragmentDismissListener) {
            ((DialogFragmentDismissListener) activity).onDismissFragmentDialog(TAG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_location_action_chooser, container, false);
        mImageWrapper = (RelativeLayout) v.findViewById(R.id.layoutImageWrapper);
        mLocationName = (TextView) v.findViewById(R.id.locationName);
        mLocationImage = (LoadingImageView) v.findViewById(R.id.locationImage);
        mLocationImage.setPlaceholderResource(R.drawable.placeholder_location);
        ViewTreeObserver vto = mImageWrapper.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(this);
        }
        mTextWrapper = (RelativeLayout) v.findViewById(R.id.toolbarWrapper);
        buttonWrapper = (RelativeLayout) v.findViewById(R.id.buttonWrapper);
        mMainButton = (Button) v.findViewById(R.id.mainbutton);

        mAttackValueBar = (DiscreteSeekBar) v.findViewById(R.id.attackValueBar);
        mAttackValueBar.setMin(0);
        mAttackValueBar.setMax(mCurrentUser.getEnergy());
        mAttackValueBar.setOnProgressChangeListener(this);
        mCurEnergyValueText = (TextView) v.findViewById(R.id.curEnergyValueText);

        mMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curMode == FRAGMENT_ATTACKMODE) {
                    attack();
                } else {
                    if (getActivity() != null) {
                        startActivity(LocationDetailsActivity.getIntent(getActivity(), locationId));
                    }
                    LocationActionFragment.this.getDialog().dismiss();

                }
            }
        });
        mSecondButton = (Button) v.findViewById(R.id.secondbutton);
        mSecondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curMode != FRAGMENT_ATTACKMODE) {
                    if (inRange) initAttackMode();
                } else {
                    initChooseMode();
                }
            }
        });
        mLocationImage.setLoadingListener(this);
        if (curMode == FRAGMENT_ATTACKMODE) {
            initAttackMode();
        } else {
            initChooseMode();
        }
        return v;
    }


    public void initChooseMode() {
        curMode = FRAGMENT_CHOOSEMODE;
        refreshSecondButton();
        refreshMainButton();
        if(!this.isAdded()) return;
        mLocationName.setVisibility(View.VISIBLE);

        mCurEnergyValueText.setVisibility(View.GONE);
        mAttackValueBar.setVisibility(View.GONE);
    }

    private void refreshMainButton() {
        if(!this.isAdded()) return;
        mMainButton.setVisibility(View.VISIBLE);
        mMainButton.setEnabled(true);
        String buttonText = getString(R.string.attack);
        if (mOwnerFraction != null && mOwnerFraction.equals(Users.getCurrentS3User().getFraction())) {
            buttonText = getString(R.string.defend);
        }
        if(mOwnerFraction == null){
            buttonText = getString(R.string.conquer);
        }
        mMainButton.setText((curMode == FRAGMENT_CHOOSEMODE) ? getString(R.string.info) : buttonText);
    }

    public void refreshSecondButton() {
        if(!this.isAdded()) return;
        mSecondButton.setVisibility(View.VISIBLE);
        if (mCaptureData == null || mCurrentUser.getEnergy() == 0) {
            mSecondButton.setVisibility(View.GONE);
            return;
        }
        if (curMode == FRAGMENT_CHOOSEMODE) {
            String buttonText = getString(R.string.attack);
            if (mOwnerFraction != null && mOwnerFraction.getObjectId().equals(Users.getCurrentS3User().getFraction().getObjectId())) {
                buttonText = getString(R.string.defend);
            }
            if(mOwnerFraction == null){
                buttonText = getString(R.string.conquer);
            }
            mSecondButton.setEnabled(inRange);
            mSecondButton.setText(inRange ? buttonText : getString(R.string.out_of_range));
        } else if (curMode == FRAGMENT_ATTACKMODE) {
            if (curMode != startMode) {
                mSecondButton.setText(getString(R.string.back));
                mSecondButton.setEnabled(true);
            } else {
                mSecondButton.setVisibility(View.GONE);
            }

        }
    }

    public void initAttackMode() {
        curMode = FRAGMENT_ATTACKMODE;
        refreshSecondButton();
        refreshMainButton();
        if(!this.isAdded()) return;
        mAttackValueBar.setVisibility(View.VISIBLE);
        mCurEnergyValueText.setVisibility(View.VISIBLE);
        mLocationName.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();

        showLoading(true);

        Locations.findLocationForLocationID(locationId, new GetCallback<Location>() {
            @Override
            public void done(Location location, ParseException e) {
                if(LocationActionFragment.this.isDetached()) return;
                if (e == null) {
                    onLocationLoaded(location);
                } else {
                    LocationActionFragment.this.dismiss();
                }
            }
        });

        mCurrentUser.getFraction().fetchWhereAvailable(new GetCallback<Fraction>() {
            @Override
            public void done(Fraction userfraction, ParseException e) {
                userFractionColor = userfraction.getMainColor();
            }
        });
    }

    private int userFractionColor;

    private void showLoading(boolean show) {
        if(!this.isAdded()) return;
        mLocationImage.showLoading(show);
    }

    private ViewLocalizer localizer;

    private void onLocationLoaded(Location location) {
        if(!this.isAdded()) return;
        mLocation = location;
        refreshImage();
        refreshSecondButton();
        refreshMainButton();
        if (localizer != null) localizer.cancel();
        localizer = new ViewLocalizer(this.getActivity());
        localizer.setLocalizedStringOnTextView(location.getName(), mLocationName);
        if (mLocation.isCapturePoint()) {
            loadCaptureData();
        } else {
            refreshImage();
            refreshSecondButton();
            refreshMainButton();
        }
    }


    private void loadCaptureData(){
        mLocation.getCaptureData().fetchInBackground(new GetCallback<LocationCaptureData>() {
            @Override
            public void done(LocationCaptureData captureData, ParseException e) {
                if(!LocationActionFragment.this.isAdded()) return;
                mCaptureData = captureData;
                setEnergyText(mCaptureData.getEnergy());
                mMainButton.setText(getString(R.string.info));
                mMainButton.setEnabled(true);
                if (mCaptureData.hasOwnerFraction() && mCaptureData.getOwnerFraction()!=null) {
                    mCaptureData.getOwnerFraction().fetchWhereAvailable(new GetCallback<Fraction>() {
                        @Override
                        public void done(Fraction fraction, ParseException e) {
                            if(LocationActionFragment.this.isDetached()) return;
                            if(e==null) {
                                mOwnerFraction = fraction;
                                refreshImage();
                                refreshSecondButton();
                                refreshMainButton();
                            }
                        }
                    });
                }else{
                    refreshImage();
                    refreshSecondButton();
                    refreshMainButton();
                }
            }
        });
    }

    private void setOwnerFractionColorOnViews(){
        if(!isAdded()) return;
        int fractionColor = mOwnerFraction.getMainColor();
        int fractionTextColor = mOwnerFraction.getTextColor();
        mTextWrapper.setBackgroundColor(fractionColor);
        mSecondButton.setTextColor(fractionTextColor);
        mMainButton.setTextColor(fractionTextColor);
        mLocationName.setTextColor(fractionTextColor);
        buttonWrapper.setBackgroundColor(fractionColor);
        mCurEnergyValueText.setTextColor(fractionColor);
    }

    private void refreshImage() {
        mPicasso.cancelTag(TAG);
        if(!isAdded()) return;
        if (mLocation.hasHeaderImage()) {
            RequestCreator mPicassoLoadTmp = mPicasso.load(mLocation.getHeaderImage().getUrl()).tag(TAG).placeholder(R.drawable.placeholder_location);
            if (mOwnerFraction != null) {
                setOwnerFractionColorOnViews();
                mPicassoLoadTmp = mPicassoLoadTmp.transform(new ColorFilterTransformer(mOwnerFraction.getMainColor()));
                if (mOwnerFraction.hasLogo()) {
                    mPicassoLoadTmp = mPicassoLoadTmp.transform(new ImageOverlayTransformer(mOwnerFraction.getLogo().getUrl(), mPicasso, (int) (mLocationImage.getHeight() / fractionLogoSizeFactor)));
                }
            } else {
                mPicassoLoadTmp = mPicassoLoadTmp.transform(PaletteTransformer.instance());
            }
            mPicassoLoadTmp.into(mLocationImage);
        }
    }

    @Override
    public void onStartLoading(Drawable placeHolderDrawable) {
    }

    @Override
    public void onSuccessfullyFinishedLoading(Bitmap bmp, Picasso.LoadedFrom from) {
        if(!isAdded()) return;
        Palette mPalette = PaletteTransformer.getPalette(bmp);
        if (mPalette == null) return;
        Palette.Swatch swatch = mPalette.getVibrantSwatch() == null ? mPalette.getMutedSwatch() : mPalette.getVibrantSwatch();
        if (swatch != null) {
            mTextWrapper.setBackgroundColor(swatch.getRgb());
            buttonWrapper.setBackgroundColor(swatch.getRgb());
            mSecondButton.setTextColor(swatch.getTitleTextColor());
            mMainButton.setTextColor(swatch.getTitleTextColor());
            mLocationName.setTextColor(swatch.getTitleTextColor());
        }
    }

    @Override
    public void onFailure(Drawable errorDrawable) {
    }

    @Override
    public void dismiss() {
        mPicasso.cancelTag(TAG);
        super.dismiss();
    }

    public void setEnergyText(int energy) {
        if(!isAdded()) return;
        if (energy >= 1) {
            mCurEnergyValueText.setTextColor(mOwnerFraction != null ? mOwnerFraction.getMainColor() : getResources().getColor(R.color.white));
        }
        if (energy == 0) {
            mCurEnergyValueText.setTextColor(white);
        }
        if (energy <= -1) {
            mCurEnergyValueText.setTextColor(userFractionColor);
        }
        energy = Math.abs(energy);
        if (energy > 10000) {
            mCurEnergyValueText.setText((energy / 1000) + "k");
        } else {
            mCurEnergyValueText.setText("" + energy);
        }
    }

    private final static double fractionLogoSizeFactor = 3.0;

    private int mCurEnergyValue = 0;

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        int newEnergy = mCaptureData.getEnergy() + (mOwnerFraction != null && mOwnerFraction.getObjectId().equals(mCurrentUser.getFraction().getObjectId()) ? value : -1 * value);
        setEnergyText(newEnergy);
        mCurEnergyValue = value;
    }

    private boolean curAttacking = false;

    private void attack() {
        if (curAttacking) return;
        curAttacking = true;
        mMainButton.setText(getString(R.string.loading));
        mSecondButton.setVisibility(View.GONE);
        mMainButton.setEnabled(false);
        attackOrDefend(mCurEnergyValue, new  AttackDefendResponse.AttackDefenceCallback() {
            @Override
            public void done(AttackDefendResponse response) {
                mCurEnergyValueText.setVisibility(View.VISIBLE);
                if(response.hasError()){
                    LogUtils.e(TAG,"Failure while calling Attack Script", response.getError());
                }
                mCurEnergyValueText.setText(!response.wasSuccess() ? getString(R.string.error) : getString(R.string.success));
                if(response.hasAction()){
                    OttoEventBusProvider.getInstanceForUIThread().post(new ActionReceivedEvent(response.getAction()));
                }
                mMainButton.setEnabled(true);
                curAttacking = false;
                mMainButton.setText(getString(R.string.ok));
                mMainButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
            }
        });
    }

    protected void attackOrDefend(int energy,  AttackDefendResponse.AttackDefenceCallback callback) {
        Locations.attackOrDefend(Locale.getDefault().getISO3Language(), energy, locationId, getResources().getInteger(R.integer.api_version), new Date().getTime(), callback);
    }

    @Override
    public void onGlobalLayout() {
        int mPhotoHeightPixels = (int) (mImageWrapper.getWidth() / HEADER_ASPECT_RATIO);
        ViewGroup.LayoutParams lp;
        lp = mImageWrapper.getLayoutParams();
        if (lp.height != mPhotoHeightPixels) {
            lp.height = mPhotoHeightPixels;
            mImageWrapper.setLayoutParams(lp);
        }
        mLocationImage.setImageRatio(HEADER_ASPECT_RATIO);
    }
}
