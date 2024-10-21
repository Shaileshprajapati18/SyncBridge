package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class other_device extends Fragment implements deviceAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private deviceAdapter adapter;
    private ArrayList<FileData> fileList = new ArrayList<>();
    private String serverUrl = "";
    private String currentPath = "";
    private Stack<String> pathStack = new Stack<>();
    private TextView tvNoDevice;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private FileData fileToDownload;

    // SharedPreferences Keys
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String PREF_CURRENT_PATH = "currentPath";
    private static final String PREF_PATH_STACK = "pathStack";
    private static final int STORAGE_PERMISSION_CODE = 100;

    public other_device() {
        // Required empty constructor
    }

    public static other_device newInstance() {
        return new other_device();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other_device, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoDevice = view.findViewById(R.id.tvNoDevice);

        // Initialize RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new deviceAdapter(getContext(), fileList, this);
        recyclerView.setAdapter(adapter);

        // Restore state from SharedPreferences
        restoreState();

        // Fetch server URL from Firebase
        fetchServerUrl();

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the pathStack and currentPath
        outState.putSerializable("pathStack", (Serializable) pathStack);
        outState.putString("currentPath", currentPath);
        // Save state to SharedPreferences
        saveState();
    }

    private void saveState() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_CURRENT_PATH, currentPath);
        editor.putString(PREF_PATH_STACK, serializeStack(pathStack));
        editor.apply();
    }

    private void restoreState() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, getActivity().MODE_PRIVATE);
        currentPath = sharedPreferences.getString(PREF_CURRENT_PATH, "");
        String serializedStack = sharedPreferences.getString(PREF_PATH_STACK, "");
        if (!serializedStack.isEmpty()) {
            pathStack = deserializeStack(serializedStack);
        }
    }

    private String serializeStack(Stack<String> stack) {
        StringBuilder builder = new StringBuilder();
        for (String path : stack) {
            builder.append(path).append(",");
        }
        return builder.toString();
    }

    private Stack<String> deserializeStack(String serializedStack) {
        Stack<String> stack = new Stack<>();
        String[] paths = serializedStack.split(",");
        for (String path : paths) {
            if (!path.isEmpty()) {
                stack.push(path);
            }
        }
        return stack;
    }

    private void fetchServerUrl() {
        progressBar.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, getActivity().MODE_PRIVATE);
        String sessionID = sharedPreferences.getString("SessionID", "default_value");
        String userID = sharedPreferences.getString("UserID", "default_value");

        if (!sessionID.equals("default_value")) {
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(userID)
                    .child("Session")
                    .child(sessionID).child("PcUrl").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()) {
                                serverUrl = task.getResult().getValue(String.class);
                                if (serverUrl != null && !serverUrl.isEmpty()) {
                                    listDirectory(currentPath);  // List directory using current path
                                    progressBar.setVisibility(View.GONE);
                                    tvNoDevice.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                } else {
                                    showToast("Server URL is empty.");
                                    progressBar.setVisibility(View.GONE);
                                    tvNoDevice.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                }
                            } else {
                                showToast("Failed to load server URL.");
                                progressBar.setVisibility(View.GONE);
                                tvNoDevice.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                        }
                    });
        } else {
            progressBar.setVisibility(View.GONE);
            tvNoDevice.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void listDirectory(final String path) {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                URL url = new URL(serverUrl + "/list-directory?path=" + path);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                parseDirectoryResult(result.toString());
            } catch (Exception e) {
                Log.e("DirectoryLister", "Error", e);
                showErrorToastAndHideProgress("Error fetching directory: " + e.getLocalizedMessage());
            }
        }).start();
    }

    private void parseDirectoryResult(String jsonResult) {
        try {
            JSONObject result = new JSONObject(jsonResult);
            JSONArray directories = result.getJSONArray("directories");
            JSONArray files = result.getJSONArray("files");

            fileList.clear();

            // Parse directories
            for (int i = 0; i < directories.length(); i++) {
                String dirName = directories.getString(i);
                fileList.add(new FileData(dirName, serverUrl + "/path/to/dir/" + dirName, 0, true));
            }

            // Parse files
            for (int i = 0; i < files.length(); i++) {
                JSONObject fileObj = files.getJSONObject(i);
                String fileName = fileObj.getString("name");
                long fileSize = fileObj.getLong("size");
                fileList.add(new FileData(fileName, serverUrl + "/path/to/file/" + fileName, fileSize, false));
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                });
            }

        } catch (JSONException e) {
            Log.e("DirectoryParser", "Error parsing JSON", e);
            showErrorToastAndHideProgress("Error parsing directory result.");
        }
    }

    private void showErrorToastAndHideProgress(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            });
        }
    }

    private void showToast(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onItemClick(FileData file) {
        if (file.isDirectory()) {
            // Push current path to stack before navigating into the new directory
            pathStack.push(currentPath);
            currentPath = currentPath.isEmpty() ? file.getName() : currentPath + "/" + file.getName();
            listDirectory(currentPath);
        } else {
            // Open the selected file
            downloadFile(file);
        }
    }

    @Override
    public void onDownload(FileData file) {
        downloadFile(file);
    }

    @Override
    public void onBackPressed() {
        // Check if there is a previous directory in the stack
        if (!pathStack.isEmpty()) {
            currentPath = pathStack.pop(); // Go back to the previous directory
            listDirectory(currentPath);    // List the contents of the previous directory
        } else {
            // If no previous directory, navigate to the home fragment
            navigateToHomeFragment();
        }
    }

    public void downloadFile(FileData file) {
        fileToDownload = file;
        if (checkAndRequestPermissions()) {
            startFileDownload(file);
        } else {
            Toast.makeText(getContext(), "Permissions required for download.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // Request for notifications permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Request for storage access (No need for WRITE_EXTERNAL_STORAGE on Android 11+)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions((Activity) getContext(), permissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    private void startFileDownload(FileData file) {
        // Build the file URL
        String fileUrl = serverUrl + "/download-file?file_path=" + currentPath + "/" + file.getName();

        // Create DownloadManager request
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
        request.setTitle("Downloading " + file.getName());
        request.setDescription("Downloading file...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Allow downloads over WiFi and Mobile networks
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

        // Set the destination for the downloaded file
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.getName());

        // Get the system DownloadManager service
        DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);

        if (downloadManager != null) {
            // Enqueue the request
            downloadManager.enqueue(request);
            Toast.makeText(getContext(), "Downloading: " + file.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "DownloadManager not available.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            Map<String, Integer> permissionResults = new HashMap<>();
            for (int i = 0; i < permissions.length; i++) {
                permissionResults.put(permissions[i], grantResults[i]);
            }

            // Check if notification permission is granted for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (permissionResults.get(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getContext(), "Notification permission denied. Download notifications won't be shown.", Toast.LENGTH_SHORT).show();
                }
            }

            // Check if storage access is granted
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (permissionResults.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "Storage permission denied. Cannot download files.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Retry download if permissions are granted
            if (permissionResults.values().contains(PackageManager.PERMISSION_GRANTED) && fileToDownload != null) {
                startFileDownload(fileToDownload); // Retry file download
            }
        }
    }

    private void performFileDownload(FileData file) {
        // Build the file URL
        String fileUrl = serverUrl + "/download-file?file_path=" + currentPath + "/" + file.getName();

        // Create DownloadManager request
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
        request.setTitle("Downloading " + file.getName());
        request.setDescription("Downloading file...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Allow downloads over WiFi and Mobile networks
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

        // Set the destination for the downloaded file
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.getName());

        // Get the system DownloadManager service
        DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);

        if (downloadManager != null) {
            // Enqueue the request
            downloadManager.enqueue(request);
            Toast.makeText(getContext(), "Downloading: " + file.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "DownloadManager not available.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToHomeFragment() {
        Fragment homeFragment = new home(); // Create an instance of your home fragment
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.open_screen, homeFragment) // Replace with the actual container id
                .addToBackStack(null)
                .commit();
    }

    void handleBackPress() {
        onBackPressed();  // Call the method that handles back press logic
    }

    @Override
    public void onDetach() {
        super.onDetach();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Remove specific keys
        editor.remove(PREF_CURRENT_PATH);
        editor.remove(PREF_PATH_STACK);

        editor.apply();

        pathStack.clear();
    }
}
