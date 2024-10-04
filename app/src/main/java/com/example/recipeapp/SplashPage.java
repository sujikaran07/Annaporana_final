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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the intro is completed
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean introCompleted = prefs.getBoolean(KEY_INTRO_COMPLETED, false);

        // Check if the user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null && introCompleted) {
            // If user is logged in and intro is completed, go to home page
            goToHomePage();
            return;
        }

        if (!introCompleted) {
            // If intro is not completed, go to GetStartPage
            Intent intent = new Intent(SplashPage.this, GetStartPage.class);
            startActivity(intent);
            finish();
            return;
        }

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

    private void goToHomePage() {
        Intent intent = new Intent(SplashPage.this, HomePage.class);
        startActivity(intent);
        finish();  // Close the splash page
    }
}
