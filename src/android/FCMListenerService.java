package com.timezynk.cordova.notification;

import android.util.Log;
import org.json.JSONException;
import com.google.firebase.messaging.FirebaseMessagingService;

public class FCMListenerService extends FirebaseMessagingService {
    private static final String TAG = "FCMListenerService";
    private int mId = 1;

    @Override
    public void onMessageReceived(RemoteMessage message) {
        String from = message.getFrom();
        Map extras = message.getData();
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
        }
    }
}
