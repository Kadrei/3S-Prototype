package de.thmgames.s3.Utils;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.inaka.galgo.Galgo;
import com.inaka.galgo.GalgoOptions;
import com.parse.ParseException;

import de.thmgames.s3.BuildConfig;

/**
 * Created by Benedikt on 29.10.2014.
 */
public final class LogUtils {

    private static boolean galgoEnabled;

    public static void enableGalgo(Context ctx){
        return;
//        if(!BuildConfig.DEBUG) return;
//        GalgoOptions options = new GalgoOptions.Builder()
//                .numberOfLines(9)
//                .backgroundColor(Color.parseColor("#D9d6d6d6"))
//                .textColor(Color.BLACK)
//                .textSize(8)
//                .build();
//        Galgo.enable(ctx, options);
//        galgoEnabled= true;
    }

    public static void disableGalgo(Context ctx){
        return;
//        if(!BuildConfig.DEBUG) return;
//        Galgo.disable(ctx);
//        galgoEnabled= false;
    }

    public static void e(String tag, String message, Throwable e) {
        if(e==null){
            LogUtils.e(tag, message);
            return;
        }
        if (!BuildConfig.DEBUG) {
            Crashlytics.log(Log.ERROR, tag, message);
            Crashlytics.logException(e);
        } else {
            if(galgoEnabled){
                Galgo.log("E//"+tag+"// "+message+"//"+e.getMessage());
            }
            Log.e(tag, message, e);
        }

    }

    public static void e(String tag, String message, ParseException e) {
        if(e==null){
            LogUtils.e(tag, message);
            return;
        }
        if (!BuildConfig.DEBUG) {
            Crashlytics.log(Log.ERROR, tag, message +"\n Code: "+ e.getCode());
            Crashlytics.logException(e);
        } else {
            if(galgoEnabled){
                Galgo.log("E//"+tag+"// "+message+"//"+e.getMessage()+"//"+e.getCode());
            }
            Log.e(tag, message +"\n Code: "+e.getCode(), e);
        }

    }

    public static void e(String tag, String message) {

        if (!BuildConfig.DEBUG) {
            Crashlytics.log(Log.ERROR, tag, message);
        } else {
            if(galgoEnabled){
                Galgo.log("E//"+tag+"// "+message);
            }
            Log.e(tag, message);
        }
    }

    public static void v(String tag, String message) {

        if (!BuildConfig.DEBUG) {
            Crashlytics.log(Log.VERBOSE, tag, message);
        } else {
            if(galgoEnabled){
                Galgo.log("V//"+tag+"// "+message);
            }
            Log.v(tag, message);
        }
    }

    public static void d(String tag, String message) {
        if (!BuildConfig.DEBUG) {
            Crashlytics.log(Log.DEBUG, tag, message);
        } else {
            if(galgoEnabled){
                Galgo.log("D//"+tag+"// "+message);
            }
            Log.d(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (!BuildConfig.DEBUG) {
            Crashlytics.log(Log.INFO, tag, message);
        } else
        {
            if(galgoEnabled){
                Galgo.log("I//"+tag+"// "+message);
            }
            Log.i(tag, message);
        }
    }

    public static void w(String tag, String message) {
        if (!BuildConfig.DEBUG) {
            Crashlytics.log(Log.WARN, tag, message);
        } else {
            if(galgoEnabled){
                Galgo.log("W//"+tag+"// "+message);
            }
            Log.w(tag, message);
        }
    }
}
