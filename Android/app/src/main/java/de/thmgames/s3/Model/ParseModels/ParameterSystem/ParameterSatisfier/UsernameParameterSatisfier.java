package de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier;

import de.thmgames.s3.Controller.Users;

/**
 * Created by Benedikt on 08.02.2015.
 */
public class UsernameParameterSatisfier extends AbstractParameterSatisfier {
    public String username;

    @Override
    public boolean needsInput() {
        return false;
    }

    @Override
    public String getTag(){
        return getClass().getName();
    }
    @Override
    public boolean isSatisfied() {
        return username!=null;
    }

    @Override
    public String getValue() {
        return username!=null?username:"";
    }

    @Override
    public void loadData() {
        if(provider!=null){
            provider.loadUsername(this);
            return;
        }
        setValue(Users.getCurrentS3User().getUsername());
    }

    public void setValue(String username){
        this.username=username;
        notifyCallback();
    }
}
