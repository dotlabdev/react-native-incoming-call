package com.incomingcall;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.PowerManager;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Intent;
import android.app.Activity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;


public class IncomingCallModule extends ReactContextBaseJavaModule {
    private PowerManager.WakeLock mWakeLock;
    public StringBuilder number = new StringBuilder("");
    public static ReactApplicationContext reactContext;
    public static Activity mainActivity;
    private static final int WINDOW_MANAGER_FLAGS = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;

    private static final int POWER_MANAGER_FLAGS = PowerManager.FULL_WAKE_LOCK
            | PowerManager.ACQUIRE_CAUSES_WAKEUP
            | PowerManager.SCREEN_BRIGHT_WAKE_LOCK
            | PowerManager.ON_AFTER_RELEASE;

    private static final int INTENT_FLAGS = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP;

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
                i.addFlags(INTENT_FLAGS);
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
                mWakeLock = powerManager.newWakeLock(
                        POWER_MANAGER_FLAGS, "IncomingCallModule:mywake");

                mWakeLock.acquire();

                Window window = mCurrentActivity.getWindow();
                window.addFlags(WINDOW_MANAGER_FLAGS | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
    }

    @ReactMethod
    public void resetFlags() {
        UiThreadUtil.runOnUiThread(new Runnable() {
            public void run() {
                if (mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
                mWakeLock = null;
                Activity mCurrentActivity = getCurrentActivity();
                if (mCurrentActivity == null) {
                    return;
                }

                Window window = mCurrentActivity.getWindow();
                window.clearFlags(WINDOW_MANAGER_FLAGS | POWER_MANAGER_FLAGS | INTENT_FLAGS);
            }
        });
    }

    @ReactMethod
    public void isMuted(final Promise promise) {
        UiThreadUtil.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    Activity mCurrentActivity = getCurrentActivity();
                    if (mCurrentActivity == null) {
                        promise.reject("NO CURRENT ACTIVITY FOUND");
                    }

                    AudioManager audio = (AudioManager) mCurrentActivity.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

                    if (audio.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
                        promise.resolve(false);
                    else promise.resolve(true);

                } catch (Exception e) {
                    Log.e("RNIncomingCall", "getRingerMode", e);
                    promise.reject(e);
                }
            }
        });
    }

}
