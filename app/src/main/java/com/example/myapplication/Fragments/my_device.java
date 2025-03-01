package com.example.myapplication.Fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.Adapters.myAdapter;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class my_device extends Fragment {

    RecyclerView recyclerView;
    TextView noFilesTextView;
    ShimmerFrameLayout shimmerFrameLayout;

    private ExecutorService executorService;
    private Handler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_device, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        noFilesTextView = view.findViewById(R.id.nofiles_textview);
        shimmerFrameLayout = view.findViewById(R.id.shmmerview);

        executorService = Executors.newSingleThreadExecutor();
        handler = new Handler();

        if (checkPermission()) {
            displayFiles();
        } else {
            requestPermission();
        }

        return view;
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Check if the app has the MANAGE_EXTERNAL_STORAGE permission on Android 11 and above
            return Environment.isExternalStorageManager();
        } else {
            int readPermission = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE);
            int writePermission = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return readPermission == PackageManager.PERMISSION_GRANTED && writePermission == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                // Direct the user to the settings page to grant MANAGE_EXTERNAL_STORAGE permission
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(android.net.Uri.parse("package:" + getActivity().getPackageName()));
                startActivityForResult(intent, 1);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 1);
            }
        } else {

            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        }
    }
    private void displayFiles() {

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                String path = Environment.getExternalStorageDirectory().getPath();
                File root = new File(path);
                File[] files = root.listFiles();

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        shimmerFrameLayout.setVisibility(View.GONE);
                        if (files == null || files.length == 0) {
                            noFilesTextView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            noFilesTextView.setVisibility(View.GONE);
                           recyclerView.setVisibility(View.VISIBLE);

                            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                            recyclerView.setAdapter(new myAdapter(getActivity(), files));
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                displayFiles();
            } else {
                Toast.makeText(getActivity(), "Permission denied. Cannot access storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    displayFiles();
                } else {
                    Toast.makeText(getActivity(), "Permission denied. Cannot access storage.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
