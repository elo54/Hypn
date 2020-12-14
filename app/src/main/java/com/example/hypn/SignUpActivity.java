package com.example.hypn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText editEmail, editPassword, confirmPassword;
    private Button signUp;
    private TextView goBack;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        signUp = findViewById(R.id.signUp);
        goBack = findViewById(R.id.goBack);

        mAuth = FirebaseAuth.getInstance();

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkFields()) {
                    signUp();
                } else {
                    Toast.makeText(getApplicationContext(), "Veuillez compléter tous les champs", Toast.LENGTH_SHORT).show();
                }
            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void signUp() {
        final String email = editEmail.getText().toString().trim();
        final String password = editPassword.getText().toString().trim();

        if (editPassword.getText().toString().trim().equals(confirmPassword.getText().toString().trim())) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                createSettings(user);
                                startActivity(new Intent(SignUpActivity.this, SettingsActivity.class));
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(getApplicationContext(), "Erreur d'authentification.",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        } else{
            Toast.makeText(getApplicationContext(), "Confirmation de mot de passe incorrecte", Toast.LENGTH_LONG).show();
            confirmPassword.setError("Les deux mots de passe ne sont pas identiques");
        }

    }

    private void createSettings(FirebaseUser user) {
        String userID = user.getUid();
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        mRef.child("startTime").setValue("19:00");
        mRef.child("curfewTime").setValue("20:00");
        mRef.child("endTime").setValue("8:00");
        mRef.child("chargeTime").setValue("01:00");
        mRef.child("useTime").setValue("00:30");
    }

    private boolean checkFields() {
        if(editEmail.getText().toString().trim().length() > 0 && editPassword.getText().toString().trim().length() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mAuth.getCurrentUser()!=null) {
            mAuth.signOut();
        }
        openMainActivity();
    }

    private void openMainActivity() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void openSettingsActivity() {
        Intent intent = new Intent(SignUpActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}