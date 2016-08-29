package de.thmgames.s3.Model.ParseModels;

import android.graphics.Color;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.Date;

import de.thmgames.s3.Utils.LogUtils;

/**
 * Created by Benedikt on 28.01.2015.
 */
public abstract class AbstractParseObject extends ParseObject {
    protected String defaultString = "";
    protected int defaultInt = 0;
    protected boolean defaultBoolean = false;
    protected int defaultColor = Color.parseColor("#000000");
    protected double defaultDouble = 0.0;
    protected float defaultFloat = 0.0f;
    protected Date defaultDate = new Date();

    public final static String PARSEOBJECT_OBJECTID = "objectId";
    public final static String PARSEOBJECT_CREATEDAT="createdAt";
    public final static String PARSEOBJECT_UPDATEDAT="updatedAt";

    protected abstract boolean shouldBeSavedInLocalStore();

    public AbstractParseObject setDefaultString(String newDefault){
        this.defaultString=newDefault;
        return this;
    }

    public AbstractParseObject setDefaultDate(Date newDefault){
        this.defaultDate = newDefault;
        return this;
    }

    public AbstractParseObject setDefaultDouble(double newDefault){
        this.defaultDouble=newDefault;
        return this;
    }

    public AbstractParseObject setDefaultBoolean(Boolean newDefault){
        this.defaultBoolean = newDefault;
        return this;
    }

    public AbstractParseObject setDefaultColor(int newDefault){
        this.defaultColor=newDefault;
        return this;
    }

    public int getColorIntWithDefault(String key){
        return getColorIntWithDefault(key, defaultColor);
    }

    public int getColorIntWithDefault(String key, int defaultColor){
        try {
            return (has(key) && getString(key)!=null) ? Color.parseColor("#" + this.getString(key).replace("#", "")) : defaultColor;
        }catch(IllegalArgumentException e){
            return defaultColor;
        }
    }

    public Date getDateWithDefault(String key){
        return getDateWithDefault(key, defaultDate);
    }

    public Date getDateWithDefault(String key, Date defaultDate){
        return (has(key)&&getDate(key)!=null) ? getDate(key) : defaultDate;
    }

    public String getStringWithDefault(String key, String defaultString){
        return (has(key) && getString(key)!=null)?getString(key):defaultString;
    }
    public String getStringWithDefault(String key){
        return getStringWithDefault(key, defaultString);
    }

    public int getIntWithDefault(String key, int defaultInt){
        return has(key)? getInt(key) : defaultInt;
    }

    public int getIntWithDefault(String key){
        return getIntWithDefault( key,  defaultInt);
    }

    public double getDoubleWithDefault(String key){
        return getDoubleWithDefault( key,  defaultDouble);
    }

    public double getDoubleWithDefault(String key, double defaultDouble){
        return has(key)? getDouble(key) : defaultDouble;
    }

    public float getFloatWithDefault(String key){
        return getFloatWithDefault( key,  defaultFloat);
    }

    public float getFloatWithDefault(String key, float defaultFloat){
        return has(key)? (float) getDouble(key) : defaultFloat;
    }

    public boolean getBooleanWithDefault(String key, boolean defaultBoolean){
        return has(key) ? getBoolean(key) : defaultBoolean;
    }

    public boolean getBooleanWithDefault(String key){
        return getBooleanWithDefault(key, defaultBoolean);
    }


    public <T extends AbstractParseObject> void fetchWhereAvailable(final GetCallback<T> callback){
        if(!this.shouldBeSavedInLocalStore() || this.isDataAvailable()){
            this.fetchIfNeededInBackground(callback);
            return;
        }
        this.fetchFromLocalDatastoreInBackground(new GetCallback<T>() {
            @Override
            public void done(T parseObject, ParseException e) {
                if(parseObject==null || e!=null){
                    AbstractParseObject.this.fetchIfNeededInBackground(new GetCallback<T>() {
                        @Override
                        public void done(T parseObject, ParseException e) {
                            if(e!=null)LogUtils.e(AbstractParseObject.class.getName(), "error while loading from network",e);
                            if(parseObject!=null) LogUtils.i(AbstractParseObject.class.getName(),"loaded "+parseObject.getObjectId()+ " from network");
                            callback.done(parseObject, e);
                            if(e==null && parseObject != null && parseObject.shouldBeSavedInLocalStore()) {
                                LogUtils.i(AbstractParseObject.class.getName(),"saved Data for "+parseObject.getObjectId()+ "in database");
                                parseObject.pinInBackground();
                            }
                        }
                    });
                }else{
                    if(parseObject.isDataAvailable()){
                        LogUtils.i(AbstractParseObject.class.getName(),"loaded "+parseObject.getObjectId()+ " from database");
                        callback.done(parseObject, e);
                        return;
                    }
                    parseObject.fetchIfNeededInBackground(new GetCallback<T>() {
                        @Override
                        public void done(T parseObject, ParseException e) {
                            if(e!=null)LogUtils.e(AbstractParseObject.class.getName(), "error while refreshing data from network",e);
                            if(parseObject!=null) LogUtils.i(AbstractParseObject.class.getName(),"loaded Data for "+parseObject.getObjectId()+ "from network");
                            callback.done(parseObject, e);
                            if(e==null && parseObject != null && parseObject.shouldBeSavedInLocalStore()) {
                                LogUtils.i(AbstractParseObject.class.getName(),"saved Data for "+parseObject.getObjectId()+ "in database");
                                parseObject.pinInBackground();
                            }
                        }
                    });

                }
            }
        });
    }

    public <T extends AbstractParseObject> void refresh(final GetCallback<T> callback){
        this.fetchInBackground(new GetCallback<T>() {
            @Override
            public void done(T parseObject, ParseException e) {
                callback.done(parseObject, e);
                if(shouldBeSavedInLocalStore() && e!=null && parseObject != null){
                    LogUtils.i(AbstractParseObject.class.getName(),"saved Data for "+parseObject.getObjectId()+ "in database");
                    parseObject.pinInBackground();
                }
            }
        });

    }
}
