package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class settings extends Fragment {

    // Declare the TextView
    TextView view_details,Logout;
    LinearLayout sendMessageTextView,about_us,faqs;
    FirebaseAuth mAuth;

    public settings() {
        // Required empty public constructor
    }

    public static settings newInstance(String param1, String param2) {
        settings fragment = new settings();
        Bundle args = new Bundle();
        // You can add parameters to the args Bundle if needed
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            // Retrieve any arguments passed to the fragment, if needed
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        view_details = view.findViewById(R.id.full_details);
        about_us = view.findViewById(R.id.about_us);
        sendMessageTextView = view.findViewById(R.id.send_message_textview);
        faqs = view.findViewById(R.id.faqs);
        Logout=view.findViewById(R.id.Logout);

        mAuth=FirebaseAuth.getInstance();

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                showExitConfirmationDialog();
            }
        });
        about_us.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),About_us.class);
                startActivity(intent);
            }
        });
        sendMessageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }

            private void sendEmail() {
                String[] recipients = new String[]{"purohitvinayak48@gmail.com"};  // Replace with your email
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

        faqs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),faqs.class);
                startActivity(intent);
            }
        });

        if (view_details != null) {
            // Set an OnClickListener to the TextView
            view_details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Use an Intent to navigate to DetailsActivity
                    Intent intent = new Intent(getActivity(), full_details.class);
                    startActivity(intent); // Start the new activity
                }
            });
        } else {
            // Log an error or handle the case where the view is not found
            Log.e("SettingsFragment", "TextView view_details not found in the layout");
        }

        return view;

    }
    private void showExitConfirmationDialog() {

        View customDialogView = getLayoutInflater().inflate(R.layout.dialog_logout, null);

        Button positiveButton = customDialogView.findViewById(R.id.dialog_positive);
        Button negativeButton = customDialogView.findViewById(R.id.dialog_negative);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(customDialogView)  // Set the custom layout as the dialog view
                .setCancelable(false)       // Make the dialog non-cancelable by touching outside
                .create();

        // Set the positive button click listener
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // Clear activity stack
                startActivity(intent);
            }
        });

        // Set the negative button click listener
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
