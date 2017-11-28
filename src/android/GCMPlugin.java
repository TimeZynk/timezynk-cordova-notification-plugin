package com.timezynk.cordova.notification;

import org.apache.cordova.*;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class GCMPlugin extends CordovaPlugin {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static String TAG = "GCMPlugin";
    private static GCMPlugin instance;
    private CallbackContext ongoingContext;
    private CallbackContext listenContext;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.v(TAG, "initialize");
        instance = this;
    }

    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.v(TAG, "GCMPlugin.execute(" + action + ", ...)");
        if (action.equals("register")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    register(callbackContext);
                }
            });
            return true;
        } else if (action.equals("unregister")) {
            unregister(callbackContext);
            return true;
        } else if (action.equals("getPendingNotifications")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    getPendingNotifications(callbackContext);
                }
            });
            return true;
        } else if (action.equals("listen")) {
            listenContext = callbackContext;
            return true;
        } else if (action.equals("setBadge")) {
            return true;
        }

        return false;
    }

    @Override
    public void onPause(boolean multitasking) {
        Log.v(TAG, "onPause");
        instance = null;
    }

    @Override
    public void onResume(boolean multitasking) {
        Log.v(TAG, "onResume");
        instance = this;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        instance = null;
    }

    public void onRegistrationResponse(String regId) {
        if (ongoingContext == null) {
            Log.w(TAG, "ongoingContext is null when getting response");
            return;
        }

        JSONObject response = new JSONObject();
        try {
            response.put("platform", "android");
            response.put("registrationId", regId);
        } catch (JSONException e) {
            Log.w(TAG, "failed to construct JSON response", e);
        }
        ongoingContext.success(response);
        ongoingContext = null;
    }

    public void onRegistrationError(String errorId) {
        if (ongoingContext == null) {
            Log.w(TAG, "ongoingContext is null when getting error response");
            return;
        }

        ongoingContext.error(errorId);
        ongoingContext = null;
    }

    public void onMessage(Bundle message) throws JSONException {
        Log.v(TAG, "Recieved message: " + message + ", context: " + listenContext);
        if (listenContext == null) {
            return;
        }

        JSONObject response = bundleToJSON(message);
        PluginResult result = new PluginResult(PluginResult.Status.OK, response);
        result.setKeepCallback(true);
        listenContext.sendPluginResult(result);
    }

    private void register(CallbackContext context) {
        if (checkPlayServices()) {
            Log.i(TAG, "Google Play Services OK, launching GCMRegistrationService");
            ongoingContext = context;
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(cordova.getActivity(), GCMRegistrationService.class);
            intent.putExtra("gcmSenderId", preferences.getString("GcmSenderId", ""));
            Activity a = cordova.getActivity();
            a.startService(intent);
        } else {
            context.error("Google Play Services not found");
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        try {
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(cordova.getActivity());
            if (resultCode != ConnectionResult.SUCCESS) {
                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                    GooglePlayServicesUtil.getErrorDialog(resultCode, cordova.getActivity(),
                            PLAY_SERVICES_RESOLUTION_REQUEST).show();
                } else {
                    Log.i(TAG, "This device is not supported.");
                }
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception when checking for google play services", e);
        }
        return false;
    }

    private void unregister(CallbackContext context) throws JSONException {
        JSONObject response = new JSONObject();
        context.success(response);
    }

    private void getPendingNotifications(CallbackContext context) {
        try {
            CordovaActivity tz = (CordovaActivity)this.cordova.getActivity();
            Intent i = tz.getIntent();
            JSONArray response = new JSONArray();
            if (i != null) {
                Bundle extras = i.getExtras();
                if (extras != null) {
                    JSONObject notification = bundleToJSON(extras);
                    response.put(notification);
                }
            }
            context.success(response);
        } catch (JSONException e) {
            context.error(e.getMessage());
        }
    }

    private JSONObject bundleToJSON(Bundle b) throws JSONException {
        JSONObject j = new JSONObject();
        for (String key : b.keySet()) {
            Object o = b.get(key);
            j.put(key.replace("com.timezynk.mobile.", ""), o);
        }
        return j;
    }

    public static GCMPlugin getInstance() {
        return instance;
    }
}
