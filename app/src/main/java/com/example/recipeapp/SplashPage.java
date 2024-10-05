package com.example.recipeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashPage extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_INTRO_COMPLETED = "IntroCompleted";
    private static final String SKIP_INTRO = "SkipIntro";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user has skipped the intro
        boolean skipIntro = getIntent().getBooleanExtra(SKIP_INTRO, false);

        // Access SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // If skip intro is true, mark intro as completed
        if (skipIntro) {
            markIntroCompleted(prefs);
        }

        // Check if the intro is completed
        boolean introCompleted = prefs.getBoolean(KEY_INTRO_COMPLETED, false);

        // Check if the user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Logic to determine navigation
        if (introCompleted && currentUser != null) {
            // If intro is completed and the user is logged in, go to the home page
            goToHomePage();
        } else if (!introCompleted) {
            // If the intro is not completed, go to GetStartPage
            Intent intent = new Intent(SplashPage.this, GetStartPage.class);
            startActivity(intent);
            finish();
        } else {
            // Show the splash page for users who haven't skipped or completed the intro
            setContentView(R.layout.splash_page);

            Button signInButton = findViewById(R.id.signin_btn);
            Button createAccountButton = findViewById(R.id.create_btn);

            // Sign in button
            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Navigate to LoginPage
                    Intent intent = new Intent(SplashPage.this, LoginPage.class);
                    startActivity(intent);
                }
            });

            // Create account button
            createAccountButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Navigate to RegisterPage
                    Intent intent = new Intent(SplashPage.this, RegisterPage.class);
                    startActivity(intent);
                }
            });
        }
    }

    // Method to mark the intro as completed
    private void markIntroCompleted(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_INTRO_COMPLETED, true);
        editor.apply();
    }

    // Method to navigate to the home page
    private void goToHomePage() {
        Intent intent = new Intent(SplashPage.this, HomePage.class);
        startActivity(intent);
        finish();  // Close the splash page
    }
}
