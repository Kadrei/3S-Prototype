package de.thmgames.s3.Model.ParseModels.LocationSystem;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import de.thmgames.s3.Model.ParseModels.AbstractParseObject;

/**
 * Created by Benedikt on 22.01.2015.
 */
@ParseClassName(MapOverlay.PARSE_KEY)
public class MapOverlay extends AbstractParseObject {
    public static final String PARSE_KEY = "MapOverlay";
    public static final String MAPOVERLAY_VISIBILITY = "visibility";
    public static final String MAPOVERLAY_IMAGE = "image";
    public static final String MAPOVERLAY_CENTER="center";
    public static final String MAPELEMENT_HEIGHT="height";
    public static final String MAPELEMENT_WIDTH="width";
    public static final String MAPELEMENT_BEARING="bearing";

    public boolean isVisible(){
        return has(MAPOVERLAY_VISIBILITY) && getBoolean(MAPOVERLAY_VISIBILITY);
    }

    public boolean hasImage(){
        return has(MAPOVERLAY_IMAGE);
    }

    public ParseFile getImage(){
        return getParseFile(MAPOVERLAY_IMAGE);
    }

    private boolean hasCenter(){
        return has(MAPOVERLAY_CENTER);
    }

    private ParseGeoPoint getCenter(){
        return getParseGeoPoint(MAPOVERLAY_CENTER);
    }

    public LatLng getCenterLatLng(){
        if(hasCenter()){
            ParseGeoPoint center = getCenter();
            return new LatLng(center.getLatitude(), center.getLongitude());
        }
        return new LatLng(0.0, 0.0);
    }

    public float getWidth(){
        return getFloatWithDefault(MAPELEMENT_WIDTH);
    }

    public float getHeight(){
        return getFloatWithDefault(MAPELEMENT_HEIGHT);
    }

    public boolean hasBearing(){
        return has(MAPELEMENT_BEARING);
    }

    public float getBearing(){
        return getFloatWithDefault(MAPELEMENT_BEARING);
    }

    @Override
    protected boolean shouldBeSavedInLocalStore() {
        return true;
    }

}
