package de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier;

import java.util.Date;

/**
 * Created by Benedikt on 08.02.2015.
 */
public class TimestampParameterSatisfier extends AbstractParameterSatisfier {
    private Date timestamp;

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
        return timestamp!=null;
    }

    @Override
    public String getValue() {
        return timestamp!=null? timestamp.toString():"";
    }

    @Override
    public void loadData() {
        if(provider!=null){
            provider.loadTimestamp(this);
            return;
        }
        setValue(new Date());
    }

    public void setValue(Date date){
        this.timestamp = date;
        notifyCallback();
    }
}
