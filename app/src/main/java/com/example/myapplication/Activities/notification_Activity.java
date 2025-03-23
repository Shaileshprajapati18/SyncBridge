package com.example.myapplication.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.myapplication.Adapters.NotificationAdapter;
import com.example.myapplication.Model.Notification;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class notification_Activity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LottieAnimationView animationView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        back = findViewById(R.id.back);
        recyclerView = findViewById(R.id.recycler_view_notifications);
        animationView = findViewById(R.id.animationView);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList, this); // Pass activity reference
        recyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        notificationList.add(new Notification("New Device Connected", "(HP-2343) connected successfully.", "2 hours ago"));
        notificationList.add(new Notification("App Update", "A new version of the app is available.", "1 day ago"));
        updateUI();
    }

    // New method to update UI based on list size
    public void updateUI() {
        if (notificationList.isEmpty()) {
            animationView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            animationView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }
}