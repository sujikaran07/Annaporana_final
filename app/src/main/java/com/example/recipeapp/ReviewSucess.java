package com.example.recipeapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class ReviewSucess extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_sucess);

        // Initialize the "Done" button
        Button doneButton = findViewById(R.id.bottomButton);

        // Set the click listener for the "Done" button
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to HomeFragment
                navigateToHomeFragment();
            }
        });
    }

    // Method to navigate back to HomeFragment
    private void navigateToHomeFragment() {
        // Check if the frame_layout exists in the current layout
        if (findViewById(R.id.frame_layout) != null) {
            Fragment homeFragment = new HomeFragment();  // Assuming HomeFragment is your destination
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // Replace the current fragment with HomeFragment
            fragmentTransaction.replace(R.id.frame_layout, homeFragment);
            fragmentTransaction.commit();
        } else {
            // Log an error or show a toast if the frame layout is not found
            // For now, just close the activity
            finish();  // Closes the current activity
        }
    }
}
