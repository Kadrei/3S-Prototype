package de.thmgames.s3.Model.ParseModels;

import com.parse.ParseClassName;

import java.util.Locale;

/**
 * Created by Benedikt on 09.02.2015.
 */
@ParseClassName(LocalizedString.PARSE_KEY)
public class LocalizedString extends AbstractParseObject {
    public static final String PARSE_KEY = "LocalizedString";
    private final Locale defaultLocale = Locale.getDefault();

    public String getMessageForLocale(Locale e){
        if(!isDataAvailable()) return "NOT LOADED";
        return getStringWithDefault(e.getISO3Language(), getStringWithDefault("default"));
    }

    public String getMessageForDefaultLocale(){
        if(!isDataAvailable()) return "NOT LOADED";
        return getStringWithDefault(defaultLocale.getISO3Language(), getStringWithDefault("default"));
    }

    public String getDefaultMessage(){
        if(!isDataAvailable()) return "NOT LOADED";
        return getStringWithDefault("default");
    }

    @Override
    protected boolean shouldBeSavedInLocalStore() {
        return true;
    }
}
