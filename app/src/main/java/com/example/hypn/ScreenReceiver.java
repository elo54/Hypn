package com.example.hypn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenReceiver extends BroadcastReceiver {

    private boolean screenOff;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            screenOff = true;
            Log.d("Screen state", "Receiver : Screen is turned off.");
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            screenOff = false;
            Log.d("Screen state", "Receiver : Screen is turned on.");
        }
        /*Intent i = new Intent(context, UpdateService.class);
        i.putExtra("screen_state", screenOff);
        context.startService(i);*/
    }

    public boolean wasScreenOff() {
        return screenOff;
    }
}
