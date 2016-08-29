package de.thmgames.s3.Model.ParseModels.LocationSystem;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import org.altbeacon.beacon.Beacon;

import de.thmgames.s3.Model.ParseModels.AbstractParseObject;
import de.thmgames.s3.Model.ParseModels.LocalizedString;
import de.thmgames.s3.Model.ParseModels.WebElement;

/**
 * Created by Benedikt on 22.10.2014.
 */
@ParseClassName(Location.PARSE_KEY)
public class Location extends AbstractParseObject {
    public final static String PARSE_KEY = "Location";
    public final static String LOCATION_NAME = "name";
    public final static String LOCATION_GEOPOINT = "geopoint";
    public final static String LOCATION_BEACON_UUID = "beaconuuid";
    public final static String LOCATION_BEACON_MAYOR = "beaconmayor";
    public final static String LOCATION_BEACON_MINOR = "beaconminor";
    public final static String LOCATION_ADDRESS = "address";
    public final static String LOCATION_LORE = "lore";
    public final static String LOCATION_INFO = "information";
    public final static String LOCATION_CAPTUREDATA = LocationCaptureData.PARSE_KEY;
    public final static String LOCATION_IMAGE_HEADER = "header_image";
    public final static String LOCATION_IMAGE_MINI = "mini_image";
    public final static String LOCATION_VISIBILITY = "visibility";

   public boolean isVisible(){
       return getBooleanWithDefault(LOCATION_VISIBILITY);
   }

    public boolean isCapturePoint() {
        return has(LOCATION_CAPTUREDATA) && getCaptureData()!=null;
    }

    public LocationCaptureData getCaptureData(){
        return (LocationCaptureData) getParseObject(LOCATION_CAPTUREDATA);
    }

    public LocalizedString getName() {
        return (LocalizedString) getParseObject(LOCATION_NAME);
    }

    public boolean hasHeaderImage(){
        return has(LOCATION_IMAGE_HEADER) && getHeaderImage()!=null;
    }

    public ParseFile getHeaderImage() {
        return getParseFile(LOCATION_IMAGE_HEADER);
    }

    public boolean hasMiniImage(){
        return has(LOCATION_IMAGE_MINI) && getMiniImage()!=null;
    }

    public ParseFile getMiniImage(){
        return getParseFile(LOCATION_IMAGE_MINI);
    }

    public Boolean hasBeacon() {
        return has(LOCATION_BEACON_UUID) && has(LOCATION_BEACON_MAYOR) && has(LOCATION_BEACON_MINOR);
    }

    public String getBeaconUUID() {
        return getStringWithDefault(LOCATION_BEACON_UUID);
    }

    public String getBeaconMayor() {
        return getStringWithDefault(LOCATION_BEACON_MAYOR);
    }

    public String getBeaconMinor() {
        return getStringWithDefault(LOCATION_BEACON_MINOR);
    }

    public LocalizedString getAddress() {
        return (LocalizedString) getParseObject(LOCATION_ADDRESS);
    }

    public WebElement getLore() {
        return (WebElement) getParseObject(LOCATION_LORE);
    }

    public WebElement getInfo() {
        return (WebElement) getParseObject(LOCATION_INFO);
    }

    public boolean hasLocation(){
        return has(LOCATION_GEOPOINT) && getLocation()!=null;
    }

    public double getLatitude() {
        return getParseGeoPoint(LOCATION_GEOPOINT).getLatitude();
    }

    public double getLongitude() {
        return getParseGeoPoint(LOCATION_GEOPOINT).getLongitude();
    }

    public ParseGeoPoint getGeoPoint() {
        return getParseGeoPoint(LOCATION_GEOPOINT);
    }

    public LatLng getLatLng(){
        return new LatLng(getGeoPoint().getLatitude(), getGeoPoint().getLongitude());
    }

    private android.location.Location loc;

    public android.location.Location getLocation() {
        if (loc == null) {
            loc = new android.location.Location(getName().getMessageForDefaultLocale());
            loc.setLatitude(getLatitude());
            loc.setLongitude(getLongitude());
        }
        return loc;
    }

    public boolean isLocationForBeacon(Beacon bcn){
        return this.hasBeacon() && bcn!=null && bcn.getId1().toString().equals(this.getBeaconUUID()) && bcn.getId2().toString().equals(this.getBeaconMayor()) && bcn.getId3().toString().equals(this.getBeaconMinor());
    }

    @Override
    protected boolean shouldBeSavedInLocalStore() {
        return true;
    }
}

