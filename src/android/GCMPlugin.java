package com.timezynk.cordova.notification;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import java.util.Map;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
                    getToken(callbackContext);
                }
            });
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
        this.onRegistrationResponse(regId, ongoingContext);
        ongoingContext = null;
    }

    public void onRegistrationResponse(String regId, CallbackContext context) {
        JSONObject response = new JSONObject();
        try {
            response.put("platform", "fcm");
            response.put("registrationId", regId);
        } catch (JSONException e) {
            Log.w(TAG, "failed to construct JSON response", e);
        }
        context.success(response);
    }

    public void onMessage(Map message) throws JSONException {
        Log.v(TAG, "Recieved message: " + message + ", context: " + listenContext);
        if (listenContext == null) {
            return;
        }

        JSONObject response = new JSONObject(message);
        PluginResult result = new PluginResult(PluginResult.Status.OK, response);
        result.setKeepCallback(true);
        listenContext.sendPluginResult(result);
    }

    private void getToken(CallbackContext context) {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        this.onRegistrationResponse(refreshedToken, context);
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
