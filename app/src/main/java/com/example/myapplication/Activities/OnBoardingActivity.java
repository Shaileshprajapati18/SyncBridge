package com.example.myapplication.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.myapplication.Adapters.SliderAdapter;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;

public class OnBoardingActivity extends AppCompatActivity {

    ViewPager viewPager;
    SliderAdapter sliderAdapter;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_on_boarding);

        viewPager = findViewById(R.id.slider);
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(OnBoardingActivity.this, activity_Login.class));
            finish();
            return;
        }

        sliderAdapter = new SliderAdapter(this);
        viewPager.setAdapter(sliderAdapter);

        sliderAdapter.setOnButtonClickListener(new SliderAdapter.OnButtonClickListener() {
            @Override
            public void onNextClick(int position) {
                if (position < sliderAdapter.getCount() - 1) {

                    viewPager.setCurrentItem(position + 1);
                } else {
                    startActivity(new Intent(OnBoardingActivity.this, activity_Login.class));
                    finish();
                }
            }

            @Override
            public void onSignInClick() {

                startActivity(new Intent(OnBoardingActivity.this, activity_Login.class));
                finish();
            }
        });
    }
}