package com.example.recipeapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
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

        // Handle bottom navigation item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Log.d(TAG, "Menu item selected: " + id);  // Log the selected item

            // Launch the appropriate activity based on the clicked item
            if (id == R.id.home) {
                Log.d(TAG, "Home button clicked");
                startActivity(new Intent(HomePage.this, PageHome.class));  // Reload HomePage
            } else if (id == R.id.search) {
                Log.d(TAG, "Search button clicked");
                startActivity(new Intent(HomePage.this,PageSearch.class));
            } else if (id == R.id.add) {
                Log.d(TAG, "Add button clicked");
                startActivity(new Intent(HomePage.this, PageAdd.class));  // Launch AddActivity
            } else if (id == R.id.fav) {
                Log.d(TAG, "Favorites button clicked");
                startActivity(new Intent(HomePage.this, PageFavourite.class));  // Launch FavoriteActivity
            } else if (id == R.id.profile) {
                Log.d(TAG, "Profile button clicked");
                startActivity(new Intent(HomePage.this, PageProfile.class));  // Launch ProfileActivity
            }

            return true;
        });
    }
}
