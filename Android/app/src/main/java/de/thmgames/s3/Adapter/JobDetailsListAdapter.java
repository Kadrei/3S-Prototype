package de.thmgames.s3.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;

import de.thmgames.s3.Model.ParseModels.Questsystem.Job;
import de.thmgames.s3.Model.ParseModels.Questsystem.UserQuestRelation;
import de.thmgames.s3.Views.JobDetailsView;

/**
 * Created by Benedikt on 16.12.2014.
 */
public class JobDetailsListAdapter extends BaseAdapter {
    private Context context;
    private List<Job> nextJobdata = new ArrayList<>();
    private List<Job> finishedJobdata = new ArrayList<>();
    private Job activeJob;
    public JobDetailsListAdapter(Context context, UserQuestRelation userQuestRelation) {
        this.context = context;
        this.nextJobdata=userQuestRelation.getNextJobs();
        this.finishedJobdata=userQuestRelation.getFinishedJobs();
        this.activeJob=userQuestRelation.getActiveJob();
    }


    public JobDetailsListAdapter(Context context) {
        this.context = context;
    }

    public void setData(UserQuestRelation userQuestRelation) {
        this.nextJobdata=userQuestRelation.getNextJobs();
        this.finishedJobdata=userQuestRelation.getFinishedJobs();
        this.activeJob=userQuestRelation.getActiveJob();
    }

    private class JobDetailsViewHolder {
        JobDetailsView mJobDetailsView;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public boolean isEmpty(){
        return activeJob==null && (finishedJobdata==null || finishedJobdata.isEmpty()) && (nextJobdata==null || nextJobdata.isEmpty());
    }

    @Override
    public int getCount() {
        int count =0;
        if(activeJob!=null){
            count++;
        }
        if(finishedJobdata!=null){
            count+=finishedJobdata.size();
        }
        if(nextJobdata!=null){
            count+=nextJobdata.size();
        }
        return count;
    }

    @Override
    public Object getItem(int position) {
        if(finishedJobdata!=null){
            if(position<finishedJobdata.size()){
                return finishedJobdata.get(position);
            }else{
                position-=finishedJobdata.size();
            }
        }

        if(activeJob!=null){
            if(position==0)return activeJob;
            position--;
        }

        return nextJobdata.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final JobDetailsViewHolder holder;
        if (convertView == null) {
            JobDetailsView jobView = new JobDetailsView(context);
            holder = new JobDetailsViewHolder();
            holder.mJobDetailsView=jobView;
            jobView.setTag(holder);
        } else {
            holder = (JobDetailsViewHolder) convertView.getTag();
        }
        holder.mJobDetailsView.setJob((Job)getItem(position));
        int count = 0;
        if(finishedJobdata!=null){
            count+=finishedJobdata.size();
            if(count>position){
                holder.mJobDetailsView.setIsFinishedJob();
                return holder.mJobDetailsView;
            }

        }
        if(activeJob!=null){
            count++;
            if(count>position) {
                holder.mJobDetailsView.setIsActiveJob();
                return holder.mJobDetailsView;
            }

        }
        holder.mJobDetailsView.setIsOnNextList();
        return holder.mJobDetailsView;
    }

}
