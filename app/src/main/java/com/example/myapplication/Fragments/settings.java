package com.example.myapplication.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.Activites.About_us;
import com.example.myapplication.Activites.activity_Login;
import com.example.myapplication.Model.DatabaseHelper;
import com.example.myapplication.R;
import com.example.myapplication.Activites.profile_settings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class settings extends Fragment {

    TextView view_details, Logout;
    TextView Username, Email;
    LinearLayout sendMessageTextView, about_us, faqs;
    FirebaseAuth mAuth;
    Switch copyPasteSwitch;
    DatabaseHelper databaseHelper;
    CircleImageView profileImage;
    private DatabaseReference reference;
    private String currentUserEmail;
    private ValueEventListener valueEventListener;

    public settings() {
        // Required empty public constructor
    }

    public static settings newInstance(String param1, String param2) {
        settings fragment = new settings();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Handle arguments if needed
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        databaseHelper = new DatabaseHelper(getContext());

        view_details = view.findViewById(R.id.full_details);
        about_us = view.findViewById(R.id.about_us);
        sendMessageTextView = view.findViewById(R.id.send_message_textview);
        faqs = view.findViewById(R.id.faqs);
        Logout = view.findViewById(R.id.Logout);
        copyPasteSwitch = view.findViewById(R.id.copyPasteSwitch);
        profileImage = view.findViewById(R.id.profile);

        ImageView upgradenow = view.findViewById(R.id.upgradenow);
        Glide.with(this).load(R.drawable.upgradenow).into(upgradenow);

        Username = view.findViewById(R.id.username);
        Email = view.findViewById(R.id.email);

        mAuth = FirebaseAuth.getInstance();
        currentUserEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : null;
        if (currentUserEmail == null) {
            Log.e("SettingsFragment", "Current user email is null, user might not be logged in");
            Toast.makeText(getActivity(), "User not logged in", Toast.LENGTH_SHORT).show();
            Username.setText("Guest");
            Email.setText("");
            return view; // Exit early if no user is logged in
        }

        reference = FirebaseDatabase.getInstance().getReference("Users");

        // Load local data immediately
        loadLocalUserData();

        // Setup Firebase listener for updates
        setupFirebaseListener();

        // Set OnClickListener for Logout
        Logout.setOnClickListener(v -> showExitConfirmationDialog());

        // Set OnClickListener for About Us
        about_us.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), About_us.class);
            startActivity(intent);
        });

        // Set OnClickListener for Send Message
        sendMessageTextView.setOnClickListener(v -> sendEmail());

        // Set OnClickListener for FAQs
        faqs.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.myapplication.Activites.faqs.class);
            startActivity(intent);
        });

        // Set OnClickListener for View Details (Profile Settings)
        if (view_details != null) {
            view_details.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), profile_settings.class);
                startActivity(intent);
            });
        } else {
            Log.e("SettingsFragment", "view_details is null");
        }

        copyPasteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        Toast.makeText(getContext(), "Enabled..", Toast.LENGTH_SHORT).show();
                    } else {
                        copyPasteSwitch.setChecked(false); // Disable the switch
                        Toast.makeText(getContext(), "Clipboard access is restricted in Android 10 and above.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
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
                                            Log.d("SettingsFragment", "Loaded local image from: " + imageFilePath);
                                        }
                                    }
                                }
                                cursor.close();
                            }

                            // If local file isn’t valid, convert Base64 from Firebase
                            if (imageFilePath == null || !new File(imageFilePath).exists()) {
                                File imageFile = convertBase64ToLocalFile(imageBase64);
                                if (imageFile != null && imageFile.exists()) {
                                    imageFilePath = imageFile.getAbsolutePath();
                                    Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
                                    if (bitmap != null) {
                                        Log.d("SettingsFragment", "Image converted successfully from Firebase");
                                        profileImage.setImageBitmap(bitmap);
                                    } else {
                                        Log.e("SettingsFragment", "Failed to load image from converted file");
                                        profileImage.setImageResource(R.color.black);
                                    }
                                } else {
                                    Log.e("SettingsFragment", "Failed to convert Base64 to image file");
                                    profileImage.setImageResource(R.color.black);
                                }
                            }
                        } else {
                            profileImage.setImageResource(R.color.black);
                        }

                        boolean success = databaseHelper.insertOrUpdateProduct(firstname, lastname, phoneNumber, email, imageFilePath);
                        if (!success) {
                            Log.e("SettingsFragment", "Failed to insert or update product");
                        }
                        // Always update UI with Firebase data
                        Username.setText(firstname != null && lastname != null ? firstname + " " + lastname : "User");
                        Email.setText(email != null ? email : currentUserEmail);
                        Log.d("SettingsFragment", "Firebase updated - Username: " + Username.getText() + ", Email: " + email);
                        break; // Exit loop after finding the user
                    }
                }
                if (!userFound) {
                    Log.w("SettingsFragment", "User not found in Firebase for email: " + currentUserEmail);
                    Username.setText("User"); // Default instead of "User not found"
                    Email.setText(currentUserEmail);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SettingsFragment", "Firebase database error: " + error.getMessage());
                Toast.makeText(getActivity(), "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        };
        reference.addValueEventListener(valueEventListener);
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
                    String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL));
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE));

                    Log.d("SettingsFragment", "Local data found - FirstName: " + firstName + ", LastName: " + lastName + ", Email: " + email);
                    Username.setText(firstName != null && lastName != null ? firstName + " " + lastName : "User");
                    Email.setText(email != null ? email : currentUserEmail);
                    if (imagePath != null && !imagePath.isEmpty()) {
                        File imageFile = new File(imagePath);
                        if (imageFile.exists()) {
                            try {
                                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                                if (bitmap != null) {
                                    profileImage.setImageBitmap(bitmap);
                                    profileImage.setVisibility(View.VISIBLE);
                                } else {
                                    Log.w("SettingsFragment", "Failed to decode local image file: " + imagePath);
                                    profileImage.setImageResource(R.color.black);
                                }
                            } catch (Exception e) {
                                Log.e("SettingsFragment", "Failed to load local profile image: " + e.getMessage());
                                Toast.makeText(getActivity(),
                                        "Failed to load profile image: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                profileImage.setImageResource(R.color.black);
                            }
                        } else {
                            Log.w("SettingsFragment", "Local image file not found: " + imagePath);
                            profileImage.setImageResource(R.color.black);
                        }
                    } else {
                        profileImage.setImageResource(R.color.black);
                    }
                } else {
                    Log.w("SettingsFragment", "No local data found for email: " + currentUserEmail);
                    Username.setText("Loading..."); // Temporary placeholder
                    Email.setText(currentUserEmail);
                    profileImage.setImageResource(R.color.black);
                }
            } catch (Exception e) {
                Log.e("SettingsFragment", "Error loading local data: " + e.getMessage());
                Username.setText("Loading...");
                Email.setText(currentUserEmail);
                profileImage.setImageResource(R.color.black);
            } finally {
                cursor.close();
            }
        } else {
            Log.e("SettingsFragment", "Database cursor is null");
            Username.setText("Loading...");
            Email.setText(currentUserEmail);
            profileImage.setImageResource(R.color.black);
        }
    }

    private File convertBase64ToLocalFile(String base64Image) {
        try {
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            if (bitmap == null) {
                Log.e("SettingsFragment", "Failed to decode Base64 to bitmap");
                return null;
            }
            File dir = new File(getContext().getFilesDir(), "profile_images");
            if (!dir.exists() && !dir.mkdirs()) {
                Log.e("SettingsFragment", "Failed to create profile_images directory");
                return null;
            }
            File imageFile = new File(dir, "profile_" + System.currentTimeMillis() + ".jpg");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            }
            return imageFile;
        } catch (Exception e) {
            Log.e("SettingsFragment", "Error converting Base64 to local file: " + e.getMessage());
            return null;
        }
    }

    private void sendEmail() {
        String[] recipients = new String[]{"shaileshprajapati182005@gmail.com"};
        String subject = "Message Subject";
        String message = "Hello, I would like to inquire about...";

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", recipients[0], null));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, recipients);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), "No email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showExitConfirmationDialog() {
        View customDialogView = getLayoutInflater().inflate(R.layout.dialog_logout, null);
        Button positiveButton = customDialogView.findViewById(R.id.dialog_positive);
        Button negativeButton = customDialogView.findViewById(R.id.dialog_negative);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(customDialogView)
                .setCancelable(false)
                .create();

        positiveButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), activity_Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (reference != null && valueEventListener != null) {
            reference.removeEventListener(valueEventListener);
        }
    }
}