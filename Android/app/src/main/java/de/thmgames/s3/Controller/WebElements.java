package de.thmgames.s3.Controller;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import de.thmgames.s3.Model.ParseModels.AbstractParseObject;
import de.thmgames.s3.Model.ParseModels.WebElement;

/**
 * Created by Benedikt on 08.02.2015.
 */
public abstract class WebElements {
    public static final String[] STANDARD_INCLUDES = {WebElement.STORYELEMENT_PARAMETER};

    public static void findStoryForIdInBackground(String storyID, GetCallback<WebElement> callback, String... includes){
        ParseQuery<WebElement> query = ParseQuery.getQuery(WebElement.class);
        for(String include: STANDARD_INCLUDES){
            query.include(include);
        }
        for(String include:includes){
            query.include(include);
        }
        query.getInBackground(storyID, callback);
    }
}
