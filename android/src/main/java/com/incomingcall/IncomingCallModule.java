package com.incomingcall;

import android.os.Handler;
import android.os.PowerManager;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Intent;
import android.app.Activity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;


public class IncomingCallModule extends ReactContextBaseJavaModule {
    public StringBuilder number = new StringBuilder("");
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
    public void getNumber(Promise promise) {
        if (number.length() > 0) {
            promise.resolve(number.toString());

            new Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            number.setLength(0);
                        }
                    },
                    3000);
        } else {
            promise.resolve(null);

        }

    }

    @ReactMethod
    public void display(String num, Boolean shouldStoreNumber) {
        String packageNames = reactContext.getPackageName();
        Intent launchIntent = reactContext.getPackageManager().getLaunchIntentForPackage(packageNames);
        String className = launchIntent.getComponent().getClassName();
        try {
            Class<?> activityClass = Class.forName(className);
            Intent i = new Intent(reactContext, activityClass);
            if (reactContext != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                if (shouldStoreNumber) {
                    number.append(num);
                }

                reactContext.startActivity(i);
            }
        } catch (Exception e) {
            Log.e("RNIncomingCall", "Class not found", e);
            return;
        }

    }

    @ReactMethod
    public void dismiss() {
        final Activity activity = reactContext.getCurrentActivity();
        assert activity != null;
    }

    @ReactMethod
    public void exitApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @ReactMethod
    public void unlockDevice() {
        UiThreadUtil.runOnUiThread(new Runnable() {
            public void run() {
                Activity mCurrentActivity = getCurrentActivity();
                if (mCurrentActivity == null) {
                    return;
                }
                KeyguardManager keyguardManager = (KeyguardManager) reactContext.getSystemService(reactContext.KEYGUARD_SERVICE);

                KeyguardLock keyguardLock = keyguardManager.newKeyguardLock(reactContext.KEYGUARD_SERVICE);
                keyguardLock.disableKeyguard();

                PowerManager powerManager = (PowerManager) reactContext.getSystemService(reactContext.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                        PowerManager.FULL_WAKE_LOCK
                                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                                | PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                                | PowerManager.ON_AFTER_RELEASE, "IncomingCallModule:mywake");
                wakeLock.setReferenceCounted(false);
                if ((wakeLock != null) &&           // we have a WakeLock
                        (wakeLock.isHeld() == false)) {  // but we don't hold it
                    wakeLock.acquire();
                }

                Window window = mCurrentActivity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            }
        });
    }
}
