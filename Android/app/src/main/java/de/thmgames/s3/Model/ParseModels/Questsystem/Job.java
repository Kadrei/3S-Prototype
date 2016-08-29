package de.thmgames.s3.Model.ParseModels.Questsystem;

import com.parse.ParseClassName;
import com.parse.ParseFile;

import java.util.ArrayList;
import java.util.List;

import de.thmgames.s3.Model.ParseModels.AbstractParseObject;
import de.thmgames.s3.Model.ParseModels.LocalizedString;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.Parameter;

/**
 * Created by Benedikt on 28.10.2014.
 */
@ParseClassName(Job.PARSE_KEY)
public class Job extends AbstractParseObject {

    public final static String PARSE_KEY = "Job";
    public final static String JOB_NAME = "name";
    public final static String JOB_IMG = "image";
    public final static String JOB_DESCRIPTION = "description";
    public final static String JOB_SHORT_DESCRIPTION = "shortDescription";
    public final static String JOB_POINTGAIN = "pointGain";
    public final static String JOB_ENERGYGAIN = "energyGain";
    public final static String JOB_PARAMETERS= Parameter.PARSE_KEY+"s";
    public final static String JOB_FUNCTIONNAME ="functionname";

    public LocalizedString getName() {
        return (LocalizedString) getParseObject(JOB_NAME);
    }

    public LocalizedString getDescription() {
        return (LocalizedString) getParseObject(JOB_DESCRIPTION);
    }

    public LocalizedString getShortDescription() {return (LocalizedString) getParseObject(JOB_SHORT_DESCRIPTION);}

    public boolean hasImage() {
        return has(JOB_IMG);
    }

    public ParseFile getImage() {
        return getParseFile(JOB_IMG);
    }

    public int getPointGain() {
        return getIntWithDefault(JOB_POINTGAIN);
    }

    public int getEnergyGain() {
        return getIntWithDefault(JOB_ENERGYGAIN);
    }

    public String getScriptname(){
        return getStringWithDefault(JOB_FUNCTIONNAME);
    }

    public boolean hasScriptname(){
        return has(JOB_PARAMETERS);
    }

    public boolean hasParameters(){
        return has(JOB_PARAMETERS);
    }

    public List<Parameter> getParameters(){
        if(hasParameters()) {
            return getList(JOB_PARAMETERS);
        }else{
            return new ArrayList<>();
        }
    }

    @Override
    protected boolean shouldBeSavedInLocalStore() {
        return true;
    }
}
