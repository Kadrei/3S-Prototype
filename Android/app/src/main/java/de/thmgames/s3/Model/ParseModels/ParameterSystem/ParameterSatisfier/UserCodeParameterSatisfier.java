package de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier;

/**
 * Created by Benedikt on 08.02.2015.
 */
public class UserCodeParameterSatisfier extends AbstractParameterSatisfier {
    public String usercode;

    @Override
    public boolean needsInput() {
        return true;
    }

    @Override
    public String getTag(){
        return getClass().getName();
    }

    @Override
    public boolean isSatisfied() {
        return usercode!=null;
    }

    @Override
    public String getValue() {
        return usercode!=null?usercode:"";
    }

    @Override
    public void loadData() {
        if(provider!=null){
            provider.loadUserInput(this);
            return;
        }
        this.setData("");
    }

    public void setData(String input){
        this.usercode=input;
        notifyCallback();
    }
}
