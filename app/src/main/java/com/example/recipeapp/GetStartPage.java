package com.example.recipeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class GetStartPage extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_INTRO_COMPLETED = "IntroCompleted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the intro pages have been completed
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean introCompleted = prefs.getBoolean(KEY_INTRO_COMPLETED, false);

        if (introCompleted) {
            // If the intro is completed, go directly to the SplashPage
            Intent intent = new Intent(GetStartPage.this, SplashPage.class);
            startActivity(intent);
            finish();
            return;  // Exit onCreate
        }

        setContentView(R.layout.getstartpage);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to NotificationPage after Get Started
                Intent intent = new Intent(GetStartPage.this, NotificationPage.class);
                startActivity(intent);
            }
        });
    }
}
