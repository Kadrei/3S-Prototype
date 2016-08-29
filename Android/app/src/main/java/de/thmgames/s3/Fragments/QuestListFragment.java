package de.thmgames.s3.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import de.thmgames.s3.Activities.QuestDetailsActivity;
import de.thmgames.s3.Adapter.QuestListAdapter;
import de.thmgames.s3.Adapter.ViewPagerAdapter;
import de.thmgames.s3.Controller.UserQuestRelations;
import de.thmgames.s3.Controller.Users;
import de.thmgames.s3.Model.ParseModels.Questsystem.UserQuestRelation;
import de.thmgames.s3.R;
import de.thmgames.s3.Utils.LayoutUtils;
import de.thmgames.s3.Utils.LogUtils;
import de.thmgames.s3.Views.EndlessQuestListView;
import de.thmgames.s3.Views.Widgets.SlidingTabLayout;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * A simple {@link Fragment} subclass.
 */
public class QuestListFragment extends BaseFragment {
    private final static String TAG = QuestListFragment.class.getName();
    private QuestListAdapter mUnactiveQuestAdapter;
    private QuestListAdapter mActiveQuestAdapter;
    private QuestListAdapter mHistoryQuestAdapter;
    private SmoothProgressBar mProgressLinearView;
    private LayoutInflater mInflater;
    private boolean isCancelledQuestVisible = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_questlist, container, false);
        mInflater = inflater;
        QuestListPagerAdapter mPagerAdapter = new QuestListPagerAdapter();
        ViewPager mViewPager = (ViewPager) v.findViewById(R.id.viewpager);
        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) v.findViewById(R.id.sliding_tabs);
        mProgressLinearView = (SmoothProgressBar) v.findViewById(R.id.loading_progress_linear);
        mViewPager.setAdapter(mPagerAdapter);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);
        mHistoryQuestAdapter = new QuestListAdapter(getActivity());
        mHistoryQuestAdapter.showCancelled(isCancelledQuestVisible);
        mHistoryQuestAdapter.setData(new ArrayList<UserQuestRelation>());

        mUnactiveQuestAdapter = new QuestListAdapter(getActivity());
        mUnactiveQuestAdapter.showCancelled(isCancelledQuestVisible);
        mUnactiveQuestAdapter.setData(new ArrayList<UserQuestRelation>());

        mActiveQuestAdapter = new QuestListAdapter(getActivity());
        mActiveQuestAdapter.showCancelled(isCancelledQuestVisible);
        mActiveQuestAdapter.setData(new ArrayList<UserQuestRelation>());

        this.setHasOptionsMenu(true);
        mViewPager.setCurrentItem(0);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_questlist, menu);
        // set filter icon state
        menu.findItem(R.id.menu_action_shows_filter)
                .setIcon(isCancelledQuestVisible ?
                        R.drawable.ic_action_filter_selected : R.drawable.ic_action_filter);
        // set filter check box states
        menu.findItem(R.id.menu_action_quests_show_cancelled)
                .setChecked(isCancelledQuestVisible);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_action_quests_show_cancelled) {
            isCancelledQuestVisible = !isCancelledQuestVisible;
            mActiveQuestAdapter.showCancelled(isCancelledQuestVisible);
            mActiveQuestAdapter.notifyDataSetChanged();
            mUnactiveQuestAdapter.showCancelled(isCancelledQuestVisible);
            mUnactiveQuestAdapter.notifyDataSetChanged();
            mHistoryQuestAdapter.showCancelled(isCancelledQuestVisible);
            mHistoryQuestAdapter.notifyDataSetChanged();
            // refresh filter icon state
            getActivity().invalidateOptionsMenu();
        }
        return true;
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

    private class QuestClickListener implements AdapterView.OnItemClickListener {

        private QuestListAdapter adapter;

        public QuestClickListener(QuestListAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            QuestListFragment.this.startActivity(QuestDetailsActivity.getIntent(QuestListFragment.this.getActivity(), adapter.getItem(position).getQuest().getObjectId()));
        }
    }

    public class QuestListPagerAdapter extends ViewPagerAdapter {
        private RelativeLayout[] views = new RelativeLayout[3];

        @Override
        public CharSequence getPageTitle(int position) {
            return position == 0 ? getActivity().getString(R.string.active) : (position == 1 ? getActivity().getString(R.string.open) : getActivity().getString(R.string.history));
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public View getView(int position, ViewPager pager) {
            if (position < views.length && views[position] != null) return views[position];
            if (QuestListFragment.this.getActivity() == null || mInflater == null) return null;
            RelativeLayout mWrapper = (RelativeLayout) mInflater.inflate(R.layout.refreshing_quest_listview, pager, false);
            SwipeRefreshLayout mQuestListView = (SwipeRefreshLayout) mWrapper.findViewById(R.id.swipe_container);
            EndlessQuestListView mRefreshListView = (EndlessQuestListView) mWrapper.findViewById(R.id.questListView);
            TextView mEmptyView = (TextView) mWrapper.findViewById(R.id.emptyView);
            mEmptyView.setGravity(Gravity.CENTER);
            mEmptyView.setText(getActivity().getString(R.string.loading_quests));
            mRefreshListView.setLoadingView(R.layout.loading_layout);
            mRefreshListView.setDivider(null);
            mRefreshListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            mRefreshListView.setEmptyView(mEmptyView);
            mRefreshListView.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !LayoutUtils.isInLandscape(QuestListFragment.this.getActivity())) {
                mRefreshListView.setPadding(0, 0, 0, LayoutUtils.getNavigationBarHeight(QuestListFragment.this.getActivity()));
                mRefreshListView.setClipToPadding(false);
            }
            switch (position) {
                case 0:
                    mRefreshListView.setAdapter(mActiveQuestAdapter);
                    mRefreshListView.setOnItemClickListener(new QuestClickListener(mActiveQuestAdapter));
                    break;
                case 2:
                    mRefreshListView.setAdapter(mHistoryQuestAdapter);
                    mRefreshListView.setOnItemClickListener(new QuestClickListener(mHistoryQuestAdapter));
                    break;
                case 1:
                default:
                    mRefreshListView.setAdapter(mUnactiveQuestAdapter);
                    mRefreshListView.setOnItemClickListener(new QuestClickListener(mUnactiveQuestAdapter));
            }
            mQuestListView.setColorSchemeColors(R.color.accent, R.color.primary);
            QuestLoader loader = new QuestLoader(position, 100, mRefreshListView, mQuestListView, mEmptyView);
            mRefreshListView.setListener(loader);
            mQuestListView.setOnRefreshListener(loader);
            mQuestListView.setRefreshing(true);
            loader.onRefresh();
            views[position] = mWrapper;
            return mWrapper;
        }
    }

    public class QuestLoader implements SwipeRefreshLayout.OnRefreshListener, EndlessQuestListView.EndlessListener {
        public int mode;
        public int limit;
        public int itemsloaded;
        private EndlessQuestListView mViewToLoadInto;
        private SwipeRefreshLayout mRefreshLayout;
        private TextView mEmptyView;
        public final static int MODE_ACTIVE = 0;
        public final static int MODE_UNACTIVE = 1;
        public final static int MODE_HISTORY = 2;

        public final int DEFAULT_LIMIT = 25;

        public QuestLoader(int mode, int limit, EndlessQuestListView mViewToLoadInto, SwipeRefreshLayout mRefreshLayout, TextView emptyView) {
            this.mode = mode;
            this.limit = limit;
            this.mViewToLoadInto = mViewToLoadInto;
            this.mRefreshLayout = mRefreshLayout;
            this.mEmptyView = emptyView;
            itemsloaded = 0;
        }

        @Override
        public void onRefresh() {
            showLoadingProgress(true);
            switch (mode) {
                case MODE_ACTIVE:
                    UserQuestRelations.getActiveQuestUserRelationsForUser(Users.getCurrentS3User(), new FindCallback<UserQuestRelation>() {
                        @Override
                        public void done(List<UserQuestRelation> s3QuestsUserRel, ParseException e) {
                            showLoadingProgress(false);
                            if (e != null) {
                                showSnackBar(getString(R.string.quests_loading_active_error));
                                LogUtils.e(TAG, "Error while dling active quests", e);
                                mEmptyView.setText(getString(R.string.quests_loading_active_error));
                                return;
                            }
                            mEmptyView.setText(getActivity().getString(R.string.quests_no_active_found));
                            mEmptyView.setGravity(Gravity.CENTER);
                            itemsloaded = s3QuestsUserRel.size();
                            mViewToLoadInto.setDataFromQuestUserRel(s3QuestsUserRel);
                            mViewToLoadInto.setHasAllData(itemsloaded < DEFAULT_LIMIT);
                            mRefreshLayout.setRefreshing(false);
                        }
                    }, DEFAULT_LIMIT, 0, UserQuestRelation.QUESTUSERREL_QUEST, UserQuestRelation.QUESTUSERREL_JOB_ACTIVE, UserQuestRelation.QUESTUSERREL_JOB_FINISHED, UserQuestRelation.QUESTUSERREL_JOBS_NEXT);
                    break;
                case MODE_HISTORY:
                    UserQuestRelations.getHistoryQuestUserRelationsForUser(Users.getCurrentS3User(), new FindCallback<UserQuestRelation>() {
                        @Override
                        public void done(List<UserQuestRelation> s3QuestsUserRel, ParseException e) {
                            showLoadingProgress(false);
                            if (e != null) {
                                showSnackBar(getString(R.string.quest_loading_history_error));
                                LogUtils.e(TAG, " Error while dling closed and cancelled quests", e);
                                mEmptyView.setText(getString(R.string.quest_loading_history_error));
                                return;
                            }
                            itemsloaded = s3QuestsUserRel.size();
                            mViewToLoadInto.setDataFromQuestUserRel(s3QuestsUserRel);
                            mEmptyView.setText(getActivity().getString(R.string.quest_no_history_found));
                            mEmptyView.setGravity(Gravity.CENTER);
                            mViewToLoadInto.setHasAllData(itemsloaded < DEFAULT_LIMIT);
                            mRefreshLayout.setRefreshing(false);
                        }
                    }, DEFAULT_LIMIT, 0, UserQuestRelation.QUESTUSERREL_QUEST, UserQuestRelation.QUESTUSERREL_JOB_ACTIVE, UserQuestRelation.QUESTUSERREL_JOB_FINISHED, UserQuestRelation.QUESTUSERREL_JOBS_NEXT);
                    break;
                case MODE_UNACTIVE:
                    UserQuestRelations.findAllValidInactiveQuests(Users.getCurrentS3User(), new FindCallback<UserQuestRelation>() {
                        @Override
                        public void done(List<UserQuestRelation> userQuestRelationList, ParseException e) {
                            showLoadingProgress(false);
                            if (e != null) {
                                showSnackBar(getString(R.string.quests_loading_open_error));
                                LogUtils.e(TAG, " Error while dling unactive quests", e);
                                mEmptyView.setText(getString(R.string.quests_loading_open_error));
                                return;
                            }
                            itemsloaded = userQuestRelationList.size();
                            mViewToLoadInto.setDataFromQuestUserRel(userQuestRelationList);
                            mEmptyView.setText(getString(R.string.quest_open_no_found));
                            mEmptyView.setGravity(Gravity.CENTER);
                            mViewToLoadInto.setHasAllData(itemsloaded < DEFAULT_LIMIT);
                            mRefreshLayout.setRefreshing(false);
                        }
                    }, getAPIVersion(), DEFAULT_LIMIT, 0);
                    break;
            }

        }

        @Override
        public void loadData() {
            showLoadingProgress(true);
            switch (mode) {
                case MODE_ACTIVE:
                    UserQuestRelations.getActiveQuestUserRelationsForUser(Users.getCurrentS3User(), new FindCallback<UserQuestRelation>() {
                        @Override
                        public void done(List<UserQuestRelation> s3QuestsUserRel, ParseException e) {
                            showLoadingProgress(false);
                            if (e != null) {
                                showSnackBar(getString(R.string.quests_loading_active_error));
                                LogUtils.e(TAG, " Error while dling additional active quests", e);
                                return;
                            }
                            itemsloaded += s3QuestsUserRel.size();
                            mViewToLoadInto.addNewDataFromQuestUserRel(s3QuestsUserRel);

                            mViewToLoadInto.setHasAllData(s3QuestsUserRel.size() < DEFAULT_LIMIT);
                            mRefreshLayout.setRefreshing(false);
                        }
                    }, DEFAULT_LIMIT, itemsloaded, UserQuestRelation.QUESTUSERREL_QUEST, UserQuestRelation.QUESTUSERREL_JOB_ACTIVE, UserQuestRelation.QUESTUSERREL_JOB_FINISHED, UserQuestRelation.QUESTUSERREL_JOBS_NEXT);
                    break;
                case MODE_HISTORY:
                    UserQuestRelations.getHistoryQuestUserRelationsForUser(Users.getCurrentS3User(), new FindCallback<UserQuestRelation>() {
                        @Override
                        public void done(List<UserQuestRelation> s3QuestsUserRel, ParseException e) {
                            showLoadingProgress(false);
                            if (e != null) {
                                showSnackBar(getString(R.string.quest_loading_history_error));
                                LogUtils.e(TAG, " Error while dling additional closed quests", e);
                                return;
                            }
                            mViewToLoadInto.addNewDataFromQuestUserRel(s3QuestsUserRel);
                            itemsloaded += s3QuestsUserRel.size();
                            mViewToLoadInto.setHasAllData(s3QuestsUserRel.size() < DEFAULT_LIMIT);
                            mRefreshLayout.setRefreshing(false);
                        }
                    }, DEFAULT_LIMIT, itemsloaded, UserQuestRelation.QUESTUSERREL_QUEST, UserQuestRelation.QUESTUSERREL_JOB_ACTIVE, UserQuestRelation.QUESTUSERREL_JOB_FINISHED, UserQuestRelation.QUESTUSERREL_JOBS_NEXT);
                    break;
                case MODE_UNACTIVE:
                    UserQuestRelations.findAllValidInactiveQuests(Users.getCurrentS3User(), new FindCallback<UserQuestRelation>() {
                        @Override
                        public void done(List<UserQuestRelation> userQuestRelationList, ParseException e) {
                            showLoadingProgress(false);
                            if (e != null) {
                                showSnackBar(getString(R.string.quests_loading_open_error));
                                LogUtils.e(TAG, " Error while dling additional unactive quests", e);
                                return;
                            }
                            itemsloaded += userQuestRelationList.size();
                            mViewToLoadInto.setHasAllData(userQuestRelationList.size() < DEFAULT_LIMIT);
                            mViewToLoadInto.addNewDataFromQuestUserRel(userQuestRelationList);
                            mRefreshLayout.setRefreshing(false);
                        }
                    }, getAPIVersion(), DEFAULT_LIMIT, itemsloaded);
                    break;
            }
        }
    }
}
