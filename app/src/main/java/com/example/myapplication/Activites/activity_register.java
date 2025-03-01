package com.example.myapplication.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Model.RegisterModel;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.Timer;
import java.util.TimerTask;

public class activity_register extends AppCompatActivity {

    private EditText emailField, passwordField, firstNameField, lastNameField, phoneField;
    private Button registerButton;
    private TextView loginRedirect;
    private FirebaseAuth mAuth;
    private CountryCodePicker countryCodePicker;
    DatabaseReference reference;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView textView = findViewById(R.id.signup);
        Animation slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_text);
        textView.startAnimation(slideAnimation);

        slideAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startCharacterColorAnimation(textView, "REGISTER");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }
    private void startCharacterColorAnimation(TextView textView, String text) {
        Timer timer = new Timer();
        int delay = 0; // Initial delay
        int interval = 100; // Change color every 200ms

        timer.schedule(new TimerTask() {
            int index = 0; // Character index

            @Override
            public void run() {
                runOnUiThread(() -> {
                    SpannableString spannable = new SpannableString(text);

                    for (int i = 0; i < text.length(); i++) {
                        spannable.setSpan((Object) new ForegroundColorSpan(Color.WHITE),  i,  (i + 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    spannable.setSpan((Object) new ForegroundColorSpan(Color.LTGRAY), index,  (index + 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    textView.setText(spannable);

                    index++;
                    if (index >= text.length()) {
                        index = 0; // Restart animation after the last character
                    }
                });
            }
        }, delay, interval);

        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        firstNameField = findViewById(R.id.firstname);
        lastNameField = findViewById(R.id.last_name);
        phoneField = findViewById(R.id.phone);
        registerButton = findViewById(R.id.registerButton);
        loginRedirect = findViewById(R.id.loginRedirect);
        countryCodePicker = findViewById(R.id.countryCodePicker);
        progressBar=findViewById(R.id.progressbar);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Redirect to login page if the user already has an account
        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_register.this, activity_Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void registerUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String firstName = firstNameField.getText().toString().trim();
        String lastName = lastNameField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();
        String fullPhoneNumber = countryCodePicker.getSelectedCountryCodeWithPlus() + phone;

        // Validate inputs
        if (TextUtils.isEmpty(firstName)) {
            firstNameField.setError("First name is required");
            firstNameField.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(lastName)) {
            lastNameField.setError("Last name is required");
            lastNameField.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            phoneField.setError("Phone number is required");
            phoneField.requestFocus();
            return;
        }
        if (!Patterns.PHONE.matcher(phone).matches() || !phone.matches("^[0-9]{10}$")) {
            phoneField.setError("Invalid phone number");
            phoneField.requestFocus();
            return;
        }
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
        if (password.length() < 6) {
            passwordField.setError("Password must be at least 6 characters");
            passwordField.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Register user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            RegisterModel registerModel = new RegisterModel(firstName, lastName, fullPhoneNumber, email, password);
                            reference.child(email.replace(".", ",")).setValue(registerModel);

                            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", getApplicationContext().MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            editor.putString("UserID", email.replace(".", ","));
                            editor.apply();

                            Toast.makeText(activity_register.this, "Registration successful", Toast.LENGTH_SHORT).show();

                            // Redirect to login activity after successful registration
                            Intent intent = new Intent(activity_register.this, activity_Login.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(activity_register.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
