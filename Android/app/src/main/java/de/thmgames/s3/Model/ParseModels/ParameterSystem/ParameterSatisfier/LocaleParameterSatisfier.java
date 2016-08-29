package de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier;

import java.util.Locale;
/**
 * Created by Benedikt on 08.02.2015.
 */
public class LocaleParameterSatisfier extends AbstractParameterSatisfier {
    private Locale mLocale;

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
        return mLocale!=null;
    }

    @Override
    public String getValue() {
        return mLocale!=null? mLocale.getISO3Language():"";
    }

    @Override
    public void loadData() {
        if(provider!=null){
            provider.loadLocale(this);
            return;
        }
        setData(Locale.getDefault());
    }

    public void setData(Locale aDefault) {
        mLocale=aDefault;
        notifyCallback();
    }
}
