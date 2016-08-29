package de.thmgames.s3.Model.ParseModels.LocationSystem;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.parse.ParseClassName;

import java.util.ArrayList;
import java.util.List;

import de.thmgames.s3.Model.ISpinnerElement;
import de.thmgames.s3.Model.ParseModels.AbstractParseObject;
import de.thmgames.s3.Model.ParseModels.LocalizedString;

/**
 * Created by Benedikt on 14.12.2014.
 */
@ParseClassName(MapElement.PARSE_KEY)
public class MapElement extends AbstractParseObject implements ISpinnerElement {
    public static final String PARSE_KEY = "MapElement";
    public static final String MAPELEMENT_NAME="name";
    public static final String MAPELEMENT_NORTHEAST_LAT="northeast_lat";
    public static final String MAPELEMENT_NORTHEAST_LONG="northeast_long";
    public static final String MAPELEMENT_SOUTHWEST_LAT="southwest_lat";
    public static final String MAPELEMENT_SOUTHWEST_LONG="southwest_long";
    public static final String MAPELEMENT_PARTS=MapElementPart.PARSE_KEY+"s";
    public static final String MAPELEMENT_VISIBILITY = "visibility";

    public boolean isVisible(){
        return getBooleanWithDefault(MAPELEMENT_VISIBILITY);
    }

    public LocalizedString getName(){
        return (LocalizedString) getParseObject(MAPELEMENT_NAME);
    }

    public LatLng getNortEast(){
      return  new LatLng(getDouble(MAPELEMENT_NORTHEAST_LAT),getDouble(MAPELEMENT_NORTHEAST_LONG));
    }

    public LatLng getSouthWest(){
        return  new LatLng(getDouble(MAPELEMENT_SOUTHWEST_LAT),getDouble(MAPELEMENT_SOUTHWEST_LONG));
    }

    public boolean hasBounds(){
        return has(MAPELEMENT_NORTHEAST_LAT) && has(MAPELEMENT_NORTHEAST_LONG) && has(MAPELEMENT_SOUTHWEST_LONG) && has(MAPELEMENT_SOUTHWEST_LONG);
    }

    public LatLngBounds getBounds(){
        return LatLngBounds.builder()
                .include(getNortEast())
                .include(getSouthWest())
                .include(new LatLng(getDouble(MAPELEMENT_SOUTHWEST_LAT), getDouble(MAPELEMENT_NORTHEAST_LONG)))
                .include(new LatLng(getDouble(MAPELEMENT_NORTHEAST_LAT), getDouble(MAPELEMENT_SOUTHWEST_LONG)))
                .build();
    }

    public LatLngBounds.Builder getBoundsBuilder(){
        return LatLngBounds.builder()
                .include(getNortEast())
                .include(getSouthWest())
                .include(new LatLng(getDouble(MAPELEMENT_SOUTHWEST_LAT), getDouble(MAPELEMENT_NORTHEAST_LONG)))
                .include(new LatLng(getDouble(MAPELEMENT_NORTHEAST_LAT), getDouble(MAPELEMENT_SOUTHWEST_LONG)));
    }

    public boolean hasParts(){
        return has(MAPELEMENT_PARTS) && getList(MAPELEMENT_PARTS) !=null;
    }
    public List<MapElementPart> getParts(){
        if(hasParts()){
            return getList(MAPELEMENT_PARTS);
        }else{
            return new ArrayList<>();
        }

    }

    @Override
    protected boolean shouldBeSavedInLocalStore() {
        return true;
    }

    @Override
    public LocalizedString getTitle() {
        return getName();
    }

    @Override
    public boolean hasParent() {
        return false;
    }

    @Override
    public MapElement getParent() {
        return null;
    }

    @Override
    public String toString(){
        return hasParent() ? getParent().getTitle().getMessageForDefaultLocale() + " - " + getTitle().getMessageForDefaultLocale() : getTitle().getMessageForDefaultLocale();
    }
}
