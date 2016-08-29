package de.thmgames.s3.Model.ParseModels.Cloud.Responses;

import com.parse.ParseException;

import java.util.Map;

import de.thmgames.s3.Model.ParseModels.ActionSystem.Action;

/**
 * Created by Benedikt on 25.02.2015.
 */
public class JobFinishedResponse extends AbstractCloudResponse {
    public static final String ACTION_KEY = "action";
    public static final String SUCCESS_KEY = "success";

    public JobFinishedResponse(Map<String, Object> response) {
        super(response);
    }

    public JobFinishedResponse(ParseException e) {
        super(e);
    }

    public JobFinishedResponse(Map<String, Object> response, ParseException e) {
        super(response, e);
    }

    public boolean hasAction(){
        return has(ACTION_KEY);
    }

    public Action getAction(){
        return getParseObject(ACTION_KEY);
    }

    public boolean wasSuccess(){
        return getBoolean(SUCCESS_KEY);
    }

    public interface JobFinishedCallback{
        public void done(JobFinishedResponse response);
    }
}
