package com.example.hypn;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY_PERMISSION = 1;
    private ImageView settings;

    private Button lockPhone;

    private Button incrementChrono, decrementChrono;
    private Chronometer myChrono;
    private boolean isChronoRunning = false;
    private long pauseOffset = 0;

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //INIT DEVICE MANAGER
        mDevicePolicyManager = (DevicePolicyManager)getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(this, MyAdminReceiver.class);

        settings = findViewById(R.id.settings);
        lockPhone = findViewById(R.id.lockPhone);
        incrementChrono = findViewById(R.id.incrementChrono);
        decrementChrono = findViewById(R.id.decrementChrono);

        myChrono = findViewById(R.id.myChrono);
        myChrono.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometerChanged) {
                if (myChrono.isCountDown()) {
                    //si le temps passé et supérieur à la base qu'on avait défini
                    /*if (SystemClock.elapsedRealtime() >= myChrono.getBase()) {
                        myChrono.setBase(SystemClock.elapsedRealtime());
                        myChrono.setCountDown(false);
                        Toast.makeText(getApplicationContext(), "Bloquer le téléphone", Toast.LENGTH_SHORT).show();
                    }*/
                }
            }
        });
        chronoStart();

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });

        lockPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLockPhone();
            }
        });

        incrementChrono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onIncrementChrono();
            }
        });

        decrementChrono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDecrementChrono();
            }
        });

    }

    public boolean isScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
            for (Display display : dm.getDisplays()) {
                if (display.getState() == Display.STATE_ON) {
                    return true;
                }
            }
            return false;
        } else {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            return pm.isScreenOn();
        }
    }

    private void chronoStart() {
        if (!isChronoRunning) {
            //on initialise le chrono au temps qu'on veut
            //mChronometer.setBase(SystemClock.elapsedRealtime() - (nr_of_min * 60000 + nr_of_sec * 1000)))
            //SystemClock.elapsedRealtime() = time since the system was booted
            myChrono.setBase(SystemClock.elapsedRealtime() - 90*60000);
            myChrono.start();
            onDecrementChrono();
            isChronoRunning = true;
        }
    }

    private void onDecrementChrono() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!myChrono.isCountDown()) {
                long elapsedMillis = SystemClock.elapsedRealtime() - myChrono.getBase();
                myChrono.setBase(SystemClock.elapsedRealtime() + elapsedMillis);
                myChrono.setCountDown(true);
            }
        }
    }

    private void onIncrementChrono() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (myChrono.isCountDown()) {
                long elapsedMillis = SystemClock.elapsedRealtime() - myChrono.getBase();
                myChrono.setBase(SystemClock.elapsedRealtime() + elapsedMillis);
                myChrono.setCountDown(false);
            }
        }
    }

    private void onLockPhone() {
        //check if we can lock phone
        boolean isAdmin = mDevicePolicyManager.isAdminActive(mComponentName);
        if (isAdmin) {
            //create a pop-up that always stay on top
            checkOverlayPermissions();
            //do stuff to block phone
            mDevicePolicyManager.lockNow();

        } else {
            Toast.makeText(getApplicationContext(), "Go to settings to enable admin", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkOverlayPermissions() {
        //request permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(this)){
                // ask for setting
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            } else {
                //permission is granted, create overlay
                creatingOverlay();
            }
        }
    }

    private void creatingOverlay() {
        //create view
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        WindowManager mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                LAYOUT_FLAG ,
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                PixelFormat.TRANSLUCENT);
        mLayoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;


        ViewGroup mView = (ViewGroup) getLayoutInflater().inflate(R.layout.overlay, null);
        mWindowManager.addView(mView, mLayoutParams);

        Button unlockPhone = mView.findViewById(R.id.unlockPhone);
        unlockPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWindowManager.removeView(mView);
            }
        });

        //create timer for the view
        //view destroys when the timer is done for
        /*final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after delay = 10s
                mWindowManager.removeView(mView);
            }
        }, 10000);*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // permission granted...
                    Toast.makeText(getApplicationContext(), "Overlaying enabled", Toast.LENGTH_SHORT).show();
                    creatingOverlay();
                } else {
                    // permission not granted...
                    Toast.makeText(getApplicationContext(), "Go to settings to enable overlaying", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void openLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}