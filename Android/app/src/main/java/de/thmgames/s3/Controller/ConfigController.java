package de.thmgames.s3.Controller;

import com.parse.ConfigCallback;
import com.parse.ParseConfig;

/**
 * Created by Benedikt on 06.11.2014.
 */
public abstract class ConfigController {

    public static void getConfigInBackground(ConfigCallback callback){
        ParseConfig.getInBackground(callback);
    }

}
