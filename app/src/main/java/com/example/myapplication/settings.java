package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.TextView;

public class settings extends Fragment {

    // Declare the TextView
    TextView view_details;

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

        // Initialize the TextView by finding its ID in the inflated view
        view_details = view.findViewById(R.id.full_details);

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
}
