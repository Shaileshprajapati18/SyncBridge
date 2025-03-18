package com.example.myapplication.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Model.DatabaseHelper;
import com.example.myapplication.Model.RegisterModel;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class profile_settings extends AppCompatActivity {

    private TextView Firstname, Lastname, Phonenumber, Email,saveUpdate;
    private ImageView arrow_back;
    private CircleImageView profile;
    private static final int PICK_IMAGE = 1;
    private Bitmap selectedBitmap;
    private DatabaseReference reference;
    private ProgressDialog progressDialog;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String sanitizedEmail = currentUserEmail.replace(".", ","); // Replace . with , for Firebase
        reference = FirebaseDatabase.getInstance().getReference("Users").child(sanitizedEmail);

        databaseHelper = new DatabaseHelper(this);

        initializeViews();

        saveUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImageToFirebase(selectedBitmap);
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);

        loadLocalData();
        setupListeners();
        loadUserData();
    }

    private void initializeViews() {
        arrow_back = findViewById(R.id.arrow_back);
        profile = findViewById(R.id.profile);
        Firstname = findViewById(R.id.firstname);
        Lastname = findViewById(R.id.lastname);
        Phonenumber = findViewById(R.id.phonenumber);
        Email = findViewById(R.id.email);
        saveUpdate = findViewById(R.id.saveUpdate);
    }

    private void setupListeners() {
        profile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        arrow_back.setOnClickListener(v -> onBackPressed());
    }

    private void loadLocalData() {
        Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_PRODUCTS + " WHERE " + DatabaseHelper.COLUMN_EMAIL + " = ?",
                new String[]{FirebaseAuth.getInstance().getCurrentUser().getEmail()});
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIRST_NAME));
                    String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_NAME));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL));
                    String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE));
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE));

                    Firstname.setText(firstName);
                    Lastname.setText(lastName);
                    Phonenumber.setText(phoneNumber);
                    Email.setText(email);

                    if (imagePath != null && !imagePath.isEmpty()) {
                        File imageFile = new File(imagePath);
                        if (imageFile.exists()) {
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                            if (bitmap != null) {
                                profile.setImageBitmap(bitmap);
                            }
                        }
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }

    private void loadUserData() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    RegisterModel user = snapshot.getValue(RegisterModel.class);
                    if (user != null) {
                        Firstname.setText(user.getFirstname() != null ? user.getFirstname() : "firstname");
                        Lastname.setText(user.getLastname() != null ? user.getLastname() : "lastname");
                        Phonenumber.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "phoneNumber");
                        Email.setText(user.getEmail() != null ? user.getEmail() : "email");

                        String imageBase64 = user.getProfileImage();
                        if (imageBase64 != null && !imageBase64.isEmpty()) {
                            String imagePath = convertBase64ToLocalFile(imageBase64);
                            if (imagePath != null) {
                                File imageFile = new File(imagePath);
                                if (imageFile.exists()) {
                                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                                    if (bitmap != null) {
                                        profile.setImageBitmap(bitmap);
                                    }
                                }
                            }
                            databaseHelper.insertOrUpdateProduct(
                                    user.getFirstname(), user.getLastname(), user.getPhoneNumber(), user.getEmail(), imagePath);
                        } else {
                            profile.setImageResource(R.color.black);
                        }
                    }
                } else {
                    Firstname.setText("firstname");
                    Lastname.setText("lastname");
                    Phonenumber.setText("phoneNumber");
                    Email.setText("email");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(profile_settings.this,
                        "Failed to load data: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profile.setImageBitmap(selectedBitmap);
            } catch (IOException e) {
                Log.e("ProfileActivity", "Error loading image", e);
            }
        }
    }

    private void uploadImageToFirebase(Bitmap bitmap) {
        if (bitmap == null) {
            progressDialog.dismiss();
            return;
        }

        progressDialog.show();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        reference.child("profileImage").setValue(encodedImage)
                .addOnSuccessListener(aVoid -> {

                    String imagePath = convertBase64ToLocalFile(encodedImage);
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    boolean success = databaseHelper.insertOrUpdateProduct(
                            Firstname.getText().toString(),
                            Lastname.getText().toString(),
                            Phonenumber.getText().toString(),
                            email,
                            imagePath);
                    progressDialog.dismiss();
                    if (success) {
                        Log.d("ProfileActivity", "Image uploaded and path stored successfully");
                    } else {
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Error uploading image", e);
                    progressDialog.dismiss();
                });
    }

    private String convertBase64ToLocalFile(String base64Image) {
        try {
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            File dir = new File(getFilesDir(), "profile_images");
            if (!dir.exists()) dir.mkdirs();
            File imageFile = new File(dir, "profile_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();
            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}