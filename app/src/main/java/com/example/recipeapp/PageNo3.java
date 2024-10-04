package com.example.recipeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class PageNo3 extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_no3);

        Button skipButton = findViewById(R.id.top_left_button);
        Button continueButton = findViewById(R.id.continue_button);

        // Skip button to go to SplashPage
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSplashPage();
            }
        });

        // Continue button to go to PageNo4
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPageNo4();
            }
        });
    }

    // Navigate to SplashPage
    private void goToSplashPage() {
        Intent intent = new Intent(PageNo3.this, SplashPage.class);
        startActivity(intent);
        finish();
    }

    // Navigate to PageNo4
    private void goToPageNo4() {
        Intent intent = new Intent(PageNo3.this, PageNo4.class);
        startActivity(intent);
        finish();
    }
}
