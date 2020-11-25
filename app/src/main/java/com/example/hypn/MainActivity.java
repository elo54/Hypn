package com.example.hypn;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY_PERMISSION = 1;
    private ImageView settings, timelapse;

    private Button lockPhone;
    private boolean isLocked = false;

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //INIT DEVICE MANAGER
        mDevicePolicyManager = (DevicePolicyManager)getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(this, MyAdminReceiver.class);

        settings = findViewById(R.id.settings);
        timelapse = findViewById(R.id.timelapse);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });

        lockPhone = findViewById(R.id.lockPhone);

        lockPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if we can lock phone
                boolean isAdmin = mDevicePolicyManager.isAdminActive(mComponentName);
                if (isAdmin) {
                    //check status
                    if (isLocked) {
                        isLocked = false;
                        lockPhone.setText("Lock Phone");
                        timelapse.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_timelapse_24));
                        Toast.makeText(getApplicationContext(), "Phone unlocked", Toast.LENGTH_SHORT).show();
                    } else {
                        isLocked = true;
                        lockPhone.setText("Unlock Phone");
                        timelapse.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_lock_24));
                        Toast.makeText(getApplicationContext(), "Phone locked", Toast.LENGTH_SHORT).show();
                        //do stuff to block phone
                        //mDevicePolicyManager.lockNow();

                        //create a pop-up that always stay on top
                        checkOverlayPermissions();

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Go to settings to enable admin", Toast.LENGTH_SHORT).show();
                }

            }
        });

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

        //create timer for the view
        //view destroys when the timer is done for
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after delay = 10s
                mWindowManager.removeView(mView);
            }
        }, 10000);
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