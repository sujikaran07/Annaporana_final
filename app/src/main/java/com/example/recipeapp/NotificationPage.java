package com.example.recipeapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class NotificationPage extends AppCompatActivity {

    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_page);

        // Initialize UI components
        Button skipButton = findViewById(R.id.skip_button);
        Button enableButton = findViewById(R.id.enable_button);

        // Set OnClickListener for Skip button
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Page 3
                goToPage3();
            }
        });

        // Set OnClickListener for Enable button
        enableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Enable notifications
                enableNotifications();
            }
        });
    }

    // Method to navigate to Page 3
    private void goToPage3() {
        Intent intent = new Intent(NotificationPage.this, PageNo3.class); // Replace with actual activity
        startActivity(intent);
        finish(); // Close current activity
    }

    // Method to enable notifications and handle permission request
    private void enableNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if notification permission is already granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request notification permission
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIFICATION_PERMISSION);
            } else {
                // Permission already granted, enable notifications
                enableNotificationSettings();
                goToPage3(); // Navigate to Page 3 after enabling notifications
            }
        } else {
            // No need to ask permission below Android 13
            enableNotificationSettings();
            goToPage3(); // Navigate to Page 3 after enabling notifications
        }
    }

    // Callback when the user responds to the permission request dialog
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            // Check if permission was granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable notifications
                enableNotificationSettings();
            }
            // Navigate to Page 3 after the permission result
            goToPage3();
        }
    }

    // Method to actually enable notifications
    private void enableNotificationSettings() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (notificationManager.areNotificationsEnabled()) {
            // Notifications are enabled, you can set up or send notifications here
        } else {
            // Notifications are not enabled; prompt the user to enable them in system settings
        }
    }
}
