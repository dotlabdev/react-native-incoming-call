package com.incomingcall;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import cn.nodemedia.NodePlayer;
import cn.nodemedia.NodePlayerView;

public class UnlockScreenActivity extends AppCompatActivity implements UnlockScreenActivityInterface {

    private static final String TAG = "MessagingService";
    private NodePlayer mNodePlayer;
    private TextView tvBody;
    private TextView tvName;
    private TextView tvNumber;
    private NodePlayerView player;
    private String uuid = "";
    private String packageName = "";
    static boolean active = false;

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_call_incoming);

        tvBody = findViewById(R.id.tvBody);
        tvName = findViewById(R.id.tvName);
        tvNumber=findViewById(R.id.tvPhoneNumber);
        player = findViewById(R.id.play_surface);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("body")) {
                String body = bundle.getString("body");
                tvBody.setText(body);
            }
            if (bundle.containsKey("displayName")) {
                String displayName = bundle.getString("displayName");
                tvName.setText(displayName);
            }
            if (bundle.containsKey("number")) {
                String displayName = bundle.getString("number");
                tvNumber.setText(displayName);
            }
            if (bundle.containsKey("packageName")) {
                String packageName = bundle.getString("packageName");
            }
            if (bundle.containsKey("avatar")) {
                String playerUri = bundle.getString("avatar");

                mNodePlayer = new NodePlayer(this);
                mNodePlayer.setPlayerView(player);
                mNodePlayer.setInputUrl(playerUri);
                NodePlayerView.UIViewContentMode mode = NodePlayerView.UIViewContentMode.valueOf("ScaleAspectFill");
                player.setUIViewContentMode(mode);
                mNodePlayer.setAudioEnable(false);
                mNodePlayer.setBufferTime(300);
                mNodePlayer.setMaxBufferTime(1000);
                mNodePlayer.start();
            }
            if (bundle.containsKey("uuid")) {
                uuid = bundle.getString("uuid");
            }
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        AnimateImage acceptCallBtn = findViewById(R.id.ivAcceptCall);
        acceptCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    acceptDialing();
                } catch (Exception e) {
                    WritableMap params = Arguments.createMap();
                    params.putString("message", e.getMessage());
                    sendEvent("error", params);
                    dismissDialing();
                }
            }
        });

        AnimateImage rejectCallBtn = findViewById(R.id.ivDeclineCall);
        rejectCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissDialing();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Dont back
    }

    private void acceptDialing() {
        // Intent i = new Intent(this, MainActivity.class);
        Intent i = IncomingCallModule.reactContext.getPackageManager().getLaunchIntentForPackage(packageName);
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.putExtra("uuid", uuid);
            IncomingCallModule.reactContext.startActivity(i);
        } else {
            // No intent
            WritableMap params = Arguments.createMap();
            params.putString("message", "No intent");
            sendEvent("error", params);
        }

        WritableMap params = Arguments.createMap();
        params.putBoolean("done", true);
        params.putString("uuid", uuid);
        sendEvent("answerCall", params);
        finish();
    }

    private void dismissDialing() {
        mNodePlayer.stop();
        WritableMap params = Arguments.createMap();
        params.putBoolean("done", false);
        params.putString("uuid", uuid);

        sendEvent("endCall", params);
        finish();
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "onConnected: ");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "onDisconnected: ");

    }

    @Override
    public void onConnectFailure() {
        Log.d(TAG, "onConnectFailure: ");

    }

    @Override
    public void onIncoming(ReadableMap params) {
        Log.d(TAG, "onIncoming: ");
    }

    private void sendEvent(String eventName, WritableMap params) {
        IncomingCallModule.reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }
}