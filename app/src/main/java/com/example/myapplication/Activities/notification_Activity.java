package com.example.myapplication.Activities;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;

public class notification_Activity extends AppCompatActivity {

    ImageView no_notification,back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        no_notification = findViewById(R.id.no_notification);
        Glide.with(this).load(R.drawable.botification).into(no_notification);
        back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            finish();
        });
    }
}