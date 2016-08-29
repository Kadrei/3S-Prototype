package de.thmgames.s3.Fragments;


import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import de.thmgames.s3.BuildConfig;
import de.thmgames.s3.Controller.MapElements;
import de.thmgames.s3.Model.ParseModels.LocalizedString;
import de.thmgames.s3.Model.ParseModels.LocationSystem.Location;
import de.thmgames.s3.Model.ParseModels.LocationSystem.MapElement;
import de.thmgames.s3.Model.ParseModels.LocationSystem.MapElementPart;
import de.thmgames.s3.Model.ParseModels.LocationSystem.MapOverlay;
import de.thmgames.s3.Otto.Events.LoadEvent;
import de.thmgames.s3.Otto.Events.LocationClickedEvent;
import de.thmgames.s3.Otto.Events.MapElementSelectedEvent;
import de.thmgames.s3.Otto.Events.MapElementsLoadedEvent;
import de.thmgames.s3.Otto.Events.ShowSnackBarEvent;
import de.thmgames.s3.Otto.OttoEventBusProvider;
import de.thmgames.s3.R;
import de.thmgames.s3.Utils.LogUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends com.google.android.gms.maps.MapFragment implements GoogleMap.OnMarkerClickListener {

    public final static String TAG = MapFragment.class.getName();
    private boolean isSetUp = false;
    private List<MapElement> mMapElements;
    protected Picasso mPicasso;
    private GoogleMap mMap;
    private LatLngBounds.Builder mMaxLocationBoundsBuilder;
    private List<Location> mLocations;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPicasso = Picasso.with(activity);
        mPicasso.setIndicatorsEnabled(BuildConfig.DEBUG);
        mPicasso.setLoggingEnabled(BuildConfig.DEBUG);

    }

    @Override
    public void onStart() {
        OttoEventBusProvider.getInstance().register(this);
        OttoEventBusProvider.getInstanceForUIThread().register(this);
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Unregister from the eventbus.
     */
    @Override
    public void onStop() {
        super.onStop();
        mPicasso.cancelTag(TAG);
        targets.removeAllElements();
        OttoEventBusProvider.getInstance().unregister(this);
        OttoEventBusProvider.getInstanceForUIThread().unregister(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mMap = this.getMap();
        if (!isSetUp && mMap != null) {
            mMap.setOnMarkerClickListener(this);
            setUpIfNeeded();
        }
        loadMapElements();

        return v;
    }

    public void setUpIfNeeded() {
        if(!this.isAdded()) return;
        mMap.setMyLocationEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        isSetUp = true;

    }

    private void loadMapElements() {
        showTileOverlay();
        MapElements.findAllMapElementsInBackground(new FindCallback<MapElement>() {
            @Override
            public void done(List<MapElement> loadedElements, ParseException e) {
                if(!MapFragment.this.isAdded()) return;
                if (e != null) {
                    handleParseError(e, getString(R.string.error_loading_mapinformation));
                    return;
                }
                mMapElements = loadedElements;
                OttoEventBusProvider.getInstanceForUIThread().post(new MapElementsLoadedEvent(mMapElements));
            }
        }, MapElement.MAPELEMENT_PARTS, MapElementPart.MAPELEMENTPART_LOCATIONS, MapElementPart.MAPELEMENTPART_OVERLAYS);
    }

    @Subscribe
    public void onLoadEvent(LoadEvent e) {
        //TODO: Progressbar
    }

    public void showSnackBar(String text) {
        if(!this.isAdded()) return;
        OttoEventBusProvider.getInstanceForUIThread().post(new ShowSnackBarEvent(text));
    }

    private MapElement mLastMapElement;

    @Subscribe
    public void onMapElementPartSelected(final MapElementSelectedEvent e) {
        if(!this.isAdded()) return;
        LogUtils.v(TAG, "MapElementSelectedEvent //" + e.toString());
        if (mMapElements == null || mMapElements.size() < e.mSelectedParentElementIndex) return;
        MapElement mMapElement = mMapElements.get(e.mSelectedParentElementIndex);
        if (mMapElement == null || mMapElement.getParts().size() < e.mSelectedPartElementIndex)
            return;
        mMap.clear();

        MapElementPart mMapElementPart = mMapElement.getParts().get(e.mSelectedPartElementIndex);
        mMaxLocationBoundsBuilder = mMapElement.getBoundsBuilder();
        if (!mMapElement.equals(mLastMapElement) || !mMap.getProjection().getVisibleRegion().latLngBounds.contains(mMap.getCameraPosition().target) || mMap.getCameraPosition().zoom<10) {
            focusOn(mMaxLocationBoundsBuilder);
        }

        mLastMapElement = mMapElement;
        showTileOverlay();
        if (mMapElementPart.hasOverlays()) {
            loadOverlaysAndShow(mMapElementPart.getOverlays());
        }

        if (mMapElementPart.hasLocations()) {
            mLocations = mMapElementPart.getLocations();
            loadLocationsAndShow(mLocations);
        }
    }

    private final static int MAX_WIDTH = 1000;
    private final static int MAX_HEIGHT = 1000;
    private final static int PADDING = 100;

    public void focusOn(LatLngBounds.Builder builder) {
        if(!this.isAdded()) return;
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), MAX_WIDTH, MAX_HEIGHT, PADDING));
    }

    private void loadOverlaysAndShow(List<MapOverlay> overlays) {
        if(!this.isAdded()) return;
        targets.removeAllElements();
        mPicasso.cancelTag(TAG);
        for (MapOverlay overlay : overlays) {
            overlay.fetchIfNeededInBackground(new GetCallback<MapOverlay>() {
                @Override
                public void done(MapOverlay mapOverlay, ParseException e) {
                    if (e == null) {
                        if (mapOverlay.isVisible()) {
                            showOverlay(mapOverlay);
                        }
                    } else {
                        handleParseError(e, getString(R.string.error_loading_overlays));
                    }
                }
            });
        }
    }

    private Vector<Target> targets = new Vector<>(); //Hold Strong Reference to avoid GC

    private void showOverlay(MapOverlay mapOverlay) {
        if(!this.isAdded()) return;
        if (mapOverlay.hasImage()) {
            Target target = new MapOverlayTarget(mapOverlay);
            mPicasso.load(mapOverlay.getImage().getUrl()).tag(TAG).noPlaceholder().noFade().into(target);
            targets.add(target);
        }
    }

    private void showTileOverlay() {
        if(!this.isAdded()) return;
        TileProvider tileProvider = new UrlTileProvider(256, 256) {
            @Override
            public URL getTileUrl(int x, int y, int zoom) {
                if (!checkTileExists(x, y, zoom) || ParseConfig.getCurrentConfig() == null || ParseConfig.getCurrentConfig().getParseFile("TileImage") == null)
                    return null;
                try {
                    return new URL(ParseConfig.getCurrentConfig().getParseFile("TileImage").getUrl());
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
            }

            /*
             * Check that the tile server supports the requested x, y and zoom.
             * Complete this stub according to the tile range you support.
             * If you support a limited range of tiles at different zoom levels, then you
             * need to define the supported x, y range at each zoom level.
             */
            private boolean checkTileExists(int x, int y, int zoom) {
                return true;
            }
        };

        TileOverlay tileOverlay = mMap.addTileOverlay(new TileOverlayOptions()
                .tileProvider(tileProvider).zIndex(-10).fadeIn(false));
//        tileOverlay.clearTileCache();
    }


    private void loadLocationsAndShow(List<Location> locations) {
        if(!this.isAdded()) return;
        for (Location loc : locations) {
            loc.fetchIfNeededInBackground(new GetCallback<Location>() {
                @Override
                public void done(Location loc, ParseException e) {
                    if(MapFragment.this.isDetached()) return;
                    if (e == null) {
                        if (loc.isVisible()) showMarkerForLocation(loc);
                    } else {
                        handleParseError(e);
                    }
                }
            });
        }
    }

    private void showMarkerForLocation(final Location loc) {
        if(!this.isAdded()) return;
        loc.getName().fetchWhereAvailable(new GetCallback<LocalizedString>() {
            @Override
            public void done(LocalizedString string, ParseException e) {
                if(!MapFragment.this.isAdded()) return;
                MarkerOptions mMarkerOptions = new MarkerOptions()
                        .position(loc.getLatLng())
                        .draggable(false);
                if(string!=null){
                    mMarkerOptions= mMarkerOptions.title(string.getMessageForDefaultLocale());
                }
                mMap.addMarker(mMarkerOptions);
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(!this.isAdded()) return false;
        CameraUpdate markerLocation = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), mMap.getCameraPosition().zoom);
        mMap.animateCamera(markerLocation);
        for (Location loc : mLocations) {
            if (marker.getPosition().equals(loc.getLatLng())) {
                OttoEventBusProvider.getInstanceForUIThread().post(new LocationClickedEvent(loc));
            }
        }

        return false;
    }

    private void handleParseError(ParseException e) {
        handleParseError(e, "ERROR");
    }

    private void handleParseError(ParseException e, String message) {
        showSnackBar(message);
        LogUtils.e(TAG, message, e);
    }

    private class MapOverlayTarget implements Target {
        private MapOverlay overlay;

        public MapOverlayTarget(MapOverlay overlay) {
            this.overlay = overlay;
        }

        public MapOverlay getOverlay() {
            return overlay;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof MapOverlayTarget && overlay.equals(((MapOverlayTarget) o).getOverlay());
        }

        @Override
        public int hashCode() {
            return mMap.hashCode();
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            if(!MapFragment.this.isAdded()) return;
            GroundOverlayOptions mMapElementPartOverlay = new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromBitmap(bitmap))
                    .bearing(overlay.getBearing())
                    .position(overlay.getCenterLatLng(), overlay.getWidth(), overlay.getHeight());
            mMap.addGroundOverlay(mMapElementPartOverlay);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            LogUtils.i(TAG, "Error while loading Overlay");
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            LogUtils.i(TAG, "placeholder Overlay");
        }
    }

}
