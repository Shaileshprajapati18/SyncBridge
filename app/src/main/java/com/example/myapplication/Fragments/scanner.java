package com.example.myapplication.Fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.example.myapplication.R;
import com.google.firebase.database.FirebaseDatabase;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);

        barcodeScannerView = view.findViewById(R.id.barcode_scanner);
        galleryButton = view.findViewById(R.id.gallery_button);
        flashToggleButton = view.findViewById(R.id.flash_toggle);

        barcodeScannerView.getStatusView().setVisibility(View.GONE);

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
            barcodeScannerView.resume();
            isQrProcessed = false;
            Log.d("ScannerFragment", "Scanning started");
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
                barcodeScannerView.setTorchOn();
            } else {
                barcodeScannerView.setTorchOff();
            }
            flashToggleButton.setImageResource(isFlashOn ? R.drawable.flash_on : R.drawable.flash_off);
        }
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null && !isQrProcessed) {
                isQrProcessed = true;
                String qrData = result.getText();

                if (isValidUUID(qrData)) {
                    showSuccessDialog(qrData);
                } else {
                    showAlert();
                }
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
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
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

    private void openOtherDeviceFragment(String qrData) {
        other_device fragment = new other_device();
        Bundle args = new Bundle();
        args.putString("qrData", qrData);
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
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startScanning();
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
                showCustomPermissionDialog();
            }
        }
    }

    private void showCustomPermissionDialog() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_permission_denied, null);

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        Button buttonSettings = dialogView.findViewById(R.id.button_settings);

        buttonSettings.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
            dialog.dismiss();
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
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
                        if (isValidUUID(qrData)) {
                            showSuccessDialog(qrData);
                        } else {
                            showAlert();
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
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_custom_connection, null);

        LottieAnimationView animationView = dialogView.findViewById(R.id.animationView);
        Button cancelButton = dialogView.findViewById(R.id.dialog_cancel_button);

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        final boolean[] isDialogCanceled = {false};

        animationView.playAnimation();

        cancelButton.setOnClickListener(v -> {
            isDialogCanceled[0] = true;
            animationView.cancelAnimation();
            dialog.dismiss();
            startScanning();
        });

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                300,
                getResources().getDisplayMetrics()
        );
        dialog.getWindow().setAttributes(params);

        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!isDialogCanceled[0] && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    animationView.cancelAnimation();
                    dialog.dismiss();
                    storeQRCodeData(qrData);
                });
            }
        }).start();
    }

    private void showAlert() {

        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_custom_invalidqqr);
        dialog.setCancelable(false);

        TextView okButton = dialog.findViewById(R.id.dialog_ok_button);
        TextView cancelButton = dialog.findViewById(R.id.dialog_cancel_button);

        okButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                isQrProcessed = false;
                startScanning();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            }
        });

        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private Result decodeQRCodeFromBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(width, height, pixels)));

        try {
            return new MultiFormatReader().decode(binaryBitmap);
        } catch (Exception e) {
            return null;
        }
    }
}