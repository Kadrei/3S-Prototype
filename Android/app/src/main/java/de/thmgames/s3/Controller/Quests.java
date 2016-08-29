package de.thmgames.s3.Controller;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseQuery;

import java.util.Date;

import de.thmgames.s3.Model.ParseModels.Fraction;
import de.thmgames.s3.Model.ParseModels.Questsystem.Quest;
import de.thmgames.s3.Model.ParseModels.Questsystem.UserQuestRelation;
import de.thmgames.s3.Model.ParseModels.WebElement;
import de.thmgames.s3.Model.ParseModels.User;

/**
 * Created by Benedikt on 22.02.2015.
 */
public abstract class Quests {
    public static final String[] STANDARD_INCLUDES = {Quest.QUEST_SUBJOBS, Quest.QUEST_SHORT_DESCRIPTION, Quest.QUEST_NAME,Quest.QUEST_DESCRIPTION,Quest.QUEST_LORE};

    public static void findQuestForIdInBackground(String questId, final GetCallback<Quest> callback, String... includes) {
        getQuestForIdQuery(questId, includes).getFirstInBackground(callback);
    }

    public static ParseQuery<Quest> getQuestForIdQuery(String questId, String... includes) {
        ParseQuery<Quest> questQuery = ParseQuery.getQuery(Quest.class);
        questQuery.whereEqualTo("objectId", questId);
        ParseQuery<WebElement> query = ParseQuery.getQuery(WebElement.class);
        for (String include : STANDARD_INCLUDES) {
            query.include(include);
        }
        for (String include : includes) {
            query.include(include);
        }
        return questQuery;
    }

    public static void findAllValidInactiveQuests(User user, final FindCallback<Quest> callback, final int curAPILevel, final int limit, final int from, final String... includes) {
        Date now = new Date();
        final ParseQuery<Quest> inactiveQuests = ParseQuery.getQuery(Quest.class)
                .whereDoesNotMatchKeyInQuery(Quest.PARSEOBJECT_OBJECTID, UserQuestRelation.QUESTUSERREL_QUESTID, UserQuestRelations.getAllQuestUserRelationsForUserQuery(user))
                .orderByAscending(Quest.QUEST_END)
                .whereGreaterThan(Quest.QUEST_END, now)
                .whereEqualTo(Quest.QUEST_VISIBILITY, true)
                .whereLessThanOrEqualTo(Quest.QUEST_START, now);
        inactiveQuests.whereLessThanOrEqualTo(Quest.QUEST_MINAPI, curAPILevel);
        inactiveQuests.whereEqualTo(Quest.QUEST_VISIBILITY, true);
        inactiveQuests.setLimit(limit);
        inactiveQuests.setSkip(from);
        for(String include: STANDARD_INCLUDES){
            inactiveQuests.include(include);
        }
        for (String include : includes) {
            inactiveQuests.include(include);
        }
        inactiveQuests.findInBackground(callback);
    }
}
