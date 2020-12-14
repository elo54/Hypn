package com.example.hypn;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY_PERMISSION = 1;

    private EditText editStartTime, editEndTime;
    private EditText editChargeTime, editUseTime;
    private Button confirmSettings;
    private TextView goBack;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        editStartTime = findViewById(R.id.editStartTime);
        editEndTime = findViewById(R.id.editEndTime);
        editChargeTime = findViewById(R.id.editChargeTime);
        editUseTime = findViewById(R.id.editUseTime);
        confirmSettings = findViewById(R.id.confirmSettings);
        goBack = findViewById(R.id.goBack);

        checkOverlayPermissions();

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser()!=null) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            //ACCES BDD
            mDatabase = FirebaseDatabase.getInstance();
            DatabaseReference mRef = mDatabase.getReference("Users").child(user.getUid());
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //retrieve data from database and fill editText accordingly
                    GenericTypeIndicator<HashMap<String, String>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, String>>() {
                    };
                    HashMap<String, String> hashmap = dataSnapshot.getValue(genericTypeIndicator);
                    editStartTime.setText(hashmap.get("startTime"));
                    editEndTime.setText(hashmap.get("endTime"));
                    editChargeTime.setText(hashmap.get("chargeTime"));
                    editUseTime.setText(hashmap.get("useTime"));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            openLoginActivity();
        }

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        editStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayTimePickerClock(editStartTime);
            }
        });

        editEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayTimePickerClock(editEndTime);
            }
        });

        editChargeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayTimePickerSpinner(editChargeTime);
            }
        });

        editUseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayTimePickerSpinner(editUseTime);
            }
        });

        confirmSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

    }

    private void saveSettings() {
        //save to database and return to mainActivity
        String startTime = editStartTime.getText().toString().trim();
        String endTime = editEndTime.getText().toString().trim();
        String chargeTime = editChargeTime.getText().toString().trim();
        String useTime = editUseTime.getText().toString().trim();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userID = user.getUid();
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        mRef.child("startTime").setValue(startTime);
        mRef.child("endTime").setValue(endTime);
        mRef.child("chargeTime").setValue(chargeTime);
        mRef.child("useTime").setValue(useTime);

        openMainActivity();
    }

    private void displayTimePickerClock(EditText chooseTime) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(SettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                chooseTime.setText(hourOfDay + ":" + minutes);
                chooseTime.setText(String.format("%02d:%02d", hourOfDay, minutes));
                chooseTime.setGravity(Gravity.CENTER);
            }
        }, 0, 0, true);
        timePickerDialog.show();
    }

    private void displayTimePickerSpinner(EditText chooseTime) {
        TimePickerDialog nTimePickerDialog = new TimePickerDialog(this, 2,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker pTimePicker, int hourOfDay, int minutes) {
                        chooseTime.setText(hourOfDay + ":" + minutes);
                        chooseTime.setText(String.format("%02d:%02d", hourOfDay, minutes));
                        chooseTime.setGravity(Gravity.CENTER);
                    }
                },
                0, 0, true);
        nTimePickerDialog.setTitle("Choisis le temps");
        nTimePickerDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //alert dialog to be sure to quit without saving changes
        showAlertDialog();
    }

    private void showAlertDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(SettingsActivity.this)
                .setTitle("Quitter les réglages")
                .setMessage("Etes-vous sûr de vouloir quitter sans sauvegarder?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        openMainActivity();
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void checkOverlayPermissions() {
        //request permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(this)){
                // ask for setting
                Toast.makeText(getApplicationContext(), "Demande d'autorisation pour le blocage du téléphone", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            } else {
                //permission is granted
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // permission granted...
                    Toast.makeText(getApplicationContext(), "Le blocage est autorisé", Toast.LENGTH_SHORT).show();
                } else {
                    // permission not granted...
                    Toast.makeText(getApplicationContext(), "Allez dans les paramètres pour autoriser le blocage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void openMainActivity() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void openLoginActivity() {
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        startActivity(intent);
    }

}