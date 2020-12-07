package com.example.hypn;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class UpdateService extends Service {

    private ScreenReceiver mScreenReceiver = null;

    @Override
    public void onCreate() {
        super.onCreate();
        // register receiver that handles screen on and screen off logic
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenReceiver = new ScreenReceiver();
        registerReceiver(mScreenReceiver, filter);

        Log.d("Screen state", "Service is running.");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        boolean screenOff = intent.getBooleanExtra("screen_state", false);
        if (screenOff) {
            // your code
            Log.d("Screen state", "Service: Screen is off.");
        } else {
            // your code
            Log.d("Screen state", "Service: Screen is on.");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister screenOnOffReceiver when destroy.
        if(mScreenReceiver!=null)
        {
            unregisterReceiver(mScreenReceiver);
            Log.d("Receiver state", "Service onDestroy: screenOnOffReceiver is unregistered.");
        }
    }
}
