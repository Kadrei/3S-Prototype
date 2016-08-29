package de.thmgames.s3.Controller;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.HashMap;
import java.util.Map;

import de.thmgames.s3.Model.ParseModels.Cloud.Responses.JobFinishedResponse;
import de.thmgames.s3.Model.ParseModels.Questsystem.Job;
import de.thmgames.s3.Model.ParseModels.Questsystem.Quest;
import de.thmgames.s3.Utils.LogUtils;

/**
 * Created by Benedikt on 05.12.2014.
 */
public abstract class Jobs {
    public static final String[] STANDARD_INCLUDES = { Job.JOB_PARAMETERS, Job.JOB_SHORT_DESCRIPTION, Job.JOB_NAME};

    public static void findJobForIdInBackground(String jobID, final GetCallback<Job> callback, String... includes) {
        ParseQuery<Job> query = ParseQuery.getQuery(Job.class);
        query.whereEqualTo("objectId", jobID);
        for(String include: STANDARD_INCLUDES){
            query.include(include);
        }
        for(String include: includes){
            query.include(include);
        }
        query.getFirstInBackground(callback);
    }

    public static void finishJob(String questID, final Job job, final HashMap<String, String> functionParams, final JobFinishedResponse.JobFinishedCallback callback){
        functionParams.put(Quest.PARSE_KEY.toLowerCase(),questID);
        ParseCloud.callFunctionInBackground(job.getScriptname(), functionParams, new FunctionCallback<Map<String, Object>>() {
            @Override
            public void done(Map<String, Object> o, ParseException e) {
                callback.done(new JobFinishedResponse(o,e));
            }
        });
    }
}
