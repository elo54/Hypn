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
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
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

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY_PERMISSION = 1;
    private ImageView settings;

    private Button lockPhone;
    private boolean isLocked = false;

    private ScreenReceiver mScreenReceiver;

    private static final long START_TIME_IN_MILLIS = 1 * 60000;
    private long mEndTime;
    private long mEndTime2 = 0;
    private TextView timeLeft;
    private CountDownTimer mCountDownTimer;
    private CountDownTimer mCountDownTimer2;
    private boolean isTimerRunning;
    private boolean isChronoRunning = false;
    private long mTimeLeftInMillis;
    private long mTimeLeftInMillis2 = 0;

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

        //INIT VIEW
        settings = findViewById(R.id.settings);
        timeLeft = findViewById(R.id.timeLeft);

        //INIT RECEIVER
        // register receiver that handles screen on and screen off logic
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenReceiver = new ScreenReceiver();
        registerReceiver(mScreenReceiver, filter);

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
                lockPhone();
            }
        });

        resetTimer();
        startTimer();

    }

    private void lockPhone() {
        //check if we can lock phone
        boolean isAdmin = mDevicePolicyManager.isAdminActive(mComponentName);
        if (isAdmin) {
            Toast.makeText(getApplicationContext(), "Phone locked", Toast.LENGTH_SHORT).show();
            //do stuff to block phone
            //mDevicePolicyManager.lockNow();

            //create a pop-up that always stay on top
            checkOverlayPermissions();

        } else {
            Toast.makeText(getApplicationContext(), "Go to settings to enable admin", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetTimer() {
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        updateCountDownText();
    }

    private void pauseTimer() {
        mCountDownTimer.cancel();
        isTimerRunning = false;
    }

    private void startTimer() {
        //its the timer that is displayed
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;

        //timer starts at mTimeLeftInMillis and updates every 1000ms
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                Toast.makeText(getApplicationContext(), "You are done now", Toast.LENGTH_SHORT).show();
                lockPhone();
            }
        }.start();

        isTimerRunning = true;
    }

    private void startSecondTimer() {
        //this timer is used to calculate the time spent while the screen is off
        mTimeLeftInMillis2 = mTimeLeftInMillis;
        mEndTime2 = System.currentTimeMillis() + mTimeLeftInMillis2;

        //timer starts at mTimeLeftInMillis and updates every 1000ms
        mCountDownTimer2 = new CountDownTimer(mTimeLeftInMillis2, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis2 = millisUntilFinished;
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

    private void updateCountDownText() {
        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        timeLeft.setText(timeLeftFormatted);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //screen was on
        if(!mScreenReceiver.wasScreenOff()) {
            Log.d("Screen state", "Activity.onStop: Screen is off. Pause timer");
            pauseTimer();
            startSecondTimer();
        }

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("millisLeft", mTimeLeftInMillis);
        editor.putBoolean("timerRunning", isTimerRunning);
        editor.putLong("endTime", mEndTime);

        editor.apply();

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister screenReceiver when destroy.
        if(mScreenReceiver!=null)
        {
            unregisterReceiver(mScreenReceiver);
            Log.d("Receiver state", "onDestroy: screenOnOffReceiver is unregistered.");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        mTimeLeftInMillis = prefs.getLong("millisLeft", START_TIME_IN_MILLIS);
        isTimerRunning = prefs.getBoolean("timerRunning", false);

        updateCountDownText();

        if (isTimerRunning) {
            mEndTime = prefs.getLong("endTime", 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();

            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                isTimerRunning = false;
                updateCountDownText();
            } else {
                startTimer();
            }
        } else {
            if (mScreenReceiver.wasScreenOff()) {
                //screen was off, phone was locked, we continue timer to the time it stopped
                startTimer();

            } else {
                //screen was on, timer was paused, we need to continue timer + add time while app was in background
                if (mTimeLeftInMillis2!=0 && mEndTime2!=0) {
                    //to verify that the timer was started
                    mTimeLeftInMillis = mTimeLeftInMillis2;
                    mEndTime = mEndTime2;

                    startTimer();
                }
            }
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

        Chronometer chronometer = mView.findViewById(R.id.chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if ((SystemClock.elapsedRealtime() - chronometer.getBase()) >= 10000) {
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    Toast.makeText(MainActivity.this, "Charged!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        startChronometer(chronometer);

        Button unlockPhone = mView.findViewById(R.id.unlockPhone);
        unlockPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWindowManager.removeView(mView);
                resetTimer();
                startTimer();
                chronometer.stop();
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

    private void startChronometer(Chronometer chronometer) {
        if (!isChronoRunning) {
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            isChronoRunning = true;
        }

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