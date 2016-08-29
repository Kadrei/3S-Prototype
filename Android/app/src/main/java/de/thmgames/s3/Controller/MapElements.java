package de.thmgames.s3.Controller;

import com.parse.FindCallback;
import com.parse.ParseQuery;

import de.thmgames.s3.Model.ParseModels.LocationSystem.MapElement;
import de.thmgames.s3.Model.ParseModels.LocationSystem.MapElementPart;

/**
 * Created by Benedikt on 14.12.2014.
 */
public abstract class MapElements {
    public static final String[] STANDARD_INCLUDES = {MapElement.MAPELEMENT_PARTS, MapElement.MAPELEMENT_NAME, MapElementPart.MAPELEMENTPART_NAME};
    public static void findAllMapElementsInBackground(FindCallback<MapElement> callback, String... includes){
        ParseQuery<MapElement> query = ParseQuery.getQuery(MapElement.class);
        query.whereEqualTo(MapElement.MAPELEMENT_VISIBILITY, true);
        for(String include: STANDARD_INCLUDES){
            query.include(include);
        }
        for(String include: includes){
            query.include(include);
        }
        query.findInBackground(callback);
    }


}
