package com.example.myapplication.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.myapplication.Services.ClipboardService;
import com.example.myapplication.R;

public class splashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        LottieAnimationView animationView = findViewById(R.id.animationView);
        animationView.setFailureListener(throwable -> {
            Toast.makeText(this, "Animation failed to load: " + throwable.getMessage(), Toast.LENGTH_LONG).show();

            proceedToNextActivity();
        });

        new Handler().postDelayed(this::proceedToNextActivity, 1500);
    }

    private void proceedToNextActivity() {
        LottieAnimationView animationView = findViewById(R.id.animationView);
        animationView.cancelAnimation();
        animationView.pauseAnimation();

        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isCopyPasteEnabled = sharedPreferences.getBoolean("copyPasteEnabled", false);
        if (isCopyPasteEnabled) {
            Intent serviceIntent = new Intent(getApplicationContext(), ClipboardService.class);
            try {
                startService(serviceIntent);
                Toast.makeText(this, "Clipboard Service Started", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Failed to start Clipboard Service: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        Intent intent = new Intent(this, OnBoardingActivity.class);
        startActivity(intent);
        finish();
    }
}