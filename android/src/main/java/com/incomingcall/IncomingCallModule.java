package com.incomingcall;

import android.content.Intent;
import android.app.Activity;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class IncomingCallModule extends ReactContextBaseJavaModule {

    public static ReactApplicationContext reactContext;
    public static Activity mainActivity;

    public IncomingCallModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
        mainActivity = getCurrentActivity();
    }

    @Override
    public String getName() {
        return "IncomingCall";
    }

    @ReactMethod
    public void display(String number) {
        String packageNames = reactContext.getPackageName();
        Intent launchIntent = reactContext.getPackageManager().getLaunchIntentForPackage(packageNames);
        String className = launchIntent.getComponent().getClassName();
            try {
                Class<?> activityClass = Class.forName(className);
                Intent i = new Intent(reactContext, activityClass);
                if (reactContext != null) {
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.putExtra("number",number);
                    i.putExtra("fromBackground", true);
                    reactContext.startActivity(i);
                }
            } catch(Exception e) {
                Log.e("RNIncomingCall", "Class not found", e);
                return;
            }

    }

    @ReactMethod
    public void dismiss() {
        final Activity activity = reactContext.getCurrentActivity();
        assert activity != null;
    }
}
