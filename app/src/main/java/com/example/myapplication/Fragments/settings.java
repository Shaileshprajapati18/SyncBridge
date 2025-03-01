package com.example.myapplication.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
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

public class settings extends Fragment {

    TextView view_details, Logout;
    TextView Username, Email;
    LinearLayout sendMessageTextView, about_us, faqs;
    FirebaseAuth mAuth;
    Switch copyPasteSwitch;
    DatabaseHelper databaseHelper;

    public settings() {
    }

    public static settings newInstance(String param1, String param2) {
        settings fragment = new settings();
        Bundle args = new Bundle();
        // Add parameters to the args Bundle if needed
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        databaseHelper = new DatabaseHelper(getContext());

        view_details = view.findViewById(R.id.full_details);
        about_us = view.findViewById(R.id.about_us);
        sendMessageTextView = view.findViewById(R.id.send_message_textview);
        faqs = view.findViewById(R.id.faqs);
        Logout = view.findViewById(R.id.Logout);
        copyPasteSwitch = view.findViewById(R.id.copyPasteSwitch); // Initialize the switch

        ImageView upgradenow = view.findViewById(R.id.upgradenow);
        Glide.with(this).load(R.drawable.upgradenow).into(upgradenow);

        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        Username = view.findViewById(R.id.username);
        Email = view.findViewById(R.id.email);

        Cursor cursor = databaseHelper.viewData();
        if (cursor.moveToFirst()) {
            do {

                String firstName = cursor.getString(cursor.getColumnIndexOrThrow("first_name"));
                String lastName = cursor.getString(cursor.getColumnIndexOrThrow("last_name"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));

                Username.setText(firstName + " " + lastName);
                Email.setText(email);

            }
            while (cursor.moveToNext());
            cursor.close();
        }
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean userFound = false;

                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String userEmail = null;
                        try {
                            userEmail = userSnapshot.child("email").getValue(String.class);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        if (currentUserEmail != null && currentUserEmail.equals(userEmail)) {
                            userFound = true;

                            String firstname = userSnapshot.child("firstname").getValue(String.class);
                            String lastname = userSnapshot.child("lastname").getValue(String.class);
                            String email = userSnapshot.child("email").getValue(String.class);

                            Username.setText(firstname + " " + lastname);
                            Email.setText(email);
                            break;
                        }
                    }

                    if (!userFound) {
                        Username.setText("username");
                        Email.setText("email");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Data not retrieved", Toast.LENGTH_SHORT).show();
                }
            });

            // Initialize Firebase Authentication
            mAuth = FirebaseAuth.getInstance();

            copyPasteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            Toast.makeText(getContext(), "Enabled..", Toast.LENGTH_SHORT).show();
                        } else {
                            // Show toast if the Android version is 10 or higher
                            copyPasteSwitch.setChecked(false); // Disable the switch
                            Toast.makeText(getContext(), "Clipboard access is restricted in Android 10 and above.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });


            // Set OnClickListener for Logout
            Logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showExitConfirmationDialog(); // Show logout confirmation dialog
                }
            });

            // Set OnClickListener for About Us
            about_us.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), About_us.class);
                    startActivity(intent);
                }
            });

            // Set OnClickListener for Send Message
            sendMessageTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendEmail();
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
            });

            // Set OnClickListener for FAQs
            faqs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), com.example.myapplication.Activites.faqs.class);
                    startActivity(intent);
                }
            });

            // Set OnClickListener for View Details (Profile Settings)
            if (view_details != null) {
                view_details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), profile_settings.class);
                        startActivity(intent); // Start the new activity
                    }
                });
            } else {
                Log.e("SettingsFragment", "TextView view_details not found in the layout");
            }

            return view;
        }
    // Show logout confirmation dialog
    private void showExitConfirmationDialog() {
        // Inflate custom layout for dialog
        View customDialogView = getLayoutInflater().inflate(R.layout.dialog_logout, null);

        // Initialize buttons
        Button positiveButton = customDialogView.findViewById(R.id.dialog_positive);
        Button negativeButton = customDialogView.findViewById(R.id.dialog_negative);

        // Create and show dialog
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(customDialogView)  // Set the custom layout as the dialog view
                .setCancelable(false)       // Make the dialog non-cancelable by touching outside
                .create();

        // Set OnClickListener for positive button (Logout)
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear activity stack and navigate to Login activity
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), activity_Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        // Set OnClickListener for negative button (Cancel)
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();  // Close dialog
            }
        });

        dialog.show();
    }
}
