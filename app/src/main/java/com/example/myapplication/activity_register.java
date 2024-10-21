package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Model.RegisterModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

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

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize UI elements
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        firstNameField = findViewById(R.id.firstname);
        lastNameField = findViewById(R.id.last_name);
        phoneField = findViewById(R.id.phone);
        registerButton = findViewById(R.id.registerButton);
        loginRedirect = findViewById(R.id.loginRedirect);
        countryCodePicker = findViewById(R.id.countryCodePicker);
        progressBar=findViewById(R.id.progressbar);

        // Set click listener for the register button
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
