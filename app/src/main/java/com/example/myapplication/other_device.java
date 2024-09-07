package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class other_device extends Fragment {

    private TextView qrCodeTextView;
    private String qrData;

    public other_device() {
        // Required empty public constructor
    }

    public static other_device newInstance(String param1, String param2) {
        other_device fragment = new other_device();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            qrData = getArguments().getString("qrData"); // Retrieve the passed QR code data
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_other_device, container, false);

        // Link the TextView
        qrCodeTextView = view.findViewById(R.id.qrCodeTextView);

        // Set the QR code data to the TextView
        if (qrData != null) {
            qrCodeTextView.setText(qrData);  // Display the QR code data
        } else {
            qrCodeTextView.setText("No QR Code data available.");  // Handle the case where no data is available
        }

        return view;
    }
}
