package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class splashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Optional, depends on how you handle system windows
        setContentView(R.layout.activity_splash);

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
