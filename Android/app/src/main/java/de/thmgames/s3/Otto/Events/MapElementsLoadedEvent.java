package de.thmgames.s3.Otto.Events;

import java.util.List;

import de.thmgames.s3.Model.ParseModels.LocationSystem.MapElement;

/**
 * Created by Benedikt on 12.01.2015.
 */
public class MapElementsLoadedEvent {
    public List<MapElement> mS3MapElements;

    public MapElementsLoadedEvent(List<MapElement> mapElements){
        this.mS3MapElements=mapElements;
    }
}
