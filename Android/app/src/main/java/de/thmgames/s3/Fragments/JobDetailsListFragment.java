package de.thmgames.s3.Fragments;

import android.app.ListFragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;

import de.thmgames.s3.Adapter.JobDetailsListAdapter;
import de.thmgames.s3.Controller.UserQuestRelations;
import de.thmgames.s3.Model.ParseModels.Questsystem.UserQuestRelation;
import de.thmgames.s3.R;
import de.thmgames.s3.Utils.LayoutUtils;

/**
 * Created by Benedikt on 27.02.2015.
 */
public class JobDetailsListFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String QUEST_ID = "QUEST_ID";

    public static JobDetailsListFragment newInstance(String questId) {
        JobDetailsListFragment f = new JobDetailsListFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(QUEST_ID, questId);
        f.setArguments(args);

        return f;
    }

    private String questID;
    private TextView mEmptyView;
    private SwipeRefreshLayout mJobListRefreshView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        questID=getArguments().getString(QUEST_ID);
    }

    private JobDetailsListAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout mWrapper = (RelativeLayout) inflater.inflate(R.layout.refreshing_listview, container, false);
        mJobListRefreshView = (SwipeRefreshLayout) mWrapper.findViewById(R.id.swipe_container);
        final ListView mJobList = (ListView) mWrapper.findViewById(android.R.id.list);
        mJobList.setDivider(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !LayoutUtils.isInLandscape(this.getActivity())) {
            mJobList.setPadding(0, 0, 0, LayoutUtils.getNavigationBarHeight(this.getActivity()));
            mJobList.setClipToPadding(false);
        }
        mEmptyView = (TextView) mWrapper.findViewById(R.id.emptyTextView);
        mEmptyView.setText(getString(R.string.loading));
        mJobList.setEmptyView(mEmptyView);
        mJobListRefreshView.setOnRefreshListener(this);
        return mWrapper;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new JobDetailsListAdapter(getActivity());
        setListAdapter(adapter);
        loadData();
    }


    public void loadData(){
        mJobListRefreshView.setRefreshing(true);
        UserQuestRelations.findUserQuestRelForQuestIDInBackground(questID,new GetCallback<UserQuestRelation>() {
            @Override
            public void done(UserQuestRelation userQuestRelation, ParseException e) {
                adapter.setData(userQuestRelation);
                adapter.notifyDataSetChanged();
                if(userQuestRelation.getJobList().size()==0){
                    mEmptyView.setText(getString(R.string.no_jobs_in_quest));
                }
                mJobListRefreshView.setRefreshing(false);
            }
        }, UserQuestRelation.QUESTUSERREL_QUEST, UserQuestRelation.QUESTUSERREL_JOB_ACTIVE, UserQuestRelation.QUESTUSERREL_JOB_FINISHED, UserQuestRelation.QUESTUSERREL_JOBS_NEXT);
    }

    @Override
    public void onRefresh() {
        loadData();
    }
}
