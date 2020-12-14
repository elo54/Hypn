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

public class SignInActivity extends AppCompatActivity {
    
    private EditText enterEmail, enterPassword;
    private Button signIn;
    private TextView forgotPassword;

    private TextView goBack;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        
        enterEmail = findViewById(R.id.enterEmail);
        enterPassword = findViewById(R.id.enterPassword);
        signIn = findViewById(R.id.signIn);
        forgotPassword = findViewById(R.id.forgotPassword);
        goBack = findViewById(R.id.goBack);

        mAuth = FirebaseAuth.getInstance();
        
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkFields()) {
                    signIn(enterEmail.getText().toString(), enterPassword.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "Veuillez compléter tous les champs", Toast.LENGTH_SHORT).show();
                }

            }
        });
        
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrievePassword();
            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private boolean checkFields() {
        if(enterEmail.getText().toString().trim().length() > 0 && enterPassword.getText().toString().trim().length() > 0) {
            return true;
        }
        return false;
    }

    private void openSettingsActivity() {
        Intent intent = new Intent(SignInActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void retrievePassword() {
        String emailAddress = enterEmail.getText().toString();
        if (emailAddress.trim().length() > 0) {
            mAuth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Un mail vient de vous être envoyé", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Un problème est survenu", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "Veuillez renseigner votre email", Toast.LENGTH_SHORT).show();
        }
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            openSettingsActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(), "Erreur d'authentification",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
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
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(intent);
    }
}