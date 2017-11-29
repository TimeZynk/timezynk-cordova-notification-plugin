package com.timezynk.cordova.notification;

import com.google.android.gms.gcm.*;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.app.NotificationCompat;

public class GCMIntentService extends GcmListenerService {
    private static final String TAG = "GCMIntentService";
    private int mId = 1;

    @Override
    public void onMessageReceived(String from, Bundle extras) {
        Log.d(TAG, "onMessage from: " + from);
        Log.v(TAG, "onMessage extras:" + extras.toString());

        GCMPlugin plugin = GCMPlugin.getInstance();
        if (plugin != null) {
            // Application is active and in foreground
            try {
                plugin.onMessage(extras);
            } catch (JSONException e) {
                Log.w(TAG, "Failed to encode notification as JSON", e);
            }
        } else {
            // Application is in background
            createNotification(extras);
        }
    }

    private String getTitle(Bundle params) {
        String title = params.getString("gcm-title");
        if (title != null) {
            return title;
        }
        String s = params.getString("user-name");
        return s;
    }

    private String getContent(Bundle params) {
        String s = params.getString("text");
        return s;
    }

    private void createNotification(Bundle params) {
        Log.v(TAG, "Show message notification: " + params);
        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(this)
                .setSmallIcon(com.timezynk.mobile.R.drawable.notification)
                .setContentTitle(getTitle(params))
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentText(getContent(params));

        // Creates an explicit intent for an Activity in your app
        PackageManager pm = this.getPackageManager();
        String packageName = this.getApplication().getPackageName();
        Intent resultIntent = pm.getLaunchIntentForPackage(packageName);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        for (String key : params.keySet()) {
            String o = params.getString(key);
            resultIntent.putExtra("com.timezynk.mobile." + key, o);
        }
        resultIntent.putExtra("com.timezynk.mobile.launchNotification", true);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(resultIntent.resolveActivity(pm));
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT
        );

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mId++, mBuilder.build());
    }
}
