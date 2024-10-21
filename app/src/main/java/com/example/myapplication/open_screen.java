package com.example.myapplication;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class open_screen extends AppCompatActivity {

    BottomNavigationView bottom_navigation;
    home homeFragment = new home();
    my_device myDeviceFragment = new my_device();
    scanner scannerFragment = new scanner();
    other_device otherDeviceFragment = new other_device();
    settings settingsFragment = new settings();

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_screen);

        bottom_navigation = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            navigateToFragment(homeFragment, R.id.home_icon);
        } else {
            currentFragment = getSupportFragmentManager().findFragmentById(R.id.open_screen);
        }

        // BottomNavigationView listener
        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                // Using if-else to set the fragment based on the selected item
                if (itemId == R.id.home_icon) {
                    navigateToFragment(homeFragment, itemId);
                } else if (itemId == R.id.my_device) {
                    navigateToFragment(myDeviceFragment, itemId);
                } else if (itemId == R.id.scanner) {
                    navigateToFragment(scannerFragment, itemId);
                } else if (itemId == R.id.other_device) {
                    navigateToFragment(otherDeviceFragment, itemId);
                } else if (itemId == R.id.settings) {
                    navigateToFragment(settingsFragment, itemId);
                }

                return true;
            }
        });

        // Listen for back stack changes and update the BottomNavigationView selection
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                updateBottomNavigationViewSelection();
            }
        });
    }

    // Method to navigate to fragments and update BottomNavigationView selection
    private void navigateToFragment(Fragment fragment, int menuItemId) {
        // Avoid reloading the same fragment
        if (currentFragment != fragment) {
            currentFragment = fragment;

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Check if the fragment is already added
            if (fragment.isAdded()) {
                // If the fragment is already added, just show it
                transaction.show(fragment);
            } else {
                // If it's not added, replace it
                transaction.replace(R.id.open_screen, fragment);

                // Only add non-home fragments to the back stack
                if (!(fragment instanceof home)) {
                    transaction.addToBackStack(null);
                }
            }

            transaction.commit();

            // Immediately set the selected item
            bottom_navigation.setSelectedItemId(menuItemId);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.open_screen);

        // Check if the current fragment is the home fragment
        if (currentFragment instanceof home) {
            showExitConfirmationDialog(); // Show the exit confirmation dialog
        } else if (currentFragment instanceof other_device) {
            ((other_device) currentFragment).handleBackPress();
        } else {
            // Pop the last fragment from the back stack
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            } else {
                super.onBackPressed(); // Exit the app if no fragments in back stack
            }
        }
    }

    // Method to update BottomNavigationView selection based on the current fragment
    private void updateBottomNavigationViewSelection() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.open_screen);

        if (fragment instanceof home) {
            bottom_navigation.setSelectedItemId(R.id.home_icon);
        } else if (fragment instanceof my_device) {
            bottom_navigation.setSelectedItemId(R.id.my_device);
        } else if (fragment instanceof scanner) {
            bottom_navigation.setSelectedItemId(R.id.scanner);
        } else if (fragment instanceof other_device) {
            bottom_navigation.setSelectedItemId(R.id.other_device);
        } else if (fragment instanceof settings) {
            bottom_navigation.setSelectedItemId(R.id.settings);
        }
    }

    // Method to show an AlertDialog when exiting the app
    private void showExitConfirmationDialog() {
        View customDialogView = getLayoutInflater().inflate(R.layout.dialog_custom, null);

        Button positiveButton = customDialogView.findViewById(R.id.dialog_positive);
        Button negativeButton = customDialogView.findViewById(R.id.dialog_negative);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(customDialogView)  // Set the custom layout as the dialog view
                .setCancelable(false)       // Make the dialog non-cancelable by touching outside
                .create();

        // Set the positive button click listener
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();  // Close the app
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
