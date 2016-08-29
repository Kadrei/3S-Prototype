package de.thmgames.s3.Model.ParseModels.Questsystem;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import de.thmgames.s3.Model.ParseModels.AbstractParseObject;
import de.thmgames.s3.Model.ParseModels.User;

/**
 * Created by Benedikt on 28.10.2014.
 */
@ParseClassName(UserQuestRelation.PARSE_KEY)
public class UserQuestRelation extends AbstractParseObject{

    public static final String PARSE_KEY="UserQuestRelation";
    public static final String QUESTUSERREL_USER="user";
    public static final String QUESTUSERREL_QUEST="quest";
    public static final String QUESTUSERREL_QUESTID="questID";
    public static final String QUESTUSERREL_CANCELLED="cancelled";
    public static final String QUESTUSERREL_JOB_ACTIVE="activejob";
    public static final String QUESTUSERREL_JOB_FINISHED="finishedjobs";
    public static final String QUESTUSERREL_JOBS_NEXT="nextjobs";

    public boolean hasUser(){
        return has(QUESTUSERREL_USER);
    }

    public UserQuestRelation setUser(User user){
        put(QUESTUSERREL_USER, user);
        return this;
    }

    public String getQuestId(){
        return getStringWithDefault(QUESTUSERREL_QUESTID);
    }

    public User getUser(){
        return (User) getParseUser(QUESTUSERREL_USER);
    }

    public UserQuestRelation setQuest(Quest quest){
        put(QUESTUSERREL_QUEST, quest);
        put(QUESTUSERREL_QUESTID, quest.getObjectId());
        return this;
    }

    public boolean isCancelled(){
        return getBooleanWithDefault(QUESTUSERREL_CANCELLED);
    }

    public UserQuestRelation cancel(boolean cancel){
        put(QUESTUSERREL_CANCELLED, cancel);
        return this;
    }

    public boolean hasQuest(){
        return has(QUESTUSERREL_QUEST);
    }

    public Quest getQuest(){
        return (Quest) getParseObject(QUESTUSERREL_QUEST);
    }

    public boolean hasActiveJob(){
        return has(QUESTUSERREL_JOB_ACTIVE);
    }

    public Job getActiveJob(){
        return (Job) get(QUESTUSERREL_JOB_ACTIVE);
    }

    public UserQuestRelation setActiveJob(Job activeJob){
        put(QUESTUSERREL_JOB_ACTIVE, activeJob);
        return this;
    }

    public UserQuestRelation setFinishedJobs(List<Job> finishedJobs){
        put(QUESTUSERREL_JOB_FINISHED, finishedJobs);
        return this;
    }

    public UserQuestRelation setNextJobs(List<Job> finishedJobs){
        put(QUESTUSERREL_JOBS_NEXT, finishedJobs);
        return this;
    }

    public List<Job> getJobList(){
        List<Job> joblist = new ArrayList<>();
        if(getActiveJob()!=null){
            joblist.add(getActiveJob());
        }
        if(getNextJobs()!=null) {
            for (Job jobs : getNextJobs()) {
                joblist.add(jobs);
            }
        }
        if(getFinishedJobs()!=null){
            for(Job jobs: getFinishedJobs()){
                joblist.add(jobs);
            }
        }

        return joblist;
    }

    public boolean hasFinishedJobs(){
        return getBooleanWithDefault(QUESTUSERREL_JOB_FINISHED);
    }

    public List<Job> getFinishedJobs(){
          return getList(QUESTUSERREL_JOB_FINISHED);
    }

    public boolean hasNextJobs(){
        return getBooleanWithDefault(QUESTUSERREL_JOBS_NEXT);
    }

    public List<Job> getNextJobs(){
      return  getList(QUESTUSERREL_JOBS_NEXT);
    }

    @Override
    protected boolean shouldBeSavedInLocalStore() {
        return false;
    }
}
