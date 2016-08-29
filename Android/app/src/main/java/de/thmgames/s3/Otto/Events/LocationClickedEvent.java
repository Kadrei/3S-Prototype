package de.thmgames.s3.Otto.Events;

import de.thmgames.s3.Model.ParseModels.LocationSystem.Location;

/**
 * Created by Benedikt on 21.12.2014.
 */
public class LocationClickedEvent {
    public Location location;

    public LocationClickedEvent(Location clickedLocation){
        this.location=clickedLocation;
    }

    public String toString(){
        return "S3LocationClickedEvent for Location:"+location.getName().getMessageForDefaultLocale();
    }
}
