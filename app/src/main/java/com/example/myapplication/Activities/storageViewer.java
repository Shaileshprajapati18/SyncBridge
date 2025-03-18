package com.example.myapplication.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.myapplication.Adapters.myAdapter;
import com.example.myapplication.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class storageViewer extends AppCompatActivity {

    RecyclerView recyclerView;
    LottieAnimationView noFilesTextView;
    TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_viewer);

        // Initialize UI components
        recyclerView = findViewById(R.id.recycler_view);
        noFilesTextView = findViewById(R.id.nofiles_textview);
        toolbarTitle = findViewById(R.id.toolbar_title);

        // Get path and fileType from Intent
        String path = getIntent().getStringExtra("path");
        String fileType = getIntent().getStringExtra("fileType");

        // Default to external storage root if path is null
        File root = (path != null) ? new File(path) : Environment.getExternalStorageDirectory();
        File[] filteredFiles;

        // Handle null or invalid fileType
        if (fileType == null || fileType.isEmpty()) {
            filteredFiles = root.listFiles(); // Show all files if no type specified
            if (filteredFiles == null) filteredFiles = new File[0];
        } else {
            filteredFiles = filterFiles(root, fileType);
        }

        // Set up RecyclerView and UI
        setupRecyclerView(filteredFiles, fileType);
    }

    // Filter files based on type (audio, videos, images, etc.)
    private File[] filterFiles(File root, String fileType) {
        List<File> filteredList = new ArrayList<>();
        File[] allFiles;

        if ("downloads".equalsIgnoreCase(fileType)) {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            allFiles = downloadsDir.listFiles();
            if (allFiles != null) {
                filteredList.addAll(Arrays.asList(allFiles));
            }
        } else {
            allFiles = root.listFiles();
            if (allFiles == null) return new File[0];

            String[] audioExtensions = {".mp3", ".wav", ".aac", ".m4a"};
            String[] videoExtensions = {".mp4", ".mkv", ".avi", ".3gp"};
            String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif"};
            String[] documentExtensions = {".pdf", ".doc", ".docx", ".txt", ".xls", ".xlsx", ".ppt", ".pptx"};
            String[] appExtensions = {".apk"};

            for (File file : allFiles) {
                if (file.isDirectory()) {
                    File[] subFiles = filterFiles(file, fileType); // Recursive call for directories
                    filteredList.addAll(Arrays.asList(subFiles));
                } else {
                    String fileName = file.getName().toLowerCase();
                    switch (fileType.toLowerCase()) {
                        case "audio":
                            for (String ext : audioExtensions) {
                                if (fileName.endsWith(ext)) {
                                    filteredList.add(file);
                                    break;
                                }
                            }
                            break;
                        case "videos":
                            for (String ext : videoExtensions) {
                                if (fileName.endsWith(ext)) {
                                    filteredList.add(file);
                                    break;
                                }
                            }
                            break;
                        case "images":
                            for (String ext : imageExtensions) {
                                if (fileName.endsWith(ext)) {
                                    filteredList.add(file);
                                    break;
                                }
                            }
                            break;
                        case "documents":
                            for (String ext : documentExtensions) {
                                if (fileName.endsWith(ext)) {
                                    filteredList.add(file);
                                    break;
                                }
                            }
                            break;
                        case "apps":
                            for (String ext : appExtensions) {
                                if (fileName.endsWith(ext)) {
                                    filteredList.add(file);
                                    break;
                                }
                            }
                            break;
                        default:
                            filteredList.add(file); // Fallback: add all files if type is unknown
                            break;
                    }
                }
            }
        }

        return filteredList.toArray(new File[0]);
    }

    // Set up RecyclerView and handle Lottie animation visibility
    private void setupRecyclerView(File[] files, String fileType) {
        if (files == null || files.length == 0) {
            noFilesTextView.setVisibility(View.VISIBLE);
            noFilesTextView.playAnimation(); // Play Lottie animation when no files are found
            recyclerView.setVisibility(View.GONE);
        } else {
            noFilesTextView.setVisibility(View.GONE);
            noFilesTextView.cancelAnimation(); // Stop animation when files are present
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new myAdapter(this, files)); // Use custom adapter

            // Set toolbar title based on fileType
            if (toolbarTitle != null) {
                String title = (fileType != null && !fileType.isEmpty()) ?
                        fileType.substring(0, 1).toUpperCase() + fileType.substring(1) : "My Files";
                toolbarTitle.setText(title);
            }
        }
    }
}