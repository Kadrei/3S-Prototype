package de.thmgames.s3.Model.ParseModels.ParameterSystem;

import com.parse.GetCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.thmgames.s3.Controller.Jobs;
import de.thmgames.s3.Controller.WebElements;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.AbstractParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.ParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.SatisfiedCallback;
import de.thmgames.s3.Model.ParseModels.Questsystem.Job;
import de.thmgames.s3.Model.ParseModels.Questsystem.Quest;
import de.thmgames.s3.Model.ParseModels.WebElement;
import de.thmgames.s3.Utils.LogUtils;

/**
 * Created by Benedikt on 08.02.2015.
 */
public class ParametersDataLoader implements SatisfiedCallback {
    private static final String TAG = ParametersDataLoader.class.getName();
    public List<Parameter> parameters;
    public List<ParameterSatisfier> satisfiers = new ArrayList<>();
    private ParameterDataLoadedCallback callback;
    private ParameterLoadingProgressListener listener;
    private WebElement story;
    private Job job;
    private Quest questID;
    private ParameterDataProvider provider;
    private boolean cancelled=false;
    private boolean callbackCalled = false;

    public void cancel(){
        cancelled = true;
    }

    public ParametersDataLoader withCallback(ParameterDataLoadedCallback callback){
        this.callback = callback;
        return this;
    }

    public ParametersDataLoader withDataProvider(ParameterDataProvider provider){
        this.provider=provider;
        return this;
    }

    public ParametersDataLoader withProgressListener(ParameterLoadingProgressListener listener){
        this.listener=listener;
        return this;
    }

    public ParametersDataLoader forStoryElement(WebElement element){
        this.story=element;
        this.parameters = story.getParameters();
        return this;
    }


    private boolean checkAllDataLoaded(){
        for(Parameter param: parameters){
            if(param.isDataAvailable()){
                continue;
            }
            return false;
        }
        return true;
    }
    public ParametersDataLoader forJob(Job job){
        this.job=job;
        this.parameters = job.getParameters();
        return this;
    }

    public void load(){
        if(!checkAllDataLoaded()) {
            if (job != null) {
                Jobs.findJobForIdInBackground(job.getObjectId(), new GetCallback<Job>() {
                    @Override
                    public void done(Job job, ParseException e) {
                        if (e != null) {
                            onDataLoaded(job.getParameters());
                        }
                    }
                }, Job.JOB_PARAMETERS);
            } else if (story != null) {
                WebElements.findStoryForIdInBackground(story.getObjectId(), new GetCallback<WebElement>() {
                    @Override
                    public void done(WebElement webElement, ParseException e) {
                        if (e != null) {
                            onDataLoaded(webElement.getParameters());
                        }
                    }
                }, WebElement.STORYELEMENT_PARAMETER);
            }
        }else{
            onDataLoaded(parameters);
        }

    }

    private void onDataLoaded(List<Parameter> parametersTmp){
        parameters=parametersTmp;
        satisfiers = new ArrayList<>();
        for(Parameter param: parameters){
            if(cancelled) return;
            ParameterSatisfier satisfier = AbstractParameterSatisfier.getParameterSatisfier(param);
            satisfiers.add(satisfier);
            satisfier.setSatisfiedCallback(this);
            satisfier.setDataProvider(provider);
            satisfier.loadData();

        }
        if(parameters==null || parameters.size()==0){
            onGotSatisfied();
        }
    }

    public boolean needsInput(){
        for(ParameterSatisfier satisfier:satisfiers){
            if(satisfier.needsInput()) return true;
        }
        return false;
    }

    @Override
    public void onGotSatisfied() {
        if(callbackCalled) return;
        if(checkAllSatisfied() && !cancelled){
            if(callback!=null){
                if(job!=null){
                    HashMap<String, String> params = getParamsForJob();
                    params.put(Job.PARSE_KEY.toLowerCase(),job.getObjectId());
                    callback.onAllDataLoadedForJob(job, params);
                }
                if(story!=null){
                    callback.onAllDataLoadedForStoryElement(story.getURL(), getPostParamsForStory());
                }
                callbackCalled=true;
            }
        }
    }

    private boolean checkAllSatisfied(){
       int satisfiedParameterCount = 0;
       for(ParameterSatisfier satisfier: satisfiers){
           if(satisfier.isSatisfied()) satisfiedParameterCount++;
           LogUtils.v(TAG, satisfier.getTag()+" - "+satisfier.isSatisfied());
       }
        int max = parameters.size();
        int actual = satisfiedParameterCount;
        if(listener!=null) {
            listener.onProgressChanged(actual, max);
        }
       return actual == max;
    }

    public String getPostParamsForStory() {
        StringBuilder postString = new StringBuilder();
        for(ParameterSatisfier satisfier: satisfiers){
            postString.append(satisfier.getParameter().getParameterName());
            postString.append("=");
            postString.append(satisfier.getValue());
            postString.append("&");

        }
        return postString.toString();
    }

    public HashMap<String, String> getParamsForJob() {
        HashMap<String, String> params = new HashMap<>();
        for(ParameterSatisfier satisfier: satisfiers){
            params.put(satisfier.getParameter().getParameterName(), satisfier.getValue());
        }
        return params;
    }

    public interface ParameterLoadingProgressListener{
        public void onProgressChanged(int actual, int max);
    }

    public interface ParameterDataLoadedCallback {
        public void onAllDataLoadedForStoryElement(String url, String postParams);
        public void onAllDataLoadedForJob(Job job, HashMap<String, String> functionParams);
    }
}
