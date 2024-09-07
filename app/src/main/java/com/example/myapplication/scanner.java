package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import java.io.InputStream;
import java.util.List;

public class scanner extends Fragment {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 200;
    private CompoundBarcodeView barcodeScannerView;
    private ImageButton galleryButton;
    private ImageButton flashToggleButton;
    private boolean isFlashOn = false;

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


        if (barcodeScannerView == null) {
            Toast.makeText(getActivity(), "Barcode scanner view is not initialized", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            startScanning();
        }

        // Set up click listener for the gallery button
        galleryButton.setOnClickListener(v -> openGallery());

        // Set up click listener for the flash toggle button
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
            if (result.getText() != null) {
                String qrData = result.getText();

                // Create the fragment and pass the QR code data
                other_device fragment = new other_device();
                Bundle args = new Bundle();
                args.putString("qrData", qrData); // Pass the scanned QR data
                fragment.setArguments(args);

                // Use FragmentTransaction to display the fragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.open_screen, fragment); // replace with your fragment container ID
                transaction.commit();
            }
        }

        @Override
        public void possibleResultPoints(List resultPoints) {
        }
    };

    @Override
    public void onResume() {
        super.onResume();
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

                        // Create the fragment and pass the QR code data
                        other_device fragment = new other_device();
                        Bundle args = new Bundle();
                        args.putString("qrData", qrData);
                        fragment.setArguments(args);

                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.replace(R.id.open_screen, fragment);
                        transaction.commit();
                    } else {
                        Toast.makeText(getActivity(), "No QR code found in the image", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private Result decodeQRCodeFromBitmap(Bitmap bitmap) {
        try {
            MultiFormatReader reader = new MultiFormatReader();
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] intArray = new int[width * height];
            bitmap.getPixels(intArray, 0, width, 0, 0, width, height);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(width, height, intArray)));
            return reader.decode(binaryBitmap);
        } catch (Exception e) {
            return null;
        }
    }

    private static class RGBLuminanceSource extends com.google.zxing.LuminanceSource {
        private final int[] rgb;

        RGBLuminanceSource(int width, int height, int[] rgb) {
            super(width, height);
            this.rgb = rgb;
        }

        @Override
        public byte[] getMatrix() {
            int width = getWidth();
            int height = getHeight();
            byte[] matrix = new byte[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    int pixel = rgb[offset + x];
                    int r = (pixel >> 16) & 0xFF;
                    int g = (pixel >> 8) & 0xFF;
                    int b = pixel & 0xFF;
                    matrix[offset + x] = (byte) ((r + g + b) / 3);
                }
            }
            return matrix;
        }

        @Override
        public byte[] getRow(int y, byte[] row) {
            if (row == null || row.length < getWidth()) {
                row = new byte[getWidth()];
            }
            System.arraycopy(getMatrix(), y * getWidth(), row, 0, getWidth());
            return row;
        }
    }
}
