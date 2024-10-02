package com.example.myapplication;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class CopyPasteManager {

    private boolean isRunning;
    private Handler handler;
    private Context context;

    public CopyPasteManager(Context context) {
        this.context = context;
        this.handler = new Handler(); // Handler to interact with the UI thread
    }

    public void startCopyPasteService() {
        if (!isRunning) {
            isRunning = true;
            // Start your service or logic here
            new Thread(new Runnable() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Thread started", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Simulate long-running task (monitoring clipboard, etc.)
                    while (isRunning) {
                        // Perform your long-running task here
                        try {
                            // Simulating work (you can replace this with actual logic)
                            Thread.sleep(1000); // Sleep for 1 second to simulate task running
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

    public void stopCopyPasteService() {
        if (isRunning) {
            isRunning = false;
            // Stop your service or logic here
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Thread stopped", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
