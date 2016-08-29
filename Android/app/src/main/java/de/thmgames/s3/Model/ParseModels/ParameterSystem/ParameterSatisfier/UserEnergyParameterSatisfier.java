package de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier;

/**
 * Created by Benedikt on 08.02.2015.
 */
public class UserEnergyParameterSatisfier extends AbstractParameterSatisfier {
    public String energy;

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
        return energy!=null;
    }

    @Override
    public String getValue() {
        return energy!=null?energy:"";
    }

    @Override
    public void loadData() {
        if(provider!=null){
            provider.loadUserEnergy(this);
            return;
        }
        setData(0);
    }

    public void setData(int i) {
        energy = String.valueOf(i);
        notifyCallback();
    }
}
