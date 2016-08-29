package de.thmgames.s3.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.SaveCallback;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.RequestCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.thmgames.s3.Controller.Fractions;
import de.thmgames.s3.Controller.ViewLocalizer;
import de.thmgames.s3.Fragments.DialogFragments.ParameterLoadingFragment;
import de.thmgames.s3.Model.ParseModels.Fraction;
import de.thmgames.s3.Otto.Events.ActionReceivedEvent;
import de.thmgames.s3.R;
import de.thmgames.s3.Transformer.ColorFilterTransformer;
import de.thmgames.s3.Transformer.ImageOverlayTransformer;
import de.thmgames.s3.Utils.LayoutUtils;
import de.thmgames.s3.Utils.LogUtils;
import de.thmgames.s3.Views.Widgets.LoadingImageView;
import de.thmgames.s3.Views.Widgets.SlidingTabLayout;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import uk.me.lewisdeane.ldialogs.CustomDialog;

import static de.thmgames.s3.Model.ParseModels.User.USERSTATE;



public class FractionChooserActivity extends AbstractBaseActivity implements OnPageChangeListener, View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {

    private final static String TAG = FractionChooserActivity.class.getName();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    private FractionPagerAdapter mFractionPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private LoadingImageView mFractionHeader;
    private CircularProgressBar mCircularProgressView;
    private FloatingActionButton mAcceptButton;
    private SlidingTabLayout mSlidingTabLayout;
    private RelativeLayout mContentWrapper;

    private Fraction mCurrentFraction;

    public static Intent getIntent(Context ctx) {
        return new Intent(ctx, FractionChooserActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        lookForBeacons(false);
        needInternet(true);
        askForPushEnablingIfNotAskedBefore(false);
        setNeededUserState(USERSTATE.MISSING_FRACTION);
        checkUserState(true);

        setContentView(R.layout.activity_fraction_chooser);
        mContentWrapper = (RelativeLayout) findViewById(R.id.content_wrapper);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mActionBarBackgroundDrawable = mToolbar.getBackground();
        mCircularProgressView = (CircularProgressBar) findViewById(R.id.loading_progress_circular);
        mFractionHeader = (LoadingImageView) findViewById(R.id.fractionImageHeader);
        mFractionHeader.setPlaceholderResource(R.drawable.placeholder_fraction);
        mAcceptButton = (FloatingActionButton) findViewById(R.id.acceptButton);
        mAcceptButton.setOnClickListener(FractionChooserActivity.this);
        ((ViewGroup.MarginLayoutParams) mAcceptButton.getLayoutParams()).bottomMargin = ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !LayoutUtils.isInLandscape(this)) ? LayoutUtils.getNavigationBarHeight(this) : 0) +LayoutUtils.dpToPx(10, this);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mFractionPagerAdapter = new FractionPagerAdapter(getFragmentManager());
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setOnPageChangeListener(FractionChooserActivity.this);
        mViewPager.setAdapter(mFractionPagerAdapter);
        mAcceptButton.hide(false);

