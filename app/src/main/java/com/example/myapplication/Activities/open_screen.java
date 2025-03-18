package com.example.myapplication.Activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.Fragments.home;
import com.example.myapplication.Fragments.my_device;
import com.example.myapplication.Fragments.other_device;
import com.example.myapplication.Fragments.scanner;
import com.example.myapplication.Fragments.settings;
import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class open_screen extends AppCompatActivity {

    BottomNavigationView bottom_navigation;
    private home homeFragment = new home();
    private my_device myDeviceFragment = new my_device();
    private scanner scannerFragment = new scanner();
    private other_device otherDeviceFragment = new other_device();
    private settings settingsFragment = new settings();
    private Fragment currentFragment;
    private AlertDialog exitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_screen);

        bottom_navigation = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.open_screen, homeFragment, "home");
            transaction.add(R.id.open_screen, myDeviceFragment, "my_device").hide(myDeviceFragment);
            transaction.add(R.id.open_screen, scannerFragment, "scanner").hide(scannerFragment);
            transaction.add(R.id.open_screen, otherDeviceFragment, "other_device").hide(otherDeviceFragment);
            transaction.add(R.id.open_screen, settingsFragment, "settings").hide(settingsFragment);
            transaction.commit();
            currentFragment = homeFragment;
        } else {
            homeFragment = (home) getSupportFragmentManager().findFragmentByTag("home");
            myDeviceFragment = (my_device) getSupportFragmentManager().findFragmentByTag("my_device");
            scannerFragment = (scanner) getSupportFragmentManager().findFragmentByTag("scanner");
            otherDeviceFragment = (other_device) getSupportFragmentManager().findFragmentByTag("other_device");
            settingsFragment = (settings) getSupportFragmentManager().findFragmentByTag("settings");
            currentFragment = findVisibleFragment();
            if (currentFragment == null) {
                currentFragment = homeFragment;
                switchFragment(homeFragment);
            }
        }

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                Fragment selectedFragment = null;

                if (itemId == R.id.home_icon) {
                    selectedFragment = homeFragment;
                } else if (itemId == R.id.my_device) {
                    selectedFragment = myDeviceFragment;
                } else if (itemId == R.id.scanner) {
                    selectedFragment = scannerFragment;
                } else if (itemId == R.id.other_device) {
                    selectedFragment = otherDeviceFragment;
                } else if (itemId == R.id.settings) {
                    selectedFragment = settingsFragment;
                }

                if (selectedFragment != null && selectedFragment != currentFragment) {
                    switchFragment(selectedFragment);
                    bottom_navigation.setSelectedItemId(itemId);
                }
                return true;
            }
        });
    }

    public void switchFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (currentFragment != null && currentFragment != fragment) {
            transaction.hide(currentFragment);
            Log.d("OpenScreen", "Hiding fragment: " + currentFragment.getClass().getSimpleName());
        }

        if (fragment.isAdded()) {
            transaction.show(fragment);
            Log.d("OpenScreen", "Showing fragment: " + fragment.getClass().getSimpleName());
        } else {
            transaction.add(R.id.open_screen, fragment, fragment.getClass().getSimpleName());
            Log.d("OpenScreen", "Adding fragment: " + fragment.getClass().getSimpleName());
        }

        transaction.commitNow();
        currentFragment = fragment;
        updateBottomNavigationViewSelection();
    }

    // New method to switch to the existing my_device fragment
    public void switchToMyDeviceFragment() {
        if (myDeviceFragment != null && myDeviceFragment != currentFragment) {
            switchFragment(myDeviceFragment);
            bottom_navigation.setSelectedItemId(R.id.my_device);
        }
    }

    @Override
    public void onBackPressed() {
        if (currentFragment != homeFragment) {
            Log.d("OpenScreen", "Navigating to home from: " + currentFragment.getClass().getSimpleName());
            switchFragment(homeFragment);
            bottom_navigation.setSelectedItemId(R.id.home_icon);
        } else {
            Log.d("OpenScreen", "Checking dialog state from home");
            if (isDialogVisible()) {
                Log.d("OpenScreen", "Dialog is visible, dismissing it");
                exitDialog.dismiss();
            } else {
                Log.d("OpenScreen", "Showing exit dialog from home");
                showExitConfirmationDialog();
            }
        }
    }

    private void updateBottomNavigationViewSelection() {
        if (currentFragment == homeFragment) {
            bottom_navigation.setSelectedItemId(R.id.home_icon);
        } else if (currentFragment == myDeviceFragment) {
            bottom_navigation.setSelectedItemId(R.id.my_device);
        } else if (currentFragment == scannerFragment) {
            bottom_navigation.setSelectedItemId(R.id.scanner);
        } else if (currentFragment == otherDeviceFragment) {
            bottom_navigation.setSelectedItemId(R.id.other_device);
        } else if (currentFragment == settingsFragment) {
            bottom_navigation.setSelectedItemId(R.id.settings);
        }
        Log.d("OpenScreen", "Updated BottomNavigationView to: " + bottom_navigation.getSelectedItemId());
    }

    private void showExitConfirmationDialog() {
        View customDialogView = getLayoutInflater().inflate(R.layout.dialog_custom, null);

        Button positiveButton = customDialogView.findViewById(R.id.dialog_positive);
        Button negativeButton = customDialogView.findViewById(R.id.dialog_negative);

        exitDialog = new AlertDialog.Builder(this)
                .setView(customDialogView)
                .setCancelable(false)
                .create();

        exitDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        positiveButton.setOnClickListener(v -> {
            Log.d("OpenScreen", "Close button clicked, finishing activity");
            exitDialog.dismiss();
            finish();
        });

        negativeButton.setOnClickListener(v -> {
            Log.d("OpenScreen", "Cancel button clicked, dismissing dialog");
            exitDialog.dismiss();
        });

        exitDialog.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(exitDialog.getWindow().getAttributes());
        lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        exitDialog.getWindow().setAttributes(lp);
    }

    private boolean isDialogVisible() {
        return exitDialog != null && exitDialog.isShowing();
    }

    private Fragment findVisibleFragment() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment != null && fragment.isVisible()) {
                Log.d("OpenScreen", "Visible fragment found: " + fragment.getClass().getSimpleName());
                return fragment;
            }
        }
        Log.d("OpenScreen", "No visible fragment found");
        return null;
    }
}