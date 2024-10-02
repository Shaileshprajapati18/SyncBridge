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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        // Get the current user's email from Firebase Authentication
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Firebase reference
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        // TextView for username display
        TextView username = view.findViewById(R.id.username);

        // Fetch user details from Firebase
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userFound = false;

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userEmail = userSnapshot.child("email").getValue(String.class);
                    // Match current user email with user email in database
                    if (currentUserEmail != null && currentUserEmail.equals(userEmail)) {
                        userFound = true;

                        String firstname = userSnapshot.child("firstname").getValue(String.class);
                        String lastname = userSnapshot.child("lastname").getValue(String.class);

                        // Display user's first and last name
                        username.setText(firstname + " " + lastname);
                        break; // Exit loop once we find the user
                    }
                }

                if (!userFound) {
                    username.setText("User not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Data not retrieved", Toast.LENGTH_SHORT).show();
            }
        });

        // Button to navigate to my_device fragment
        Button viewDetailButton = view.findViewById(R.id.view_detail);
        viewDetailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment myDeviceFragment = new my_device();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                // Check if the fragment is already added
                if (getFragmentManager().findFragmentById(R.id.open_screen) instanceof my_device) {
                    return; // Fragment is already added; avoid adding it again
                }

                transaction.replace(R.id.open_screen, myDeviceFragment);
                transaction.addToBackStack(null);
                transaction.commit();

                // Update bottom navigation view
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
                bottomNavigationView.getMenu().findItem(R.id.my_device).setChecked(true);
            }
        });

        // Fetch and display storage information
        TextView storageTextView = view.findViewById(R.id.storageTextView);
        ProgressBar storageProgressBar = view.findViewById(R.id.storageProgressBar);
        TextView progressText = view.findViewById(R.id.progress_text);

        long totalStorage = getTotalStorage();
        long availableStorage = getAvailableStorage();
        long usedStorage = totalStorage - availableStorage;

        String storageInfo =  " Used "+": "+formatSize(usedStorage) +"\n" +""+" Total "+": " +formatSize(totalStorage) ;
        storageTextView.setText(storageInfo);

        int usedPercentage = (int) ((usedStorage * 100) / totalStorage);
        storageProgressBar.setProgress(usedPercentage);
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