        ViewTreeObserver vto = mContentWrapper.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(this);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSupportActionBar(mToolbar);
        mActionBarBackgroundDrawable.setAlpha(0);
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mContentWrapper == null) {
            return;
        }
        ViewTreeObserver vto = mContentWrapper.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.removeGlobalOnLayoutListener(this);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
        mPicasso.cancelTag(TAG);
    }

    @Override
    protected void onCorrectUserState() {

    }


    private int[] mFractionColors;

    private final int MAX_FRAGMENTS_IN_MEMORY=10;
    public void loadData() {
        showLoadingProgress(true);
        Fractions.findAllVisibleFractionsOrderedByKeyInBackground(Fraction.FRACTION_POINTS, true, new FindCallback<Fraction>() {
            @Override
            public void done(List<Fraction> fractions, ParseException e) {
                if (FractionChooserActivity.this.isFinishing()) return;
                if (e != null) {
                    showSnackBar(getString(R.string.error_loading_fraction_list));
                    LogUtils.e(TAG, "Error while loading Fractions", e);
                    return;
                }
                mContentWrapper.setVisibility(View.VISIBLE);
                showLoadingProgress(false);
                mAcceptButton.show(true);
                mViewPager.setOffscreenPageLimit(fractions.size()<=MAX_FRAGMENTS_IN_MEMORY?fractions.size():MAX_FRAGMENTS_IN_MEMORY);
                mFractionPagerAdapter.setData(fractions);
                mSlidingTabLayout.setViewPager(mViewPager);
                mFractionColors = new int[fractions.size()];
                for (int i = 0; i < fractions.size(); i++) {
                    Fraction fraction = fractions.get(i);
                    mFractionColors[i] = fraction.getMainColor();
                }
                mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.white));
                setCurrentFraction(0);
                startHeight = mAcceptButton.getY();
            }
        });
    }

    private float startHeight;

    @Override
    public void onSnackbarShown(final int height) {
        if (mAcceptButton.isShown())
            mAcceptButton.animate().translationY(startHeight - LayoutUtils.dpToPx(height, FractionChooserActivity.this)).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mAcceptButton.setY(startHeight - LayoutUtils.dpToPx(height, FractionChooserActivity.this));
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mAcceptButton.setY(startHeight - LayoutUtils.dpToPx(height, FractionChooserActivity.this));
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();
    }

    @Override
    public void onSnackbarDismissed(final int height) {

        if (mAcceptButton.isShown())
            mAcceptButton.animate().translationY(startHeight).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mAcceptButton.setY(startHeight);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mAcceptButton.setY(startHeight);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();
    }


    @Override
    protected void showLoadingAnimation() {
        mAcceptButton.hide();
        mCircularProgressView.setVisibility(View.VISIBLE);
        mCircularProgressView.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCircularProgressView.setVisibility(View.VISIBLE);
            }
        }).start();
    }

    @Override
    protected void hideLoadingAnimation() {
        mAcceptButton.show();
        mCircularProgressView.setVisibility(View.GONE);
        mCircularProgressView.animate().setDuration(shortAnimTime).alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCircularProgressView.setVisibility(View.GONE);
            }
        }).start();
    }

    @Subscribe
    @Override
    public void onActionReceived(ActionReceivedEvent action) {
        super.onActionReceived(action);
    }

    private ViewLocalizer localizer;
    public void setCurrentFraction(int position) {
        mCurrentFraction = mFractionPagerAdapter.getFraction(position);
        if (mCurrentFraction == null) return;
        mPicasso.cancelRequest(mFractionHeader);
        if(localizer!=null){
            localizer.cancel();
        }
        localizer=new ViewLocalizer(this);
        localizer.setLocalizedStringOnTextView(mCurrentFraction.getName(), mToolbar);
        if (mCurrentFraction.hasHeaderImage()) {
            RequestCreator mPicassoLoadTmp = mPicasso.load(mCurrentFraction.getHeaderImg().getUrl())
                    .resize(mFractionHeader.getWidth(), mFractionHeader.getHeight())
                    .tag(TAG)
                    .placeholder(R.drawable.placeholder_fraction)
                    .transform(new ColorFilterTransformer(mFractionColors[position]));

            if (mCurrentFraction.hasLogo())
                mPicassoLoadTmp = mPicassoLoadTmp.transform(new ImageOverlayTransformer(mCurrentFraction.getLogo().getUrl(), mPicasso, mFractionHeader.getHeight() / 3));
            mPicassoLoadTmp.into(mFractionHeader);
        }
        mAcceptButton.setColorNormal(mFractionColors[position]);
    }

    @Override
    /**
     * @param position:             Position index of the first page currently being displayed. Page position+1 will be visible if positionOffset is nonzero.
     * @param positionOffset:       Value from [0, 1) indicating the offset from the page at position.
     * @param positionOffsetPixels: Value in pixels indicating the offset from position.
     */
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }


    @Override
    public void onPageSelected(int position) {
        setCurrentFraction(position);
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }


    public void showConfirmDialog(Fraction choosenFraction) {
        // Create the builder with required paramaters - Context, Title, Positive Text
        CustomDialog.Builder builder = new CustomDialog.Builder(this, getString(R.string.dialog_fractionchooser_title), getString(R.string.accept));

        // Now we can any of the following methods.
        builder.content(getString(R.string.you_have) + choosenFraction.getName().getMessageForDefaultLocale() + getString(R.string.choosen_sure));
        builder.negativeText(getString(R.string.cancel));
        builder.negativeColor(Color.RED);
        builder.positiveColor(Color.WHITE);
        builder.darkTheme(true);
        // Now we can build the dialog.
        final CustomDialog customDialog = builder.build();
        customDialog.setClickListener(new CustomDialog.ClickListener() {
            @Override
            public void onConfirmClick() {
                customDialog.dismiss();
                showLoadingProgress(true);
                mCurrentUser.setFraction(mCurrentFraction);
                mCurrentUser.setUserPoints(0);
                mCurrentUser.setEnergy(100);
                mCurrentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            startActivity(MainActivity.getIntent(FractionChooserActivity.this));
                        } else {
                            showLoadingProgress(false);
                            showSnackBar(getString(R.string.error_saving_userdata));
                            LogUtils.e(TAG, "Error while setting Fraction or User", e);
                        }
                    }
                });
            }

            @Override
            public void onCancelClick() {
                showLoadingProgress(false);
                customDialog.dismiss();
            }
        });
        customDialog.show();
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.acceptButton) {
            showConfirmDialog(mCurrentFraction);
        }
    }

    @Override
    public void onGlobalLayout() {
        mFractionHeader.setImageRatio(HEADER_ASPECT_RATIO);
    }


    public class FractionPagerAdapter extends FragmentPagerAdapter  {

        private List<Fraction> mFractions = new ArrayList<>();

        public FractionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fraction getFraction(int pos) {
            return mFractions.get(pos);
        }

        public void setData(List<Fraction> mNewFractions) {
            mFractions = mNewFractions;
            this.notifyDataSetChanged();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFractions.get(position).getName().getMessageForLocale(Locale.getDefault());
        }

        @Override
        public int getCount() {
            return mFractions.size();
        }

        @Override
        public Fragment getItem(int position) {
                Fraction fraction = mFractions.get(position);
                return ParameterLoadingFragment.newInstanceForWebElement( fraction.getLore() != null ? fraction.getLore().getObjectId() : "", false);

        }
    }

}
