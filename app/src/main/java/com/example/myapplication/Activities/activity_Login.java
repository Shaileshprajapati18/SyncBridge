package com.example.myapplication.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class activity_Login extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button loginButton;
    private TextView registerRedirect;
    private TextView forgotPasswordText;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;  // Added for Realtime Database
    private ProgressBar progressBar;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(activity_Login.this, open_screen.class);
            startActivity(intent);
            finish();
        }

        // Initialize UI elements
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        registerRedirect = findViewById(R.id.registerRedirect);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        progressBar = findViewById(R.id.progressbar);

        // Password visibility toggle (unchanged)
        passwordField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (passwordField.getCompoundDrawables()[2] != null) {
                        if (event.getRawX() >= (passwordField.getRight() - passwordField.getCompoundDrawables()[2].getBounds().width())) {
                            if (isPasswordVisible) {
                                passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                passwordField.setCompoundDrawablesWithIntrinsicBounds(
                                        ContextCompat.getDrawable(activity_Login.this, R.drawable.baseline_lock_24),
                                        null,
                                        ContextCompat.getDrawable(activity_Login.this, R.drawable.visibility),
                                        null
                                );
                            } else {
                                passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                                passwordField.setCompoundDrawablesWithIntrinsicBounds(
                                        ContextCompat.getDrawable(activity_Login.this, R.drawable.baseline_lock_24),
                                        null,
                                        ContextCompat.getDrawable(activity_Login.this, R.drawable.visibility_off),
                                        null
                                );
                            }
                            isPasswordVisible = !isPasswordVisible;
                            passwordField.setSelection(passwordField.getText().length());
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPasswordDialog();
            }
        });

        registerRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_Login.this, activity_register.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        // Validation checks
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Email is required");
            emailField.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Invalid email format");
            emailField.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Password is required");
            passwordField.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Firebase login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            // Login successful, update password in Realtime Database
                            String userId = email.replace(".", ",");
                            updatePasswordInDatabase(userId, password);

                            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("UserID", userId);
                            editor.apply();

                            Intent intent = new Intent(activity_Login.this, open_screen.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(activity_Login.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // New method to update password in Realtime Database
    private void updatePasswordInDatabase(String userId, String newPassword) {
        reference.child(userId).child("password").setValue(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Password updated successfully in Realtime Database
                            Toast.makeText(activity_Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity_Login.this, "Failed to update password in database", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showForgotPasswordDialog() {
        View customDialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        final EditText emailInput = customDialogView.findViewById(R.id.emailInput);
        Button positiveButton = customDialogView.findViewById(R.id.dialog_positive);
        Button negativeButton = customDialogView.findViewById(R.id.dialog_negative);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(customDialogView);
        final AlertDialog dialog = builder.create();

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    emailInput.setError("Email is required");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailInput.setError("Invalid email format");
                } else {
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(activity_Login.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(activity_Login.this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}