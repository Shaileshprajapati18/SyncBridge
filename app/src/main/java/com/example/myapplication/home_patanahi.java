package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.List;

public class home_patanahi extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private CompoundBarcodeView barcodeScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        barcodeScannerView = findViewById(R.id.barcode_scanner);

        if (barcodeScannerView == null) {
            Toast.makeText(this, "Barcode scanner view is not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        // Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            startScanning();
        }
    }

    private void startScanning() {
        if (barcodeScannerView != null) {
            barcodeScannerView.decodeContinuous(callback);
        } else {
            Toast.makeText(this, "Barcode scanner view is not initialized", Toast.LENGTH_SHORT).show();
        }
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                String qrData = result.getText();

                // Create the fragment and pass the QR code data
                my_device fragment = new my_device();
                Bundle args = new Bundle();
                args.putString("qrData", qrData); // Pass the scanned QR data
                fragment.setArguments(args);

                // Use FragmentTransaction to display the fragment
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.open_screen, fragment); // replace with your fragment container ID
                transaction.commit();
            }
        }

        @Override
        public void possibleResultPoints(List resultPoints) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeScannerView != null) {
            barcodeScannerView.resume();
        }
    }

    @Override
    protected void onPause() {
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
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
