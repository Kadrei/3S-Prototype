package de.thmgames.s3;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import java.util.Locale;

import de.thmgames.s3.Controller.Installations;
import de.thmgames.s3.Model.ParseModels.ActionSystem.Action;
import de.thmgames.s3.Model.ParseModels.Fraction;
import de.thmgames.s3.Model.ParseModels.LocationSystem.Location;
import de.thmgames.s3.Model.ParseModels.LocationSystem.LocationCaptureData;
import de.thmgames.s3.Model.ParseModels.LocationSystem.MapElement;
import de.thmgames.s3.Model.ParseModels.LocationSystem.MapElementPart;
import de.thmgames.s3.Model.ParseModels.LocationSystem.MapOverlay;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.Parameter;
import de.thmgames.s3.Model.ParseModels.LocalizedString;
import de.thmgames.s3.Model.ParseModels.Questsystem.Job;
import de.thmgames.s3.Model.ParseModels.Questsystem.Quest;
import de.thmgames.s3.Model.ParseModels.Questsystem.UserQuestRelation;
import de.thmgames.s3.Model.ParseModels.WebElement;
import de.thmgames.s3.Model.ParseModels.User;
import de.thmgames.s3.Utils.LogUtils;

/**
 * Created by Benedikt on 20.10.2014.
 */
public class App extends Application {
    public void onCreate() {
        super.onCreate();

        if (!BuildConfig.DEBUG) Crashlytics.start(this); //Starte Crashlytics

        ParseObject.registerSubclass(MapElement.class);
        ParseObject.registerSubclass(MapElementPart.class);
        ParseObject.registerSubclass(MapOverlay.class);
        ParseObject.registerSubclass(Location.class);
        ParseObject.registerSubclass(LocationCaptureData.class);

        ParseObject.registerSubclass(Fraction.class);
        ParseObject.registerSubclass(User.class);

        ParseObject.registerSubclass(UserQuestRelation.class);
        ParseObject.registerSubclass(Quest.class);
        ParseObject.registerSubclass(Job.class);
        ParseObject.registerSubclass(Parameter.class);
        ParseObject.registerSubclass(LocalizedString.class);
        ParseObject.registerSubclass(WebElement.class);
        ParseObject.registerSubclass(Action.class);
        Parse.enableLocalDatastore(this.getApplicationContext());
        Parse.initialize(this, "", "");
        Installations.setLanguage(Locale.getDefault());
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(mPrefs.getBoolean(getString(R.string.key_push_sp),false)){
            try {
                Installations.setPushForBroadcastChannelEnabled(true, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            LogUtils.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                        } else {
                            LogUtils.e("com.parse.push", "failed to subscribe for push", e);
                        }
                    }
                });
            }catch (IllegalArgumentException e){
                LogUtils.e("com.parse.push", "failed to subscribe for push", e);
            }
        }


    }

}
