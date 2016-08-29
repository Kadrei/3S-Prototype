package de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier;

/**
 * Created by Benedikt on 08.02.2015.
 */
public class UserPointsParameterSatisfier extends AbstractParameterSatisfier {
    public String points;

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
        return points!=null;
    }

    @Override
    public String getValue() {
        return points!=null?points:"";
    }

    @Override
    public void loadData() {
        if(provider!=null){
            provider.loadUserPoints(this);
            return;
        }
        setData(0);

    }

    public void setData(int i) {
        points=String.valueOf(i);
        notifyCallback();
    }
}
