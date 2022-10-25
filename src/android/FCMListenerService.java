package com.timezynk.cordova.notification;

import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;
import org.json.JSONException;

public class FCMListenerService extends FirebaseMessagingService {
    private static final String TAG = "FCMListenerService";
    private int mId = 1;

    @Override
    public void onMessageReceived(RemoteMessage message) {
        String from = message.getFrom();
        Map extras = message.getData();
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

    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.i(TAG, "Refreshed token: " + token);
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        this.notifyPlugin(token);
    }

    protected void notifyPlugin(String token) {
        // In the (unlikely) event that multiple refresh operations occur simultaneously,
        // ensure that they are processed sequentially.
        synchronized (TAG) {
            GCMPlugin instance = GCMPlugin.getInstance();
            if (instance != null) {
                instance.onRegistrationResponse(token);
            }
        }
    }
}
