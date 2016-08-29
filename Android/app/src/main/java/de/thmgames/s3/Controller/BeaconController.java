package de.thmgames.s3.Controller;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;

import com.squareup.otto.Produce;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import de.thmgames.s3.Otto.Events.BeaconInRangeEvent;
import de.thmgames.s3.Otto.OttoEventBusProvider;
import de.thmgames.s3.Utils.LogUtils;

/**
 * Created by Benedikt on 30.10.2014.
 */
public class BeaconController implements BeaconConsumer, MonitorNotifier, RangeNotifier {

    private final static String TAG = BeaconController.class.getName();
    private Context mContext;
    private Vector<String> uuids = new Vector<>();
    private Vector<Region> regions = new Vector<>();
    private Vector<String> beaconLayouts = new Vector<>();
    private boolean started = false;

    public static BeaconController INSTANCE;

    public static BeaconController getInstance(Context ctx) {
        if (INSTANCE == null) INSTANCE = new BeaconController(ctx);
        return INSTANCE;
    }

    private BeaconController(Context context) {
        mContext = context;
        INSTANCE = this;
    }

    public void addUUID(String uuid){
        LogUtils.i(TAG, "adding uuid:"+uuid);
        uuids.add(uuid);
    }

    public void addUUIDs(List<String> uuids){
        for(String uuid: uuids){
            addUUID(uuid);
        }
    }

    public void notifyUUIDsChanged(){
        if(started){ //restart
            LogUtils.i(TAG, "restarting BeaconController");
            this.stopSearching();
        }
        this.startSearchingIfNeeded();
    }

    public void setContext(Context ctx) {
        this.mContext = ctx;
    }


    public void addBeaconLayout(String beaconLayout){
        LogUtils.i(TAG, "adding beaconlayout:"+beaconLayout);
        beaconLayouts.add(beaconLayout);
    }

    public void addBeaconLayouts(List<String> beaconLayouts){
        for(String beaconLayout: beaconLayouts){
            addBeaconLayout(beaconLayout);
        }
    }

    public void notifyBeaconControllerIsSetup(){
        if(started){ //restart
            LogUtils.i(TAG, "restarting BeaconController");
            this.stopSearching();
        }
        this.startSearchingIfNeeded();
    }

    public void notifyBeaconLayoutChanged(){
        if(started){ //restart
            LogUtils.i(TAG, "restarting BeaconController");
            this.stopSearching();
        }
        this.startSearchingIfNeeded();
    }
    public void startSearchingIfNeeded() {
        if (started) return;
        LogUtils.i(TAG, "Beaconcontroller started");
        if (beaconManager == null) {
            beaconManager = BeaconManager.getInstanceForApplication(mContext);
        }
        for(String beaconLayout: beaconLayouts){
            beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(beaconLayout));
        }
        beaconLayouts.removeAllElements();
        beaconManager.bind(this);
    }

    public void stopSearching() {
        if (beaconManager != null) beaconManager.unbind(this);
        started = false;
    }

    private BeaconManager beaconManager;

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setMonitorNotifier(this);
        beaconManager.setRangeNotifier(this);

        regions = new Vector<>();
        try {
            for(String uuid: uuids){
                Region r = new Region(uuid, Identifier.parse(uuid), null, null);
                regions.add(r);
                beaconManager.startMonitoringBeaconsInRegion(r);
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "Error while starting Monitoring of Beacons in Region", e);
        }
        started = true;
    }

    @Override
    public Context getApplicationContext() {
        return mContext;
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        OttoEventBusProvider.getInstanceForUIThread().unregister(this);
        try {
            if(started){
                for(Region region: regions){
                    beaconManager.stopMonitoringBeaconsInRegion(region);
                }
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "Error while finishing Monitoring of Beacons in Region", e);
        }finally {
            regions = new Vector<>();
            mContext.unbindService(serviceConnection);
            started = false;
            LogUtils.i(TAG, "Unbinding Service");
            OttoEventBusProvider.getInstance().post(new BeaconInRangeEvent(new ArrayList<Beacon>()));
        }
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        OttoEventBusProvider.getInstanceForUIThread().register(this);
        return mContext.bindService(intent, serviceConnection, i);
    }

    private int enteredRegionsCount=0;
    @Override
    public void didEnterRegion(Region region) {
        try {
            if(!regions.contains(region)){
                LogUtils.i(TAG,"in region, but wrong region");
                return;
            }
            enteredRegionsCount++;
            LogUtils.i(TAG, "Starting Ranging of Beacons in Region");
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            LogUtils.e(TAG, "Error while starting Ranging of Beacons in Region", e);
        }
    }

    @Override
    public void didExitRegion(Region region) {
        try {
            enteredRegionsCount--;
            mLastFoundBeacons = new ArrayList<Collection<Beacon>>(MAXEVENTS+1);
            LogUtils.i(TAG, "Stopping Ranging of Beacons in Region");
            beaconManager.stopRangingBeaconsInRegion(region);
            LogUtils.i(TAG, "Exiting Region");
            if(enteredRegionsCount==0) {
                OttoEventBusProvider.getInstance().post(new BeaconInRangeEvent(new ArrayList<Beacon>()));
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "Error while stoping Ranging of Beacons in Region", e);
        }
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {

    }

    private final static int MAXEVENTS = 5;
    private ArrayList<Collection<Beacon>> mLastFoundBeacons;
    private Collection<Beacon> calcBestBeaconRanging(Collection<Beacon> beacons, Region region) {
        if (mLastFoundBeacons == null) {
            mLastFoundBeacons = new ArrayList<>(MAXEVENTS+1);
            mLastFoundBeacons.add(beacons);
            return beacons;
        }

        Collection<Beacon> bestList = beacons;
        for (Collection<Beacon> bHistory : mLastFoundBeacons) {
            bestList = (bHistory != null && bHistory.size() > bestList.size() ? bHistory : bestList);
        }
        mLastFoundBeacons.add(0, beacons);

        if(mLastFoundBeacons.size()>MAXEVENTS){
            mLastFoundBeacons.remove(mLastFoundBeacons.size()-1);
        }
        return bestList;
    }

    @Produce
    public BeaconInRangeEvent produceBeaconInRangeEvent() {
        if(this.mLastFoundBeacons!=null && this.mLastFoundBeacons.size()>0){
            return new BeaconInRangeEvent(this.mLastFoundBeacons.get(0));
        }
        return new BeaconInRangeEvent(new ArrayList<Beacon>());
    }


    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        if(region !=null && !regions.contains(region)){
            LogUtils.w(TAG,"in region, but wrong region");
            return;
        }
        if(beacons.size()==0 && enteredRegionsCount>1) return;
        OttoEventBusProvider.getInstance().post(new BeaconInRangeEvent(calcBestBeaconRanging(beacons, region)));
    }
}
