package com.example.recipeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class PageNo4 extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_no4);

        Button skipButton = findViewById(R.id.top_left_button);
        Button continueButton = findViewById(R.id.continue_button);

        // Skip button to go to SplashPage
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSplashPage();
            }
        });

        // Continue button to go to PageNo5
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPageNo5();
            }
        });
    }

    // Navigate to SplashPage
    private void goToSplashPage() {
        Intent intent = new Intent(PageNo4.this, SplashPage.class);
        intent.putExtra("SkipIntro", true);
        startActivity(intent);
        finish();
    }

    // Navigate to PageNo5
    private void goToPageNo5() {
        Intent intent = new Intent(PageNo4.this, PageNo5.class);
        startActivity(intent);
        finish();
    }
}
