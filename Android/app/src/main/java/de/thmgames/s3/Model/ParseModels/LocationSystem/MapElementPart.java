package de.thmgames.s3.Model.ParseModels.LocationSystem;

import com.parse.ParseClassName;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.thmgames.s3.Model.ISpinnerElement;
import de.thmgames.s3.Model.ParseModels.AbstractParseObject;
import de.thmgames.s3.Model.ParseModels.LocalizedString;

/**
 * Created by Benedikt on 14.12.2014.
 */
@ParseClassName(MapElementPart.PARSE_KEY)
public class MapElementPart extends AbstractParseObject implements ISpinnerElement {
    public static final String PARSE_KEY = "MapElementPart";
    public static final String MAPELEMENTPART_OVERLAYS=MapOverlay.PARSE_KEY+"s";
    public static final String MAPELEMENTPART_LOCATIONS=Location.PARSE_KEY+"s";
    public static final String MAPELEMENTPART_VISIBILITY = "visibility";
    public static final String MAPELEMENTPART_NAME="name";
    private WeakReference<MapElement> parent;

    public boolean isVisible(){
        return getBooleanWithDefault(MAPELEMENTPART_VISIBILITY);
    }

    public boolean hasOverlays(){
        return has(MAPELEMENTPART_OVERLAYS);
    }

    public List<MapOverlay> getOverlays(){
        if(hasOverlays()){
            return getList(MAPELEMENTPART_OVERLAYS);
        }else{
            return new ArrayList<>();
        }
    }

    public boolean hasLocations() {
        return has(MAPELEMENTPART_LOCATIONS);
    }

    public List<Location> getLocations(){
        if(hasLocations()){
            return getList(MAPELEMENTPART_LOCATIONS);
        }else{
            return new ArrayList<>();
        }
    }

    @Override
    public LocalizedString getTitle() {
        return getName();
    }

    @Override
    public boolean hasParent() {
        return true;
    }

    public MapElement getParent(){
        return parent.get();
    }

    public void setParent(MapElement parent) {
        this.parent = new WeakReference<>(parent);
    }

    public LocalizedString getName(){
        return (LocalizedString) getParseObject(MAPELEMENTPART_NAME);
    }

    @Override
    protected boolean shouldBeSavedInLocalStore() {
        return true;
    }

    @Override
    public String toString(){
        return hasParent() ? getParent().toString() + " - " + getTitle().getMessageForDefaultLocale() : getTitle().getMessageForDefaultLocale();
    }
}
