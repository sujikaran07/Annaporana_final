package com.example.recipeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class PageNo5 extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_INTRO_COMPLETED = "IntroCompleted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_no5);

        Button continueButton = findViewById(R.id.continue_button);

        // Continue button to finish intro and go to SplashPage
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mark the intro as completed
                markIntroCompleted();
                goToSplashPage();
            }
        });
    }

    // Navigate to SplashPage
    private void goToSplashPage() {
        Intent intent = new Intent(PageNo5.this, SplashPage.class);
        startActivity(intent);
        finish();
    }

    // Mark the intro as completed in SharedPreferences
    private void markIntroCompleted() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_INTRO_COMPLETED, true);
        editor.apply();
    }
}
