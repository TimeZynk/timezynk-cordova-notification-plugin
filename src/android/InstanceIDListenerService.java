package com.timezynk.cordova.notification;

import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class InstanceIDListenerService extends FirebaseInstanceIdService {
    private static final String TAG = "InstanceIDListenerService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.i(TAG, "Refreshed token: " + refreshedToken);
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        this.notifyPlugin(refreshedToken);
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
