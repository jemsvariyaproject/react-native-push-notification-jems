package com.dieam.reactnativepushnotification.modules;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class RNPushNotificationListenerService extends FirebaseMessagingService {

    private RNReceivedMessageHandler mMessageReceivedHandler;
    private FirebaseMessagingService mFirebaseServiceDelegate;
    private RemoteMessage temp;

    public RNPushNotificationListenerService() {
        super();
        this.mMessageReceivedHandler = new RNReceivedMessageHandler(this);
    }

    public RNPushNotificationListenerService(FirebaseMessagingService delegate) {
        super();
        this.mFirebaseServiceDelegate = delegate;
        this.mMessageReceivedHandler = new RNReceivedMessageHandler(delegate);
    }

    @Override
    public void handleIntent(Intent intent) {

        boolean isAppForeground = isApplicationInForeground(this);
        Log.d(LOG_TAG, "handleIntent: ................ isApplicationInForeground(this) : "+isAppForeground);
        if(!isAppForeground){
            mMessageReceivedHandler.handleReceivedMessage(new RemoteMessage(intent.getExtras()));
            return;
        }
        Log.d(LOG_TAG, "handleIntent: ................ super >> execute");
        super.handleIntent(intent);
    }

    public boolean isApplicationInForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        if (processInfos != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
                if (processInfo.processName.equals(context.getPackageName()) && processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && processInfo.pkgList.length > 0) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public void onNewToken(String token) {
        final String deviceToken = token;
        Log.d(LOG_TAG, "onNewToken: deviceToken:"+deviceToken);
        final FirebaseMessagingService serviceRef = (this.mFirebaseServiceDelegate == null) ? this : this.mFirebaseServiceDelegate;
        Log.d(LOG_TAG, "Refreshed token: " + deviceToken);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                // Construct and load our normal React JS code bundle
                final ReactInstanceManager mReactInstanceManager = ((ReactApplication)serviceRef.getApplication()).getReactNativeHost().getReactInstanceManager();
                ReactContext context = mReactInstanceManager.getCurrentReactContext();
                // If it's constructed, send a notification
                if (context != null) {
                    handleNewToken((ReactApplicationContext) context, deviceToken);
                } else {
                    // Otherwise wait for construction, then send the notification
                    mReactInstanceManager.addReactInstanceEventListener(new ReactInstanceManager.ReactInstanceEventListener() {
                        public void onReactContextInitialized(ReactContext context) {
                            handleNewToken((ReactApplicationContext) context, deviceToken);
                            mReactInstanceManager.removeReactInstanceEventListener(this);
                        }
                    });
                    if (!mReactInstanceManager.hasStartedCreatingInitialContext()) {
                        // Construct it in the background
                        mReactInstanceManager.createReactContextInBackground();
                    }
                }
            }
        });
    }

    private void handleNewToken(ReactApplicationContext context, String token) {
        Log.d(LOG_TAG, "handleNewToken: ");
        RNPushNotificationJsDelivery jsDelivery = new RNPushNotificationJsDelivery(context);

        WritableMap params = Arguments.createMap();
        params.putString("deviceToken", token);
        jsDelivery.sendEvent("remoteNotificationsRegistered", params);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        temp = message;
        Log.d(LOG_TAG, "onMessageReceived: ");
        mMessageReceivedHandler.handleReceivedMessage(message);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy: ");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(LOG_TAG, "onTaskRemoved: ");
    }
}
