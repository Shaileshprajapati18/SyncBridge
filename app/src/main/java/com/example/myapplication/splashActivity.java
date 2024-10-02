package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class splashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Optional, depends on how you handle system windows
        setContentView(R.layout.activity_splash);

        // Delay for 3 seconds (3000 milliseconds) before navigating to the next activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Navigate to the next activity, e.g., the home screen or login activity
                Intent intent = new Intent(splashActivity.this, activity_Login.class);  // Replace with the appropriate activity
                startActivity(intent);

                // Finish the current activity so the user can't go back to the splash screen
                finish();
            }
        }, 3000);
    }
}
