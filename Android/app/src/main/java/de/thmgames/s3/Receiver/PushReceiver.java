package de.thmgames.s3.Receiver;

import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import de.thmgames.s3.Activities.MainActivity;
import de.thmgames.s3.Utils.LogUtils;

/**
 * Created by Benedikt on 16.03.2015.
 */
public class PushReceiver extends ParsePushBroadcastReceiver {

    private static final String TAG = PushReceiver.class.getName();

    @Override
    public void onPushOpen(Context context, Intent intent) {
        ParseAnalytics.trackAppOpenedInBackground(intent);

        String uriString = null;
        try {
            JSONObject pushData = new JSONObject(intent.getStringExtra("com.parse.Data"));
            uriString = pushData.optString("uri");
        } catch (JSONException e) {
            LogUtils.e(TAG, "Unexpected JSONException when receiving push data: ", e);
        }
        Class<? extends Activity> cls = getActivity(context, intent);
        Intent activityIntent;
        if (uriString != null && !uriString.isEmpty()) {
            activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
        } else {
            activityIntent = MainActivity.getIntent(context);
        }
        activityIntent.putExtras(intent.getExtras());
        if (Build.VERSION.SDK_INT >= 16) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(cls);
            stackBuilder.addNextIntent(activityIntent);
            stackBuilder.startActivities();
        } else {
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(activityIntent);
        }
    }
}
