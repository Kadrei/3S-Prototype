package de.thmgames.s3.Model.ParseModels.LocationSystem;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Date;

import de.thmgames.s3.Model.ParseModels.AbstractParseObject;
import de.thmgames.s3.Model.ParseModels.Fraction;

/**
 * Created by Benedikt on 12.11.2014.
 */
@ParseClassName(LocationCaptureData.PARSE_KEY)
public class LocationCaptureData extends AbstractParseObject{
    public final static String PARSE_KEY = "LocationCaptureData";
    public final static String CAPTUREDATA_OWNERFRACTION = Fraction.PARSE_KEY;
    public final static String CAPTUREDATA_CAPTUREDATE = "captureDate";
    public final static String CAPTUREDATA_ENERGY = "energy";

    @Override
    protected boolean shouldBeSavedInLocalStore() {
        return false;
    }

    public boolean hasOwnerFraction(){
        return has(CAPTUREDATA_OWNERFRACTION) && getOwnerFraction()!=null;
    }

    public Fraction getOwnerFraction(){
        return (Fraction) getParseObject(CAPTUREDATA_OWNERFRACTION);
    }

    public Date getCaptureDate(){
        return getDateWithDefault(CAPTUREDATA_CAPTUREDATE);
    }

    public int getEnergy(){
        return getIntWithDefault(CAPTUREDATA_ENERGY);
    }

}
