package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class other_device extends Fragment implements deviceAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private deviceAdapter adapter;
    private ArrayList<FileData> fileList = new ArrayList<>();
    private String serverUrl = ""; // Server URL fetched from Firebase
    private String currentPath = ""; // Track the current directory path

    public other_device() {
        // Required empty public constructor
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

        // Initialize RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new deviceAdapter(getContext(), fileList, this);
        recyclerView.setAdapter(adapter);

        // Fetch server URL from Firebase
        FirebaseDatabase.getInstance().getReference("ngrokUrl").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    serverUrl = task.getResult().getValue(String.class);
                    if (serverUrl != null && !serverUrl.isEmpty()) {
                        listDirectory(""); // Fetch the root directory after URL is loaded
                    } else {
                        Toast.makeText(getContext(), "Server URL is empty.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load server URL.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
                }
                Log.e("DirectoryLister", "Error", e);
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
                fileList.add(new FileData(dirName, true, 0));
            }

            // Parse files
            for (int i = 0; i < files.length(); i++) {
                JSONObject fileObj = files.getJSONObject(i);
                String fileName = fileObj.getString("name");
                long fileSize = fileObj.getLong("size");
                fileList.add(new FileData(fileName, false, fileSize));
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                });
            }

        } catch (JSONException e) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            }
            Log.e("DirectoryParser", "Error parsing JSON", e);
        }
    }

    @Override
    public void onItemClick(FileData file) {
        if (file.isDirectory()) {
            // Navigate into the selected directory
            currentPath = currentPath.isEmpty() ? file.getName() : currentPath + "/" + file.getName();
            listDirectory(currentPath);
        } else {
            // Open the selected file
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(serverUrl + "/download-file?file_path=" + currentPath + "/" + file.getName()), "application/octet-stream");
            startActivity(Intent.createChooser(intent, "Open file with"));
        }
    }
}
