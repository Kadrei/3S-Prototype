package de.thmgames.s3.Model.ParseModels.ParameterSystem;

import com.parse.ParseClassName;

import de.thmgames.s3.Model.ParseModels.AbstractParseObject;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.ApiVersionParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.LocaleParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.NearLocationParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.ParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.UnknownParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.UserCodeParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.UserEnergyParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.UserFractionParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.UsernameParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.UserPointsParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.LocalizedString;

/**
 * Created by Benedikt on 08.02.2015.
 */
@ParseClassName(Parameter.PARSE_KEY)
public class Parameter extends AbstractParseObject {
    public static final String PARSE_KEY = "Parameter";
    public static final String PARAMETER_NAME="name";
    public static final String PARAMETER_TYPE="type";
    public static final String PARAMETER_OPTIONAL="optional";
    public static final String PARAMETER_USERMESSAGE = LocalizedString.PARSE_KEY;

    public enum TYPE
    {
        USERFRACTION, USERNAME, ENERGY, POINTS, LOCALE,
        NEAR_LOCATIONS, USERCODE, APIVERSION,
        INVALID;

        public int getInt()
        {
            switch(this){
                case USERFRACTION:
                    return 0;
                case USERNAME:
                    return 1;
                case ENERGY:
                    return 2;
                case POINTS:
                    return 3;
                case LOCALE:
                    return 4;
                case NEAR_LOCATIONS:
                    return 1000;
                case USERCODE:
                    return 1001;
                case APIVERSION:
                    return 1002;
                case INVALID:
                default:
                    return -1;
            }
        }

        public String toString()
        {
            switch(this){
                case USERFRACTION:
                    return "Fraktion";
                case USERNAME:
                    return "Usernamen";
                case ENERGY:
                    return "Energie";
                case POINTS:
                    return "Punkte";
                case LOCALE:
                    return "Sprache";
                case NEAR_LOCATIONS:
                    return "Naheliegende Stationen";
                case USERCODE:
                    return "Usereingabe";
                case APIVERSION:
                    return "Apiversion";
                case INVALID:
                default:
                    return "INVALID";
            }
        }
    }

    public String getParameterName(){
        return getStringWithDefault(PARAMETER_NAME);
    }

    private int getParameterTypeInt(){
        return getIntWithDefault(PARAMETER_TYPE,-1);
    }

    public TYPE getParameterType(){
        int type = getParameterTypeInt();
        if(type==TYPE.USERFRACTION.getInt()){
            return TYPE.USERFRACTION;
        }
        if(type==TYPE.USERNAME.getInt()){
            return TYPE.USERNAME;
        }
        if(type==TYPE.ENERGY.getInt()){
            return TYPE.ENERGY;
        }
        if(type==TYPE.POINTS.getInt()){
            return TYPE.POINTS;
        }
        if(type==TYPE.LOCALE.getInt()){
            return TYPE.LOCALE;
        }
        if(type==TYPE.NEAR_LOCATIONS.getInt()){
            return TYPE.NEAR_LOCATIONS;
        }
        if(type==TYPE.USERCODE.getInt()){
            return TYPE.USERCODE;
        }
        if(type==TYPE.APIVERSION.getInt()){
            return TYPE.APIVERSION;
        }
        return TYPE.INVALID;
    }

    public boolean isOptional(){
        return getBooleanWithDefault(PARAMETER_OPTIONAL);
    }


    public boolean hasUserMessage(){
        return has(PARAMETER_USERMESSAGE);
    }

    public LocalizedString getParameterUsermessage(){
        return (LocalizedString) getParseObject(PARAMETER_USERMESSAGE);
    }

    @Override
    protected boolean shouldBeSavedInLocalStore() {
        return true;
    }
}
