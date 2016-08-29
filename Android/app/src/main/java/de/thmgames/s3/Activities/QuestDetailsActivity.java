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
import com.squareup.picasso.Picasso;

import de.thmgames.s3.Adapter.JobDetailsListAdapter;
import de.thmgames.s3.Controller.Locations;
import de.thmgames.s3.Controller.UserQuestRelations;
import de.thmgames.s3.Controller.ViewLocalizer;
import de.thmgames.s3.Fragments.DialogFragments.ParameterLoadingFragment;
import de.thmgames.s3.Fragments.JobDetailsListFragment;
import de.thmgames.s3.Model.ParseModels.LocationSystem.Location;
import de.thmgames.s3.Model.ParseModels.LocationSystem.LocationCaptureData;
import de.thmgames.s3.Model.ParseModels.Questsystem.Quest;
import de.thmgames.s3.Model.ParseModels.Questsystem.UserQuestRelation;
import de.thmgames.s3.Model.ParseModels.User;
import de.thmgames.s3.R;
import de.thmgames.s3.Transformer.PaletteTransformer;
import de.thmgames.s3.Utils.LogUtils;
import de.thmgames.s3.Views.Widgets.LoadingImageView;
import de.thmgames.s3.Views.Widgets.SlidingTabLayout;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class QuestDetailsActivity extends AbstractBaseActivity implements View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {

    public final static String TAG = QuestDetailsActivity.class.getName();
    private LoadingImageView mQuestPhotoView;
    private TextView mQuestName;
    private TextView mQuestDesc;
    private FloatingActionButton mActionButton;
    private ViewPager mViewPager;
    private SlidingTabLayout mSlidingTabLayout;

    private QuestDetailsPagerAdapter mSectionsPagerAdapter;
    private LinearLayout mQuestInfoWrapper;
    private SmoothProgressBar mProgressbar;

    private UserQuestRelation mS3UserQuestRel;

    private static final String INTENT_QUEST_ID = "quest.id";
    private String questId;

    public static Intent getIntent(Context ctx, String questId) {
        Intent i = new Intent(ctx, QuestDetailsActivity.class);
        i.putExtra(INTENT_QUEST_ID, questId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        lookForBeacons(true);
        needInternet(true);
        setNeededUserState(User.USERSTATE.LOGGEDIN);
        checkUserState(true);
        askForPushEnablingIfNotAskedBefore(false);
        setContentView(R.layout.activity_questdetails_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mProgressbar = (SmoothProgressBar) findViewById(R.id.loading_progress_linear);
        mQuestInfoWrapper = (LinearLayout) findViewById(R.id.questInfoWrapper);
        mQuestPhotoView = (LoadingImageView) findViewById(R.id.questImageHeader);
        mQuestPhotoView.setPlaceholderResource(R.drawable.placeholder_quest);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(2);
        mQuestName = (TextView) findViewById(R.id.questName);
        mQuestDesc = (TextView) findViewById(R.id.questDesc);
        mActionButton = (FloatingActionButton) findViewById(R.id.actionButton);
        mActionButton.setEnabled(false);
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);

        super.onCreate(savedInstanceState);

        ViewTreeObserver vto = mQuestPhotoView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(this);
        }
        if (getIntent() != null && getIntent().hasExtra(INTENT_QUEST_ID)) {
            questId = getIntent().getStringExtra(INTENT_QUEST_ID);
            loadQuestUserRel();
        } else {
            this.finish();
        }
    }


    @Override
    public void onResume() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCorrectUserState() {
    }

    private void loadQuestUserRel() {
        showLoadingProgress(true);
        UserQuestRelations.findUserQuestRelForQuestIDInBackground(questId, new GetCallback<UserQuestRelation>() {
            @Override
            public void done(UserQuestRelation userQuestRelation, ParseException e) {
                showLoadingProgress(false);
                if (e != null) {
                    showSnackBar(getString(R.string.error_loading_quest));
                    LogUtils.e(TAG, "Error while retrieving Questdata", e);
                }else{
                    initiateViews(userQuestRelation);
                }
            }
        }, UserQuestRelation.QUESTUSERREL_QUEST, UserQuestRelation.QUESTUSERREL_JOB_ACTIVE, UserQuestRelation.QUESTUSERREL_JOB_FINISHED, UserQuestRelation.QUESTUSERREL_JOBS_NEXT);
    }

    @Override
    public void onDismissFragmentDialog(String fragmentTag){
        super.onDismissFragmentDialog(fragmentTag);
        if(fragmentTag.contains(ParameterLoadingFragment.class.getName())){
            loadQuestUserRel();
        }
    }

    private Quest mQuest;
    private ViewLocalizer mViewLocalizer;

    private void initiateViews(UserQuestRelation userQuestRelation) {
        mS3UserQuestRel = userQuestRelation;
        if (mS3UserQuestRel.getActiveJob() == null) {
            mActionButton.setEnabled(false);
            mActionButton.setColorNormalResId(R.color.grey);
        } else {
            mActionButton.setColorNormalResId(R.color.accent);
            mActionButton.setEnabled(true);
        }
        mActionButton.setOnClickListener(this);
        showLoadingProgress(true);
        userQuestRelation.getQuest().fetchIfNeededInBackground(
                new GetCallback<Quest>() {
                    @Override
                    public void done(Quest parseObject, ParseException e) {
                        showLoadingProgress(false);
                        if (e != null) {
                            showSnackBar(getString(R.string.error_loading_quest));
                            LogUtils.e(TAG, "Error while retrieving Questdata", e);
                            return;
                        }
                        mQuest = parseObject;
                        loadQuestImage();
                        if (mViewLocalizer != null) {
                            mViewLocalizer.cancel();
                        }
                        mViewLocalizer = new ViewLocalizer(QuestDetailsActivity.this);
                        mViewLocalizer.setLocalizedStringOnTextView(mQuest.getName(), mQuestName);
                        mViewLocalizer.setLocalizedStringOnTextView(mQuest.getShortDescription(), mQuestDesc);

                        if(mSectionsPagerAdapter==null){
                            mSectionsPagerAdapter = new QuestDetailsPagerAdapter(getFragmentManager());
                            mViewPager.setAdapter(mSectionsPagerAdapter);
                            mSlidingTabLayout.setViewPager(mViewPager);
                        }else{
                            mSectionsPagerAdapter.notifyDataSetChanged();
                        }

                    }
                }
        );
    }

    public void loadQuestImage() {
        if (!mQuest.hasImage()) return;
        mQuestPhotoView.setLoadingListener(new LoadingImageView.ImageLoadingCallback() {
            @Override
            public void onStartLoading(Drawable placeHolderDrawable) {

            }

            @Override
            public void onSuccessfullyFinishedLoading(Bitmap bmp, Picasso.LoadedFrom from) {
                Palette palette = PaletteTransformer.getPalette(bmp);
                if (palette == null) return;
                Palette.Swatch swatch = palette.getVibrantSwatch() == null ? palette.getMutedSwatch() : palette.getVibrantSwatch();

                if (swatch != null) {
                    mQuestInfoWrapper.setBackgroundColor(swatch.getRgb());
                    mSlidingTabLayout.setBackgroundColor(swatch.getRgb());
                    if (mS3UserQuestRel.getActiveJob() != null)
                        mActionButton.setColorNormal(swatch.getRgb());
                    mQuestName.setTextColor(swatch.getTitleTextColor());
                    mQuestDesc.setTextColor(swatch.getBodyTextColor());
                    mSlidingTabLayout.setTextColor(swatch.getTitleTextColor());
                }

            }

            @Override
            public void onFailure(Drawable errorDrawable) {

            }
        });
        mPicasso.load(mQuest.getImage().getUrl())
                .transform(PaletteTransformer.instance())
                .placeholder(R.drawable.placeholder_quest)
                .into(mQuestPhotoView);
    }

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
    public void onSnackbarShown(int height) {
    }

    @Override
    public void onSnackbarDismissed(int height) {

    }

    @Override
    public void onClick(View v) {
        if (v.equals(mActionButton)) {
            tryFinishActiveJobInQuestUserRel(mS3UserQuestRel);
        }
    }

    @Override
    public void onGlobalLayout() {
        mQuestPhotoView.setImageRatio(HEADER_ASPECT_RATIO);
    }


    public class QuestDetailsPagerAdapter extends FragmentPagerAdapter {

        public QuestDetailsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ParameterLoadingFragment.newInstanceForWebElement(mQuest.getDescription() != null ? mQuest.getDescription().getObjectId() : "", false);
                case 1:
                    return ParameterLoadingFragment.newInstanceForWebElement( mQuest.getLore() != null ? mQuest.getLore().getObjectId() : "", false);
                case 2:
                default:
                    return JobDetailsListFragment.newInstance(mS3UserQuestRel.getQuestId());
            }

        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.info);
                case 1:
                    return getString(R.string.story);
                default:
                    return getString(R.string.jobs);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

    }
}
