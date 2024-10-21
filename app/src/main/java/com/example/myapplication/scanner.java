package com.example.myapplication;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class scanner extends Fragment {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 200;
    private CompoundBarcodeView barcodeScannerView;
    private ImageButton galleryButton;
    private ImageButton flashToggleButton;
    private boolean isFlashOn = false;
    private boolean isQrProcessed = false;

    public scanner() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);

        barcodeScannerView = view.findViewById(R.id.barcode_scanner);
        galleryButton = view.findViewById(R.id.gallery_button);
        flashToggleButton = view.findViewById(R.id.flash_toggle);

        barcodeScannerView.getStatusView().setVisibility(View.GONE);

        startScanning();

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            startScanning();
        }

        galleryButton.setOnClickListener(v -> openGallery());

        flashToggleButton.setOnClickListener(v -> toggleFlash());

        return view;
    }

    private void startScanning() {
        if (barcodeScannerView != null) {
            barcodeScannerView.decodeContinuous(callback);
        } else {
            Toast.makeText(getActivity(), "Barcode scanner view is not initialized", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    private void toggleFlash() {
        if (barcodeScannerView != null) {
            isFlashOn = !isFlashOn;
            if (isFlashOn) {
                barcodeScannerView.setTorchOn();  // Turn on the flash
            } else {
                barcodeScannerView.setTorchOff(); // Turn off the flash
            }
            flashToggleButton.setImageResource(isFlashOn ? R.drawable.flash_on : R.drawable.flash_off);
        }
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null && !isQrProcessed) {
                isQrProcessed = true;  // Ensure transition only happens once
                String qrData = result.getText();

                // Check if the scanned QR data is a valid UUID
                if (isValidUUID(qrData)) {
                    // Show the AlertDialog with progress bar
                    showSuccessDialog(qrData);
                } else {
                    // Show an alert that the QR code is invalid
                    showAlert("Invalid QR Code", "The scanned QR code does not contain valid UUID data.");
                }

                barcodeScannerView.pause();  // Stop scanning once QR code is processed
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    private boolean isValidUUID(String qrData) {
        String uuidRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        Pattern pattern = Pattern.compile(uuidRegex);
        Matcher matcher = pattern.matcher(qrData);
        return matcher.matches();
    }

    private void storeQRCodeData(String qrData) {
        // Get SharedPreferences instance
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Store QR code data
        editor.putString("SessionID", qrData);
        editor.apply();

        String userID = sharedPreferences.getString("UserID", "default_value");
        FirebaseDatabase.getInstance().getReference("Users")
                .child(userID)
                .child("Session")
                .child(qrData)
                .child("IsOccupied")
                .setValue("true")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseUpdate", "Data updated successfully");
                        openOtherDeviceFragment(qrData);
                    } else {
                        Log.e("FirebaseUpdate", "Update failed", task.getException());
                    }
                });

    }

    private void reloadScannerFragment() {
        // Reload the scanner fragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.open_screen, new scanner()); // Replace with a new instance of scanner fragment
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openOtherDeviceFragment(String qrData) {
        // Create the fragment and pass the QR code data
        other_device fragment = new other_device();
        Bundle args = new Bundle();
        args.putString("qrData", qrData); // Pass the scanned QR data
        fragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.open_screen, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        isQrProcessed = false;
        if (barcodeScannerView != null) {
            barcodeScannerView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (barcodeScannerView != null) {
            barcodeScannerView.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(getActivity(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(selectedImage);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    Result result = decodeQRCodeFromBitmap(bitmap);

                    if (result != null && result.getText() != null) {
                        String qrData = result.getText();
                        // Check if the scanned QR data is a valid UUID
                        if (isValidUUID(qrData)) {
                            // Show the connection success dialog with the fetched QR data
                            showSuccessDialog(qrData);
                        } else {
                            // Show an alert that the QR code is invalid
                            showAlert("Invalid QR Code", "The scanned QR code does not contain valid UUID data.");
                        }
                    } else {
                        Toast.makeText(getActivity(), "No QR code found in the image", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showSuccessDialog(String qrData) {
        // Create a progress bar
        ProgressBar progressBar = new ProgressBar(getActivity());
        progressBar.setIndeterminate(true);

        // Create a LinearLayout to hold the progress bar and the "Connecting..." message
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        // Create a TextView for the connecting message
        TextView connectingText = new TextView(getActivity());
        connectingText.setText("Connecting...");
        connectingText.setPadding(0, 20, 0, 20);

        // Add the progress bar and text to the layout
        layout.addView(progressBar);
        layout.addView(connectingText);

        // Flag to check if dialog was canceled
        final boolean[] isDialogCanceled = {false};

        // Build the AlertDialog with the loader and "Cancel" button
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Connection")
                .setView(layout)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDialogCanceled[0] = true;
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Simulating connection delay
        new Thread(() -> {
            try {
                // Simulate connection time
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Dismiss the dialog only if it hasn't been canceled
            if (!isDialogCanceled[0]) {
                dialog.dismiss();
                storeQRCodeData(qrData);
            }
        }).start();
    }

    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    reloadScannerFragment(); // Reload the scanner fragment
                })
                .setCancelable(false);
        builder.create().show();
    }

    private Result decodeQRCodeFromBitmap(Bitmap bitmap) {
        // Create a binary bitmap from the given bitmap
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(width, height, pixels)));

        try {
            return new MultiFormatReader().decode(binaryBitmap);
        } catch (Exception e) {
            return null; // QR code could not be decoded
        }
    }
}
