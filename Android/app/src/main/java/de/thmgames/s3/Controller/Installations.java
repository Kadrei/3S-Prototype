package de.thmgames.s3.Controller;

import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import java.util.Locale;

/**
 * Created by Benedikt on 09.03.2015.
 */
public abstract class Installations {

    public static final String INSTALLATION_KEY_LANGUAGE="lang";

    public static void setLanguage(Locale loc){
        ParseInstallation curInstallation = ParseInstallation.getCurrentInstallation();
        curInstallation.put(INSTALLATION_KEY_LANGUAGE,loc.getISO3Language());
    }

    private static void enablePushForBroadcastChannel(SaveCallback callback){
        ParsePush.subscribeInBackground("", callback);

    }

    private static void disablePushForBroadcastChannel(SaveCallback callback){
        ParsePush.unsubscribeInBackground("",callback);
    }

    public static void setPushForBroadcastChannelEnabled(boolean enabled, SaveCallback callback) throws IllegalArgumentException{
        if(enabled){
            enablePushForBroadcastChannel(callback);
        }else{
            disablePushForBroadcastChannel(callback);
        }
    }
}
