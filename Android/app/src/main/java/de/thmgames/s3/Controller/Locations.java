package de.thmgames.s3.Controller;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.thmgames.s3.Model.ParseModels.Cloud.Responses.AttackDefendResponse;
import de.thmgames.s3.Model.ParseModels.LocationSystem.Location;
import de.thmgames.s3.Model.ParseModels.LocationSystem.LocationCaptureData;
import de.thmgames.s3.R;

/**
 * Created by Benedikt on 04.11.2014.
 */
public abstract class Locations {
    public static final String[] STANDARD_INCLUDES = { Location.LOCATION_CAPTUREDATA , Location.LOCATION_NAME, Location.LOCATION_LORE, Location.LOCATION_INFO};

    private static final String ATTACKSCRIPTNAME="AttackDefendLocation";
    public static void attackOrDefend(String locale, int energy, String locationId, int apiversion, long timestamp, final AttackDefendResponse.AttackDefenceCallback callback){
        HashMap<String, Object> params = new HashMap<>();
        params.put("locale", locale);
        params.put("spentEnergy", energy);
        params.put(Location.PARSE_KEY.toLowerCase(), locationId);
        params.put("api_version", apiversion);
        params.put("timestamp", timestamp);
        ParseCloud.callFunctionInBackground(ATTACKSCRIPTNAME, params, new FunctionCallback<Map<String, Object>>() {
            @Override
            public void done(Map<String, Object> o, ParseException e) {
                callback.done(new AttackDefendResponse(o,e));
            }
        });
    }


    public static void findLocationForLocationID(String locationId, final GetCallback<Location> callback){
        Location.createWithoutData(Location.class, locationId).fetchWhereAvailable(new GetCallback<Location>() {
            @Override
            public void done(final Location loc, ParseException e) {
                if(e==null){
                    if(loc.isCapturePoint()) {
                        loc.getCaptureData().refresh(new GetCallback<LocationCaptureData>() {
                            @Override
                            public void done(LocationCaptureData lc, ParseException e) {
                                callback.done(loc, e);
                            }
                        });
                    }else {
                        callback.done(loc, e);
                    }
                }else{
                    callback.done(loc, e);
                }
            }
        });
    }

    public static void findAllLocationsWithBeaconsInBackground(final Collection<Beacon> beacons, final FindCallback<Location> callback, String... includes) {
        ParseQuery<Location> query = ParseQuery.getQuery(Location.class);
        ArrayList<String> uuids = new ArrayList<>();
        ArrayList<String> mayors = new ArrayList<>();
        ArrayList<String> minors = new ArrayList<>();
        for (Beacon bcn : beacons) {
            uuids.add(bcn.getId1().toString());
            mayors.add(bcn.getId2().toString());
            minors.add(bcn.getId3().toString());
        }
        query.whereContainedIn(Location.LOCATION_BEACON_UUID, uuids);
        query.whereContainedIn(Location.LOCATION_BEACON_MAYOR, mayors);
        query.whereContainedIn(Location.LOCATION_BEACON_MINOR, minors);
        query.whereEqualTo(Location.LOCATION_VISIBILITY, true);
        for(String include: STANDARD_INCLUDES){
            query.include(include);
        }
        for(String include:includes){
            query.include(include);
        }
        query.findInBackground(new FindCallback<Location>() {
            @Override
            public void done(List<Location> locations, ParseException e) {
                ArrayList<Location> filteredLocations = new ArrayList<Location>();
                if (e != null) {
                    callback.done(locations, e);
                    return;
                }

                for (Location location : locations) {
                    boolean matched = false;
                    for (Beacon bcn : beacons) {
                        if (location.getBeaconUUID().equals(bcn.getId1().toString())
                                && location.getBeaconMayor().equals(bcn.getId2().toString())
                                && location.getBeaconMinor().equals(bcn.getId3().toString())) {
                            matched = true;
                            break;
                        }
                    }
                    if (matched) filteredLocations.add(location);
                }

                callback.done(filteredLocations, e);
            }
        });
    }

    public static void findS3LocationForBeaconInBackground(Beacon beacon, GetCallback<Location> callback, String... includes) {
        getS3LocationForBeaconQuery(beacon.getId1().toString(), beacon.getId2().toString(), beacon.getId3().toString(), includes).getFirstInBackground(callback);
    }

    public static void findS3LocationForBeaconInBackground(String uuid, String mayor, String minor, GetCallback<Location> callback, String... includes) {
        getS3LocationForBeaconQuery(uuid, mayor, minor, includes).getFirstInBackground(callback);
    }

    private static ParseQuery<Location> getS3LocationForBeaconQuery(String uuid, String mayor, String minor, String... includes) {
        ParseQuery<Location> query = ParseQuery.getQuery(Location.class);
        query.whereMatches(Location.LOCATION_BEACON_UUID, uuid);
        query.whereMatches(Location.LOCATION_BEACON_MAYOR, mayor);
        query.whereMatches(Location.LOCATION_BEACON_MINOR, minor);
        query.whereEqualTo(Location.LOCATION_VISIBILITY, true);
        for(String include: STANDARD_INCLUDES){
            query.include(include);
        }
        for(String include:includes){
            query.include(include);
        }
        return query;
    }



    public static void findLocationForLatLngInBackground(double latitude, double longitude, FindCallback<Location> callback, String... includes) {
        getLocationForLatLngQuery(latitude, longitude, includes).findInBackground(callback);
    }

    public static void findLocationForLatLngInBackground(android.location.Location loc, FindCallback<Location> callback, String... includes) {
        getLocationForLatLngQuery(loc.getLatitude(), loc.getLongitude(), includes).findInBackground(callback);
    }

    private static ParseQuery<Location> getLocationForLatLngQuery(double latitude, double longitude, String... includes){
        ParseQuery<Location> query = ParseQuery.getQuery(Location.class);
        query.whereNear(Location.LOCATION_GEOPOINT, new ParseGeoPoint(latitude, longitude));
        query.whereEqualTo(Location.LOCATION_VISIBILITY, true);
        for(String include: STANDARD_INCLUDES){
            query.include(include);
        }
        for(String include:includes){
            query.include(include);
        }
        return query;
    }



}
