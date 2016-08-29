package de.thmgames.s3.Fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import de.thmgames.s3.Adapter.UserHighscoreListAdapter;
import de.thmgames.s3.Adapter.ViewPagerAdapter;
import de.thmgames.s3.Controller.Fractions;
import de.thmgames.s3.Controller.Users;
import de.thmgames.s3.Model.ParseModels.Fraction;
import de.thmgames.s3.Model.ParseModels.User;
import de.thmgames.s3.R;
import de.thmgames.s3.Utils.LayoutUtils;
import de.thmgames.s3.Utils.LogUtils;
import de.thmgames.s3.Views.Widgets.SlidingTabLayout;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * A simple {@link android.app.Fragment} subclass.
 */
public class HighscoreFragment extends BaseFragment {
    private final static String TAG = HighscoreFragment.class.getName();
    private UserHighscoreListAdapter mUserHighscoreListAdapter;
    private SmoothProgressBar mProgressLinearView;
    private LayoutInflater mInflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mInflater = inflater;
        View v = inflater.inflate(R.layout.fragment_highscore_layout, container, false);
        HighScorePagerAdapter mPagerAdapter = new HighScorePagerAdapter();
        ViewPager mViewPager = (ViewPager) v.findViewById(R.id.viewpager);
        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) v.findViewById(R.id.sliding_tabs);
        mProgressLinearView = (SmoothProgressBar) v.findViewById(R.id.loading_progress_linear);
        mViewPager.setAdapter(mPagerAdapter);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);
        mUserHighscoreListAdapter = new UserHighscoreListAdapter(getActivity());
        mViewPager.setCurrentItem(0);
        return v;
    }

    @Override
    protected void showLoadingAnimation() {
        mProgressLinearView.setVisibility(View.VISIBLE);
        mProgressLinearView.animate().setDuration(shortAnimTime).alpha(
                1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressLinearView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void hideLoadingAnimation() {
        mProgressLinearView.setVisibility(View.GONE);
        mProgressLinearView.animate().setDuration(shortAnimTime).alpha(
                0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressLinearView.setVisibility(View.GONE);
            }
        });
    }


    public class HighScorePagerAdapter extends ViewPagerAdapter {
        private PieChart mPieChart;
        private RelativeLayout mWrapper;

        @Override
        public CharSequence getPageTitle(int position) {
            return position == 0 ? getString(R.string.player) : getString(R.string.fractions);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public View getView(int position, ViewPager pager) {
            if (position == 0) {
                if (mWrapper != null) {
                    return mWrapper;
                }
                mWrapper = (RelativeLayout) mInflater.inflate(R.layout.refreshing_listview, pager, false);
                final SwipeRefreshLayout mUserHighScoreListRefreshView = (SwipeRefreshLayout) mWrapper.findViewById(R.id.swipe_container);
                final ListView mUserHighscoreList = (ListView) mWrapper.findViewById(android.R.id.list);
                mUserHighscoreList.setOnItemClickListener(null);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !LayoutUtils.isInLandscape(HighscoreFragment.this.getActivity())) {
                    mUserHighscoreList.setPadding(0, 0, 0, LayoutUtils.getNavigationBarHeight(HighscoreFragment.this.getActivity()));
                    mUserHighscoreList.setClipToPadding(false);
                }
                final TextView mEmptyView = (TextView) mWrapper.findViewById(R.id.emptyTextView);
                mUserHighscoreList.setDivider(null);
                mEmptyView.setText(getString(R.string.loading));
                mUserHighscoreList.setEmptyView(mEmptyView);
                mUserHighscoreList.setAdapter(mUserHighscoreListAdapter);
                mUserHighscoreList.setItemsCanFocus(false);
                showLoadingProgress(true);

                Users.findUsersOrderedByPoints(new FindCallback<User>() {
                    @Override
                    public void done(List<User> users, ParseException e) {
                        showLoadingProgress(false);
                        mUserHighScoreListRefreshView.setRefreshing(false);
                        if (e != null) {
                            mEmptyView.setText(getString(R.string.error_loading_informations));
                            showSnackBar(getString(R.string.error_loading_userhighscore));
                            LogUtils.e(TAG, "Error while dling userhighscore", e);
                            return;
                        }
                        if (users.size() == 0) {
                            mEmptyView.setText(getString(R.string.no_data_found));
                        }
                        mUserHighscoreListAdapter.setData(users);
                    }
                });

                mUserHighScoreListRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Users.findUsersOrderedByPoints(new FindCallback<User>() {
                            @Override
                            public void done(List<User> users, ParseException e) {
                                mUserHighScoreListRefreshView.setRefreshing(false);
                                if (e != null) {
                                    mEmptyView.setText(getString(R.string.error_loading_informations));
                                    showSnackBar(getString(R.string.error_loading_userhighscore));
                                    LogUtils.e(TAG, "Error while dling userhighscore", e);
                                    return;
                                }
                                if (users.size() == 0) {
                                    mEmptyView.setText(getString(R.string.no_data_found));
                                }
                                mUserHighscoreListAdapter.setData(users);
                            }
                        });
                    }
                });

                return mWrapper;
            } else {
                if (mPieChart == null) {
                    mPieChart = new PieChart(getActivity());
                    mPieChart.setDrawLegend(false);
                    mPieChart.setDrawCenterText(false);
                    mPieChart.setValueTextSize(10f);
                    mPieChart.setDescription("");
                    mPieChart.setOnDragListener(null);
                    mPieChart.setNoDataText(getString(R.string.loading));
                    mPieChart.setEnabled(false);
                }
                showLoadingProgress(true);
                Fractions.findAllVisibleFractionsOrderedByKeyInBackground(Fraction.FRACTION_POINTS, false, new FindCallback<Fraction>() {
                    @Override
                    public void done(List<Fraction> fractions, ParseException e) {
                        showLoadingProgress(false);
                        if (e != null) {
                            LogUtils.e(TAG, "Error while dling fractionhighscore", e);
                            showSnackBar(getString(R.string.error_loading_fractionhighscore));
                            mPieChart.setNoDataText(getString(R.string.error_loading_informations));
                            return;
                        }
                        mPieChart.setDrawYValues(true);
                        mPieChart.setNoDataText(getString(R.string.no_data_found));
                        ArrayList<Entry> vals = new ArrayList<Entry>();
                        ArrayList<Integer> colors = new ArrayList<Integer>();
                        ArrayList<String> xVals = new ArrayList<String>();

                        int i = 0;
                        for (Fraction fraction : fractions) {
                            vals.add(new Entry(fraction.getPoints(), i++));
                            colors.add(fraction.getMainColor());
                            xVals.add(fraction.getName().getMessageForDefaultLocale());

                        }
                        PieDataSet set1 = new PieDataSet(vals, "Fraktionenrangliste");
                        set1.setSliceSpace(5f);

                        set1.setColors(colors);
                        set1.setSelectionShift(10);
                        PieData data = new PieData(xVals, set1);
                        mPieChart.setValueFormatter(new ValueFormatter() {
                            @Override
                            public String getFormattedValue(float v) {
                                return "" + (int) v;
                            }
                        });
                        mPieChart.setData(data);
                        mPieChart.refreshDrawableState();
                    }
                });
                return mPieChart;
            }
        }
    }
}
