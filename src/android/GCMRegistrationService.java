package com.timezynk.cordova.notification;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

public class GCMRegistrationService extends IntentService {
    private static final String TAG = "RegIntentService";

    public GCMRegistrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Started GCMRegistrationService intent");
        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                InstanceID instanceID = InstanceID.getInstance(this);
                String senderId = intent.getStringExtra("gcmSenderId");
                String token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                Log.i(TAG, "GCM Registration Token: " + token);
                GCMPlugin instance = GCMPlugin.getInstance();
                if (instance != null) {
                    instance.onRegistrationResponse(token);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to complete token refresh", e);
            GCMPlugin instance = GCMPlugin.getInstance();
            if (instance != null) {
                instance.onRegistrationError(e.getMessage());
            }
        }
    }
}
