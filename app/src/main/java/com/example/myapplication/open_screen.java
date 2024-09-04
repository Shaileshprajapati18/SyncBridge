package com.example.myapplication;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class open_screen extends AppCompatActivity {

    BottomNavigationView bottom_navigation;
    home home=new home();
    my_device my_device=new my_device();
    other_device other_device=new other_device();
    settings settings=new settings();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_screen);

        bottom_navigation = findViewById(R.id.bottom_navigation);

        getSupportFragmentManager().beginTransaction().replace(R.id.open_screen, home).commit();

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId()==R.id.home_icon){
                    getSupportFragmentManager().beginTransaction().replace(R.id.open_screen,home).commit();
                }
                else if (item.getItemId()==R.id.my_device){
                    getSupportFragmentManager().beginTransaction().replace(R.id.open_screen,my_device).commit();
                }

                else if (item.getItemId()==R.id.other_device){
                    getSupportFragmentManager().beginTransaction().replace(R.id.open_screen,other_device).commit();
                }
                else if (item.getItemId()==R.id.settings){
                    getSupportFragmentManager().beginTransaction().replace(R.id.open_screen,settings).commit();
                }

                return false;
            }
        });
    }
}
