package de.thmgames.s3.Model.ParseModels;

import com.parse.ParseClassName;

import java.util.ArrayList;
import java.util.List;

import de.thmgames.s3.Model.ParseModels.ParameterSystem.Parameter;

/**
 * Created by Benedikt on 08.02.2015.
 */
@ParseClassName(WebElement.PARSE_KEY)
public class WebElement extends AbstractParseObject{
    public final static String PARSE_KEY="WebElement";
    public final static String STORYELEMENT_PARAMETER= Parameter.PARSE_KEY+"s";
    public final static String STORYELEMENT_URL="url";

    public String getURL(){
        return getStringWithDefault(STORYELEMENT_URL);
    }

    public boolean hasScriptname(){
        return has(STORYELEMENT_PARAMETER);
    }

    public boolean hasParameters(){
        return has(STORYELEMENT_PARAMETER);
    }

    public List<Parameter> getParameters(){
        if(hasParameters()) {
            return getList(STORYELEMENT_PARAMETER);
        }else{
            return new ArrayList<>();
        }
    }

    @Override
    protected boolean shouldBeSavedInLocalStore() {
        return true;
    }

}
