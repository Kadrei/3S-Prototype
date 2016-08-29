package de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier;

import de.thmgames.s3.Model.ParseModels.LocalizedString;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.Parameter;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterDataProvider;

/**
 * Created by Benedikt on 08.02.2015.
 */
abstract public class AbstractParameterSatisfier implements ParameterSatisfier {
    public SatisfiedCallback callback = null;
    public ParameterDataProvider provider=null;
    public LocalizedString localizedString;
    private Parameter param;

    public static ParameterSatisfier getParameterSatisfier(Parameter param){
        ParameterSatisfier satisfier;

        switch(param.getParameterType()) {
            case USERFRACTION:
                satisfier= new UserFractionParameterSatisfier();
                break;
            case USERNAME:
                satisfier= new UsernameParameterSatisfier();
                break;
            case ENERGY:
                satisfier= new UserEnergyParameterSatisfier();
                break;
            case POINTS:
                satisfier= new UserPointsParameterSatisfier();
                break;
            case NEAR_LOCATIONS:
                satisfier= new NearLocationParameterSatisfier();
                break;
            case USERCODE:
                satisfier= new UserCodeParameterSatisfier();
                break;
            case LOCALE:
                satisfier = new LocaleParameterSatisfier();
                break;
            case APIVERSION:
                satisfier = new ApiVersionParameterSatisfier();
                break;
            case INVALID:
            default:
                satisfier = new UnknownParameterSatisfier();
        }
        if(param.hasUserMessage()) satisfier.setLocalizedMessage(param.getParameterUsermessage());
        satisfier.setParameter(param);
        return satisfier;
    }

    public void setParameter(Parameter param){
        this.param = param;
    }

    public Parameter getParameter(){
        return param;
    }

    @Override
    public String getTag(){
        return getClass().getName();
    }

    public void setLocalizedMessage(LocalizedString message){
        this.localizedString = message;
    }

    public LocalizedString getLocalizedMessage(){
        return localizedString;
    }

    public boolean hasLocalizedMessage(){
        return localizedString !=null;
    }

    public void setSatisfiedCallback(SatisfiedCallback callback){
        this.callback = callback;
    }

    @Override
    public void setDataProvider(ParameterDataProvider provider) {
        this.provider=provider;
    }

    public void notifyCallback(){
        if(callback!=null && this.isSatisfied()) callback.onGotSatisfied();
    }
}
