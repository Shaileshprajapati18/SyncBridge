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
    home home = new home();
    my_device my_device = new my_device();
    scanner scanner = new scanner();
    other_device other_device = new other_device();
    settings settings = new settings();

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_screen);

        bottom_navigation = findViewById(R.id.bottom_navigation);

        // Set the initial fragment
        if (savedInstanceState == null) {
            navigateToFragment(home, R.id.home_icon);  // Default to home fragment
        } else {
            currentFragment = getSupportFragmentManager().findFragmentById(R.id.open_screen);
        }

        // BottomNavigationView listener
        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                // Set the fragment based on the selected item
                if (itemId == R.id.home_icon) {
                    navigateToFragment(home, itemId);
                } else if (itemId == R.id.my_device) {
                    navigateToFragment(my_device, itemId);
                } else if (itemId == R.id.scanner) {
                    navigateToFragment(scanner, itemId);
                } else if (itemId == R.id.other_device) {
                    navigateToFragment(other_device, itemId);
                } else if (itemId == R.id.settings) {
                    navigateToFragment(settings, itemId);
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
        if (currentFragment != fragment) {
            currentFragment = fragment;

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.open_screen, fragment);
            transaction.addToBackStack(null);  // Add to back stack for back navigation
            transaction.commit();

            // Immediately set the selected item
            bottom_navigation.setSelectedItemId(menuItemId);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment current = fragmentManager.findFragmentById(R.id.open_screen);

        if (current instanceof home) {
            showExitConfirmationDialog();
        } else if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();

        } else {
            showExitConfirmationDialog();
        }
    }

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
        // Inflate the custom layout
        View customDialogView = getLayoutInflater().inflate(R.layout.dialog_custom, null);

        // Get references to the buttons in the custom layout
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

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
