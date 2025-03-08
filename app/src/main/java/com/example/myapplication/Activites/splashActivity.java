package com.example.myapplication.Activites;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.Services.ClipboardService;
import com.example.myapplication.R;

public class splashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        ImageView splashLogo = findViewById(R.id.splash_logo);
        Glide.with(this).load(R.drawable.splashscreen).into(splashLogo);

        new Handler().postDelayed(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
            boolean isCopyPasteEnabled = sharedPreferences.getBoolean("copyPasteEnabled", false);
            if (isCopyPasteEnabled) {
                Intent serviceIntent = new Intent(getApplicationContext(), ClipboardService.class);
                try {
                    startService(serviceIntent);
                    Toast.makeText(splashActivity.this, "Clipboard Service Started", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(splashActivity.this, "Failed to start Clipboard Service: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            Intent intent = new Intent(splashActivity.this, activity_Login.class);
            startActivity(intent);
            finish();
        }, 3000);
    }
}
