package com.incomingcall;

import android.os.Build;
import android.app.KeyguardManager;
import android.app.Activity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;


public class IncomingCallModule extends ReactContextBaseJavaModule {
    public static ReactApplicationContext reactContext;
    public static Activity mainActivity;
    private static final int WINDOW_MANAGER_FLAGS = WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

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
    public void unlockDevice() {
        UiThreadUtil.runOnUiThread(new Runnable() {
            public void run() {
                Activity mCurrentActivity = getCurrentActivity();
                if (mCurrentActivity == null) {
                    return;
                }
               if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    Window window = mCurrentActivity.getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                    KeyguardManager keyguardManager = (KeyguardManager) reactContext.getSystemService(reactContext.KEYGUARD_SERVICE);

                    if (keyguardManager.isKeyguardLocked()) {
                        keyguardManager.requestDismissKeyguard(mCurrentActivity, null);
                    }
                }
                else {
                    Window window = mCurrentActivity.getWindow();
                    window.addFlags(WINDOW_MANAGER_FLAGS);
                }
            }
        });
    }

    @ReactMethod
    public void resetFlags() {
        UiThreadUtil.runOnUiThread(new Runnable() {
            public void run() {
                Activity mCurrentActivity = getCurrentActivity();
                if (mCurrentActivity == null) {
                    return;
                }
                Window window = mCurrentActivity.getWindow();
                window.clearFlags(WINDOW_MANAGER_FLAGS);
            }
        });
    }
}
