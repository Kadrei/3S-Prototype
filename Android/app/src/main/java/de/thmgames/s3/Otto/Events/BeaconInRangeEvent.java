package de.thmgames.s3.Otto.Events;


import org.altbeacon.beacon.Beacon;

import java.util.Collection;

/**
 * Created by Benedikt on 30.10.2014.
 */
public class BeaconInRangeEvent {
    public final Collection<Beacon> beacons;

    public BeaconInRangeEvent(Collection<Beacon> bcns){
        this.beacons=bcns;
    }

    public String toString(){
        String toString = "Count of Beacons:" + (beacons!=null? beacons.size():0) + "\n";
        for (Beacon beacon :beacons) {
            toString+="Beacon UUID:"+beacon.getId1()+" major:"+beacon.getId2()+" minor:"+beacon.getId3()+" in Distance "+beacon.getDistance()+" m\n";
        }
        return toString;
    }
}

