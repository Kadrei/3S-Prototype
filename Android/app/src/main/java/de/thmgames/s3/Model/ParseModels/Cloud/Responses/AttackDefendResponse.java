package de.thmgames.s3.Model.ParseModels.Cloud.Responses;

import com.parse.ParseException;

import java.util.Map;

import de.thmgames.s3.Model.ParseModels.ActionSystem.Action;

/**
 * Created by Benedikt on 25.02.2015.
 */
public class AttackDefendResponse extends AbstractCloudResponse {

    public static final String ACTION_KEY = "action";
    public static final String SUCCESS_KEY = "success";

    public AttackDefendResponse(Map<String, Object> response) {
        super(response);
    }

    public AttackDefendResponse(ParseException e){
        super(e);
    }

    public AttackDefendResponse(Map<String, Object> response, ParseException e){
        super(response,e);
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

    public interface AttackDefenceCallback{
        public void done(AttackDefendResponse response);
    }
}

