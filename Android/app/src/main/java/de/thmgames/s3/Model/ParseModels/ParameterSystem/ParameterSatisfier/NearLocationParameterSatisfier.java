package de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier;

import java.util.ArrayList;
import java.util.List;

import de.thmgames.s3.Model.ParseModels.LocationSystem.Location;

/**
 * Created by Benedikt on 08.02.2015.
 */
public class NearLocationParameterSatisfier extends AbstractParameterSatisfier {
    private List<Location> locations;

    @Override
    public boolean needsInput() {
        return false;
    }

    @Override
    public String getTag(){
        return getClass().getName();
    }

    @Override
    public boolean isSatisfied() {
        return locations !=null;
    }

    @Override
    public String getValue() {
        if(locations==null) return "";
        StringBuilder builder = new StringBuilder();
        for(Location loc: locations){
            builder.append(loc.getObjectId());
            builder.append(";");
        }
        return builder.toString();
    }

    @Override
    public void loadData() {
        if(provider!=null){
            provider.loadNextLocations(this);
            return;
        }
        this.setData(new ArrayList<Location>());
    }

    public void setData(List<Location> loc){
        locations=loc;
        notifyCallback();
    }
}
