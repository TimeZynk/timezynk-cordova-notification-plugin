package com.timezynk.cordova.notification;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;

public class GCMInstanceIDService extends InstanceIDListenerService {
    private static final String TAG = "GCMInstanceIDService";

    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, GCMRegistrationService.class);
        startService(intent);
    }
}
