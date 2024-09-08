package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class full_details extends AppCompatActivity {

    TextView password;
    ImageView visiblity,arrow_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_full_details);

        password = findViewById(R.id.password);
        visiblity = findViewById(R.id.visibility);
        arrow_back=findViewById(R.id.arrow_back);

       arrow_back.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               onBackPressed();
           }
       });

        visiblity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password.getTransformationMethod() == null) {
                    password.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
                    visiblity.setImageResource(R.drawable.visibility);
                } else {
                    password.setTransformationMethod(null);
                    visiblity.setImageResource(R.drawable.visibility_off);
                }
            }
        });

    }
}