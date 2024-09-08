package com.example.myapplication;

import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

public class home extends Fragment {

    public home() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Find the button by its ID
        Button viewDetailButton = view.findViewById(R.id.view_detail);

        // Set an OnClickListener on the button
        viewDetailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Fragment myDeviceFragment = new my_device();

                // Replace the current fragment with my_device fragment
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.open_screen, myDeviceFragment);
                transaction.addToBackStack(null);
                transaction.commit();

                // Change the icon color in BottomNavigationView
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
                bottomNavigationView.getMenu().findItem(R.id.my_device).setChecked(true);
            }
        });

        // Fetch and display the storage information
        TextView storageTextView = view.findViewById(R.id.storageTextView);
        ProgressBar storageProgressBar = view.findViewById(R.id.storageProgressBar);
        TextView progressText = view.findViewById(R.id.progress_text);

        // Get total storage including system files, apps, and user data
        long totalStorage = getTotalStorage();
        long availableStorage = getAvailableStorage();
        long usedStorage = totalStorage - availableStorage;

        // Display used and total storage in the format "130 GB / 150 GB used"
        String storageInfo = formatSize(usedStorage) + " used / " + formatSize(totalStorage) + " total";
        storageTextView.setText(storageInfo);

        // Calculate the percentage of used storage
        int usedPercentage = (int) ((usedStorage * 100) / totalStorage);

        // Update the progress of the ProgressBar
        storageProgressBar.setProgress(usedPercentage);

        // Display the used storage percentage in the TextView
        progressText.setText(usedPercentage + "%");

        return view;
    }

    // Method to get total storage (system + user)
    private long getTotalStorage() {
        File path = Environment.getDataDirectory(); // This gets both system and internal storage
        StatFs statFs = new StatFs(path.getPath());
        long blockSize = statFs.getBlockSizeLong();
        long totalBlocks = statFs.getBlockCountLong();
        return totalBlocks * blockSize;
    }

    // Method to get available storage
    private long getAvailableStorage() {
        File path = Environment.getDataDirectory(); // This gets both system and internal storage
        StatFs statFs = new StatFs(path.getPath());
        long blockSize = statFs.getBlockSizeLong();
        long availableBlocks = statFs.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    // Method to format the size in human-readable form (e.g., MB, GB)
    private String formatSize(long size) {
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double sizeInUnits = size;

        while (sizeInUnits >= 1024 && unitIndex < units.length - 1) {
            sizeInUnits /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", sizeInUnits, units[unitIndex]);
    }
}
