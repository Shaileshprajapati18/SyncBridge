package com.example.myapplication.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myapplication.Activites.notification_Activity;
import com.example.myapplication.Activites.open_screen;
import com.example.myapplication.Model.Base64ToImageConverter;
import com.example.myapplication.Model.DatabaseHelper;
import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class home extends Fragment {
    private DatabaseHelper databaseHelper;
    private TextView username;
    private ImageView deviceImg, deviceImage2;
    private DatabaseReference reference;
    private String currentUserEmail;
    private ValueEventListener valueEventListener;
    private CircleImageView profileImage;

    public home() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        username = view.findViewById(R.id.username);
        deviceImg = view.findViewById(R.id.deviceImage);
        deviceImage2 = view.findViewById(R.id.deviceImage2);
        ImageView sideArrow = view.findViewById(R.id.sidearrow);
        ImageView notification = view.findViewById(R.id.notification);
        profileImage = view.findViewById(R.id.profile);

        Glide.with(this).load(R.drawable.deviceimg).into(deviceImg);
        Glide.with(this).load(R.drawable.deviceimg).into(deviceImage2);
        Glide.with(this).load(R.drawable.botification).into(notification);
        Glide.with(this).load(R.drawable.sidearrow).into(sideArrow);

        databaseHelper = new DatabaseHelper(getContext());
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
        if (currentUserEmail == null) {
            Log.e("HomeFragment", "Current user email is null, user might not be logged in");
            Toast.makeText(getActivity(), "User not logged in", Toast.LENGTH_SHORT).show();
            username.setText("Guest");
            return view; // Exit early if no user is logged in
        }

        reference = FirebaseDatabase.getInstance().getReference("Users");

        setupFirebaseListener();
        setupNavigationButton(view);
        setupStorageInfo(view);
        loadLocalUserData();

        notification.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), notification_Activity.class);
            startActivity(intent);
        });
        return view;
    }

    private void setupFirebaseListener() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userFound = false;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userEmail = userSnapshot.child("email").getValue(String.class);
                    if (currentUserEmail != null && currentUserEmail.equals(userEmail)) {
                        userFound = true;
                        String firstname = userSnapshot.child("firstname").getValue(String.class);
                        String lastname = userSnapshot.child("lastname").getValue(String.class);
                        String phoneNumber = userSnapshot.child("phoneNumber").getValue(String.class);
                        String email = userSnapshot.child("email").getValue(String.class);
                        String imageBase64 = userSnapshot.child("profileImage").getValue(String.class);

                        String imageFilePath = null;
                        if (imageBase64 != null && getContext() != null) {
                            // Check local database first
                            Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(
                                    "SELECT " + DatabaseHelper.COLUMN_IMAGE + " FROM " + DatabaseHelper.TABLE_PRODUCTS +
                                            " WHERE " + DatabaseHelper.COLUMN_EMAIL + " = ?",
                                    new String[]{currentUserEmail});
                            if (cursor != null && cursor.moveToFirst()) {
                                imageFilePath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE));
                                if (imageFilePath != null && !imageFilePath.isEmpty()) {
                                    File imageFile = new File(imageFilePath);
                                    if (imageFile.exists()) {
                                        Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
                                        if (bitmap != null) {
                                            profileImage.setImageBitmap(bitmap);
                                            Log.d("HomeFragment", "Loaded local image from: " + imageFilePath);
                                        }
                                    }
                                }
                                cursor.close();
                            }

                            // If local file isn’t valid, convert Base64 from Firebase
                            if (imageFilePath == null || !new File(imageFilePath).exists()) {
                                File imageFile = Base64ToImageConverter.convertBase64ToImage(
                                        getContext(), imageBase64, "profile_" + System.currentTimeMillis() + ".jpg");
                                if (imageFile != null && imageFile.exists()) {
                                    imageFilePath = imageFile.getAbsolutePath();
                                    Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
                                    if (bitmap != null) {
                                        Log.d("Base64ToImageConverter", "Image converted successfully");
                                        profileImage.setImageBitmap(bitmap);
                                    } else {
                                        Log.e("Base64ToImageConverter", "Failed to convert base64 to image");
                                        profileImage.setImageResource(R.color.black);
                                    }
                                } else {
                                    Log.e("Base64ToImageConverter", "Failed to convert base64 to image");
                                    profileImage.setImageResource(R.color.black);
                                }
                            }
                        } else {
                            profileImage.setImageResource(R.color.black);
                        }

                        boolean success = databaseHelper.insertOrUpdateProduct(firstname, lastname, phoneNumber, email, imageFilePath);
                        if (!success) {
                            Log.e("DatabaseHelper", "Failed to insert or update product");
                        }
                        username.setText(firstname != null && lastname != null ? firstname + " " + lastname : "User");
                        Log.d("HomeFragment", "Firebase updated - Username: " + username.getText());
                        break; // Exit loop after finding the user
                    }
                }
                if (!userFound) {
                    Log.w("HomeFragment", "User not found in Firebase for email: " + currentUserEmail);
                    username.setText("User"); // Default instead of "User not found"
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Database error: " + error.getMessage());
                Toast.makeText(getActivity(), "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        };
        reference.addValueEventListener(valueEventListener);
    }

    private void setupNavigationButton(View view) {
        Button viewDetailButton = view.findViewById(R.id.view_detail);
        viewDetailButton.setOnClickListener(v -> {
            open_screen activity = (open_screen) getActivity();
            if (activity != null) {
                my_device myDeviceFragment = new my_device();
                if (!(activity.getSupportFragmentManager().findFragmentById(R.id.open_screen) instanceof my_device)) {
                    activity.switchFragment(myDeviceFragment);
                    BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
                    bottomNavigationView.setSelectedItemId(R.id.my_device);
                }
            }
        });
    }

    private void setupStorageInfo(View view) {
        TextView storageTextView = view.findViewById(R.id.storageTextView);
        ProgressBar storageProgressBar = view.findViewById(R.id.storageProgressBar);
        TextView progressText = view.findViewById(R.id.progress_text);

        long totalStorage = getTotalStorage();
        long availableStorage = getAvailableStorage();
        long usedStorage = totalStorage - availableStorage;

        String storageInfo = "Used: " + formatSize(usedStorage) + "\nTotal: " + formatSize(totalStorage);
        storageTextView.setText(storageInfo);

        int usedPercentage = (int) ((usedStorage * 100) / totalStorage);
        storageProgressBar.setProgress(usedPercentage);
        progressText.setText(usedPercentage + "%");
    }

    private void loadLocalUserData() {
        Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_PRODUCTS + " WHERE " + DatabaseHelper.COLUMN_EMAIL + " = ?",
                new String[]{currentUserEmail});
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIRST_NAME));
                    String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_NAME));
                    Log.d("HomeFragment", "Local data found - FirstName: " + firstName + ", LastName: " + lastName);
                    username.setText(firstName != null && lastName != null ? firstName + " " + lastName : "User");
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE));
                    if (imagePath != null && !imagePath.isEmpty()) {
                        File imageFile = new File(imagePath);
                        if (imageFile.exists()) {
                            try {
                                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                                if (bitmap != null) {
                                    profileImage.setImageBitmap(bitmap);
                                    profileImage.setVisibility(View.VISIBLE);
                                } else {
                                    Log.w("HomeFragment", "Failed to decode local image file: " + imagePath);
                                    profileImage.setImageResource(R.color.black);
                                }
                            } catch (Exception e) {
                                Log.e("HomeFragment", "Failed to load local profile image: " + e.getMessage());
                                Toast.makeText(getActivity(),
                                        "Failed to load profile image: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                profileImage.setImageResource(R.color.black);
                            }
                        } else {
                            Log.w("HomeFragment", "Local image file not found: " + imagePath);
                            profileImage.setImageResource(R.color.black);
                        }
                    } else {
                        profileImage.setImageResource(R.color.black);
                    }
                } else {
                    Log.w("HomeFragment", "No local data found for email: " + currentUserEmail);
                    username.setText("Loading..."); // Temporary placeholder
                    profileImage.setImageResource(R.color.black);
                }
            } catch (Exception e) {
                Log.e("HomeFragment", "Error loading local data: " + e.getMessage());
                username.setText("Loading...");
                profileImage.setImageResource(R.color.black);
            } finally {
                cursor.close();
            }
        } else {
            Log.e("HomeFragment", "Database cursor is null");
            username.setText("Loading...");
            profileImage.setImageResource(R.color.black);
        }
    }

    private long getTotalStorage() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getPath());
        return statFs.getBlockSizeLong() * statFs.getBlockCountLong();
    }

    private long getAvailableStorage() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getPath());
        return statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
    }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (reference != null && valueEventListener != null) {
            reference.removeEventListener(valueEventListener);
        }
    }
}