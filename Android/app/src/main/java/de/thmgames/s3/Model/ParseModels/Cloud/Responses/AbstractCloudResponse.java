package de.thmgames.s3.Model.ParseModels.Cloud.Responses;

import com.parse.ParseException;

import java.util.Map;

import de.thmgames.s3.Model.ParseModels.AbstractParseObject;

/**
 * Created by Benedikt on 25.02.2015.
 */
public abstract class AbstractCloudResponse {
    private Map<String, Object> response;
    private ParseException error;

    public AbstractCloudResponse( Map<String, Object> response){
        this.response=response;
    }

    public AbstractCloudResponse(ParseException e){
        this.error = e;
    }

    public AbstractCloudResponse(Map<String, Object> response, ParseException e){
        this.response=response;
        this.error = e;
    }

    public void setResponse(Map<String, Object> response){
        this.response=response;
    }

    public boolean hasData(){
        return response!=null;
    }

    public boolean hasError(){
        return error!=null;
    }

    public ParseException getError(){
        return error;
    }

    public boolean has(String key){
        return response!=null && response.containsKey(key) && response.get(key)!=null;
    }

    public String getString(String key){
        return has(key)? (String)response.get(key) : "";
    }

    public <T extends AbstractParseObject> T getParseObject(String key){
        return has(key)? (T) response.get(key) :null;
    }

    public boolean getBoolean(String key){
        return has(key) && (boolean) response.get(key);
    }
}
