package de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier;

import de.thmgames.s3.Utils.LogUtils;

/**
 * Created by Benedikt on 08.02.2015.
 */
public class ApiVersionParameterSatisfier extends AbstractParameterSatisfier {
    private String apiVersion;

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
        LogUtils.e("ParameterSatisfier", "apiversion satisfied"+(apiVersion!=null));
        return apiVersion!=null;
    }

    @Override
    public String getValue() {
        return apiVersion!= null ? apiVersion : "";
    }

    @Override
    public void loadData() {
        if(provider!=null){
            provider.loadApiVersion(this);
            return;
        }
        this.setData(1);
    }

    public void setData(int s) {
        LogUtils.e("ParameterSatisfier", "apiversion satisfied");
        apiVersion=String.valueOf(s);
        notifyCallback();
    }
}
