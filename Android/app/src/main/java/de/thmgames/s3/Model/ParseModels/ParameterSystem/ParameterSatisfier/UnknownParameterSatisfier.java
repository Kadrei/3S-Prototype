package de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier;

/**
 * Created by Benedikt on 08.02.2015.
 */
public class UnknownParameterSatisfier extends AbstractParameterSatisfier {
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
        return true;
    }

    @Override
    public String getValue() {
        return "null";
    }

    @Override
    public void loadData() {
        notifyCallback();
    }

}
