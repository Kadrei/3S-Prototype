package de.thmgames.s3.Controller;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import de.thmgames.s3.Model.ParseModels.AbstractParseObject;
import de.thmgames.s3.Model.ParseModels.Cloud.Responses.JobFinishedResponse;
import de.thmgames.s3.Model.ParseModels.Questsystem.Job;
import de.thmgames.s3.Model.ParseModels.Questsystem.Quest;
import de.thmgames.s3.Model.ParseModels.Questsystem.UserQuestRelation;
import de.thmgames.s3.Model.ParseModels.User;

/**
 * Created by Benedikt on 24.10.2014.
 */
public abstract class UserQuestRelations {
    public static final String[] STANDARD_INCLUDES = {UserQuestRelation.QUESTUSERREL_JOB_ACTIVE, UserQuestRelation.QUESTUSERREL_JOB_FINISHED, UserQuestRelation.QUESTUSERREL_JOBS_NEXT, UserQuestRelation.QUESTUSERREL_QUEST};

    public static UserQuestRelation wrapQuestInUserQuestRelation(Quest quest) {
        UserQuestRelation questUserRelation = new UserQuestRelation().setQuest(quest).setUser(Users.getCurrentS3User());
        if (quest.hasJobs()) {
            int i = 0;
            ArrayList<Job> nextJobs = new ArrayList<>();
            for (Job job : quest.getJobs()) {
                if (i == 0) {
                    questUserRelation.setActiveJob(job);
                } else {
                    nextJobs.add(job);
                }
                i++;
            }
            questUserRelation.setNextJobs(nextJobs);
        }
        return questUserRelation;
    }

    public static ArrayList<UserQuestRelation> transformQuestListToUserQuestRelationList(Collection<Quest> quests) {
        if (quests == null) return new ArrayList<>();
        ArrayList<UserQuestRelation> s3QuestsUserRels = new ArrayList<>();
        for (Quest quest : quests) {
            UserQuestRelation questRelTmp = UserQuestRelations.wrapQuestInUserQuestRelation(quest);
            s3QuestsUserRels.add(questRelTmp);
        }
        return s3QuestsUserRels;
    }

    public static void findUserQuestRelForQuestIDInBackground(final String questId, final GetCallback<UserQuestRelation> callback, final String... includes) {
        final ParseQuery<UserQuestRelation> questForUser = ParseQuery.getQuery(UserQuestRelation.class)
                .whereEqualTo(UserQuestRelation.QUESTUSERREL_USER, Users.getCurrentS3User())
                .whereMatchesQuery(UserQuestRelation.QUESTUSERREL_QUEST, Quests.getQuestForIdQuery(questId, includes));
        questForUser.getFirstInBackground(new GetCallback<UserQuestRelation>() {
            @Override
            public void done(UserQuestRelation userQuestRelations, ParseException e) {
                if (e == null && userQuestRelations != null) {
                    callback.done(userQuestRelations, e);
                    return;
                }
                Quests.findQuestForIdInBackground(questId, new GetCallback<Quest>() {
                    @Override
                    public void done(Quest quests, ParseException e) {
                        callback.done(UserQuestRelations.wrapQuestInUserQuestRelation(quests), e);
                    }
                }, includes);
            }
        });
    }

    public static void findAllValidInactiveQuests(User user, final FindCallback<UserQuestRelation> callback, final int curAPILevel, final int limit, final int from, final String... includes) {
        Quests.findAllValidInactiveQuests(user, new FindCallback<Quest>() {
            @Override
            public void done(List<Quest> quests, ParseException e) {
                if (quests == null || e != null) {
                    callback.done(null, e);
                } else {
                    callback.done(UserQuestRelations.transformQuestListToUserQuestRelationList(quests), e);
                }
            }
        }, curAPILevel, limit, from, includes);
    }

    public static ParseQuery<UserQuestRelation> getAllQuestUserRelationsForUserQuery(User user) {
        return ParseQuery.getQuery(UserQuestRelation.class).whereEqualTo(UserQuestRelation.QUESTUSERREL_USER, user);
    }


    public static void getActiveQuestUserRelationsForUser(User user, FindCallback<UserQuestRelation> callback, int limit, int from, String... includes) {
        ParseQuery<UserQuestRelation> questForUser = ParseQuery.getQuery(UserQuestRelation.class)
                .whereEqualTo(UserQuestRelation.QUESTUSERREL_USER, user)
                .whereExists(UserQuestRelation.QUESTUSERREL_JOB_ACTIVE)
                .orderByDescending(AbstractParseObject.PARSEOBJECT_UPDATEDAT);
        questForUser.setLimit(limit);
        questForUser.setSkip(from);
        for (String include : includes) {
            questForUser.include(include);
        }
        questForUser.findInBackground(callback);
    }

    public static ParseQuery<UserQuestRelation> getAllActiveUserQuestRelationsForUserQuery(User user) {
        return ParseQuery.getQuery(UserQuestRelation.class).whereEqualTo(UserQuestRelation.QUESTUSERREL_USER, user)
                .whereExists(UserQuestRelation.QUESTUSERREL_JOB_FINISHED);
    }

    public static void getHistoryQuestUserRelationsForUser(User user, FindCallback<UserQuestRelation> callback, int limit, int from, String... includes) {
        ParseQuery<UserQuestRelation> questForUser = getHistoryQuestUserRelationsForUserQuery(user);
        for (String include : includes) {
            questForUser.include(include);
        }
        questForUser.setLimit(limit);
        questForUser.setSkip(from);
        questForUser.findInBackground(callback);
    }


    public static ParseQuery<UserQuestRelation> getHistoryQuestUserRelationsForUserQuery(User user) {
        return ParseQuery.getQuery(UserQuestRelation.class)
                .whereEqualTo(UserQuestRelation.QUESTUSERREL_USER, user)
                .whereDoesNotExist(UserQuestRelation.QUESTUSERREL_JOBS_NEXT)
                .whereDoesNotExist(UserQuestRelation.QUESTUSERREL_JOB_ACTIVE)
                .whereExists(UserQuestRelation.QUESTUSERREL_JOB_FINISHED)
                .orderByDescending(AbstractParseObject.PARSEOBJECT_UPDATEDAT);
    }

    public static void finishActiveJob(UserQuestRelation rel, final HashMap<String, String> functionParams, final JobFinishedResponse.JobFinishedCallback callback) {
        Jobs.finishJob(rel.getQuestId(), rel.getActiveJob(), functionParams, callback);
    }

}