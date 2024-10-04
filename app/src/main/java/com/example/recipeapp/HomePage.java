package com.example.recipeapp;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomePage extends AppCompatActivity {

    private static final String TAG = "HomePage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set item icon tint programmatically using the color selector
        bottomNavigationView.setItemIconTintList(getResources().getColorStateList(R.color.nav_item_selector));

        // Set default fragment to Home fragment when the app starts
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());  // Load Home Fragment by default
        }

        // Handle bottom navigation item selection with if-else statements
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Log.d(TAG, "Menu item selected: " + id);  // Log the selected item

            Fragment selectedFragment = null;

            // Use if-else instead of switch to avoid constant expression error
            if (id == R.id.home) {
                Log.d(TAG, "Home button clicked");
                selectedFragment = new HomeFragment();
            } else if (id == R.id.search) {
                Log.d(TAG, "Search button clicked");
                selectedFragment = new SearchFragment();
            } else if (id == R.id.add) {
                Log.d(TAG, "Add button clicked");
                selectedFragment = new AddFragment();
            } else if (id == R.id.fav) {
                Log.d(TAG, "Favorites button clicked");
                selectedFragment = new NotificationFragment();
            } else if (id == R.id.profile) {
                Log.d(TAG, "Profile button clicked");
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);  // Load the selected fragment
            }

            return true;
        });
    }

    // Helper method to load fragments
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);  // Replace the container with the new fragment
        transaction.addToBackStack(null);  // Optional: if you want to add the transaction to the backstack
        transaction.commit();
    }
}
