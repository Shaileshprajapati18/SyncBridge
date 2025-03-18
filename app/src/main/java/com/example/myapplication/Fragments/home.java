package com.example.myapplication.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.myapplication.Activities.notification_Activity;
import com.example.myapplication.Activities.open_screen;
import com.example.myapplication.Activities.storageViewer;
import com.example.myapplication.Model.Base64ToImageConverter;
import com.example.myapplication.Model.DatabaseHelper;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class home extends Fragment {
    private DatabaseHelper databaseHelper;
    private TextView username;
    private DatabaseReference reference;
    private String currentUserEmail;
    private ValueEventListener valueEventListener;
    private CircleImageView profileImage;
    private View fragmentView;

    public home() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        fragmentView = view;

        username = view.findViewById(R.id.username);
        ImageView notification = view.findViewById(R.id.notification);
        profileImage = view.findViewById(R.id.profile);

        databaseHelper = new DatabaseHelper(getContext());
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
        if (currentUserEmail == null) {
            Log.e("HomeFragment", "Current user email is null, user might not be logged in");
            Toast.makeText(getActivity(), "User not logged in", Toast.LENGTH_SHORT).show();
            username.setText("Guest");
            return view;
        }

        reference = FirebaseDatabase.getInstance().getReference("Users");

        setupFirebaseListener();
        setupNavigationButton(view);
        setupStorageInfo(view);
        loadLocalUserData();
        setupCardViewListeners(view);
        new CategorySizeLoader().execute();

        notification.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), notification_Activity.class);
            startActivity(intent);
        });

        return view;
    }

    private void setupCardViewListeners(View view) {
        CardView downloadsCard = view.findViewById(R.id.card_downloads);
        CardView documentsCard = view.findViewById(R.id.card_documents);
        CardView videosCard = view.findViewById(R.id.card_videos);
        CardView imagesCard = view.findViewById(R.id.card_images);
        CardView audioCard = view.findViewById(R.id.card_audio);
        CardView appsCard = view.findViewById(R.id.card_apps);

        File rootDir = Environment.getExternalStorageDirectory();

        downloadsCard.setOnClickListener(v -> openStorageViewer(rootDir, "downloads"));
        documentsCard.setOnClickListener(v -> openStorageViewer(rootDir, "documents"));
        videosCard.setOnClickListener(v -> openStorageViewer(rootDir, "videos"));
        imagesCard.setOnClickListener(v -> openStorageViewer(rootDir, "images"));
        audioCard.setOnClickListener(v -> openStorageViewer(rootDir, "audio"));
        appsCard.setOnClickListener(v -> openStorageViewer(rootDir, "apps"));
    }

    private void openStorageViewer(File directory, String fileType) {
        Intent intent = new Intent(getActivity(), storageViewer.class);
        intent.putExtra("path", directory.getAbsolutePath());
        intent.putExtra("fileType", fileType);
        startActivity(intent);
    }

    private class CategorySizeLoader extends AsyncTask<Void, Void, Map<String, Long>> {
        @Override
        protected void onPreExecute() {
            if (fragmentView != null) {
                ((TextView) fragmentView.findViewById(R.id.downloads_size)).setText("...");
                ((TextView) fragmentView.findViewById(R.id.documents_size)).setText("...");
                ((TextView) fragmentView.findViewById(R.id.videos_size)).setText("...");
                ((TextView) fragmentView.findViewById(R.id.images_size)).setText("...");
                ((TextView) fragmentView.findViewById(R.id.audio_size)).setText("...");
                ((TextView) fragmentView.findViewById(R.id.apps_size)).setText("...");
            }
        }

        @Override
        protected Map<String, Long> doInBackground(Void... voids) {
            File rootDir = Environment.getExternalStorageDirectory();

            String[] audioExtensions = {".mp3", ".wav", ".aac", ".m4a"};
            String[] videoExtensions = {".mp4", ".mkv", ".avi", ".3gp"};
            String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif"};
            String[] documentExtensions = {".pdf", ".doc", ".docx", ".txt", ".xls", ".xlsx", ".ppt", ".pptx"};
            String[] appExtensions = {".apk"};

            Map<String, Long> sizes = new HashMap<>();
            sizes.put("downloads", calculateDownloadsSize());
            sizes.put("documents", calculateCategorySize(rootDir, documentExtensions));
            sizes.put("videos", calculateCategorySize(rootDir, videoExtensions));
            sizes.put("images", calculateCategorySize(rootDir, imageExtensions));
            sizes.put("audio", calculateCategorySize(rootDir, audioExtensions));
            sizes.put("apps", calculateCategorySize(rootDir, appExtensions));

            return sizes;
        }

        @Override
        protected void onPostExecute(Map<String, Long> sizes) {
            if (fragmentView != null && getActivity() != null && !getActivity().isFinishing()) {
                ((TextView) fragmentView.findViewById(R.id.downloads_size)).setText(formatSize(sizes.get("downloads")));
                ((TextView) fragmentView.findViewById(R.id.documents_size)).setText(formatSize(sizes.get("documents")));
                ((TextView) fragmentView.findViewById(R.id.videos_size)).setText(formatSize(sizes.get("videos")));
                ((TextView) fragmentView.findViewById(R.id.images_size)).setText(formatSize(sizes.get("images")));
                ((TextView) fragmentView.findViewById(R.id.audio_size)).setText(formatSize(sizes.get("audio")));
                ((TextView) fragmentView.findViewById(R.id.apps_size)).setText(formatSize(sizes.get("apps")));
            }
        }
    }

    private long calculateDownloadsSize() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return calculateDirectorySize(downloadsDir);
    }

    private long calculateCategorySize(File directory, String[] extensions) {
        long totalSize = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    totalSize += calculateCategorySize(file, extensions);
                } else {
                    String fileName = file.getName().toLowerCase();
                    for (String ext : extensions) {
                        if (fileName.endsWith(ext)) {
                            totalSize += file.length();
                            break;
                        }
                    }
                }
            }
        }
        return totalSize;
    }

    private long calculateDirectorySize(File directory) {
        long size = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    size += calculateDirectorySize(file);
                } else {
                    size += file.length();
                }
            }
        }
        return size;
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
                        break;
                    }
                }
                if (!userFound) {
                    Log.w("HomeFragment", "User not found in Firebase for email: " + currentUserEmail);
                    username.setText("User");
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
                // Use the existing my_device fragment from the activity
                activity.switchToMyDeviceFragment();
            }
        });
    }

    private void setupStorageInfo(View view) {
        TextView storageTextView = view.findViewById(R.id.storageTextView);
        TextView storageTextViewUsed = view.findViewById(R.id.storageTextViewUsed);
        ProgressBar storageProgressBar = view.findViewById(R.id.storageProgressBar);
        TextView progressText = view.findViewById(R.id.progress_text);

        long totalStorage = getTotalStorage();
        long availableStorage = getAvailableStorage();
        long usedStorage = totalStorage - availableStorage;

        String storageInfo = "Used " + "of " + formatSize(totalStorage);
        String storageInfoUsed = formatSize(usedStorage);

        storageTextView.setText(storageInfoUsed);
        storageTextViewUsed.setText(storageInfo);

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
                    username.setText("...");
                    profileImage.setImageResource(R.color.black);
                }
            } catch (Exception e) {
                Log.e("HomeFragment", "Error loading local data: " + e.getMessage());
                username.setText("...");
                profileImage.setImageResource(R.color.black);
            } finally {
                cursor.close();
            }
        } else {
            Log.e("HomeFragment", "Database cursor is null");
            username.setText("...");
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (reference != null && valueEventListener != null) {
            reference.removeEventListener(valueEventListener);
        }
        fragmentView = null;
    }
}