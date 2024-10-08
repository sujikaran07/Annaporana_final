package com.example.recipeapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class PageSettings extends AppCompatActivity {

    // Declare the TextViews, Button, and Back Arrow ImageView
    private TextView inviteFriends, privacyPolicy, aboutApp;
    private Button signOutButton;
    private ImageView backArrow; // Back arrow ImageView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page1); // Ensure your layout file is named correctly

        // Initialize TextViews, Button, and Back Arrow
        inviteFriends = findViewById(R.id.invite_friends);
        privacyPolicy = findViewById(R.id.privacyPolicy);
        aboutApp = findViewById(R.id.aboutApp);
        signOutButton = findViewById(R.id.signOutButton);
        backArrow = findViewById(R.id.backArrow); // Initialize back arrow

        // Set click listener for back arrow
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the current activity to go back to the previous screen
                finish();
            }
        });

        // Set click listener for Invite Friends TextView
        inviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Invite Friends page when clicked
                Intent intent = new Intent(PageSettings.this, InviteFriendsActivity.class);
                startActivity(intent); // Starts the InviteFriendsActivity
            }
        });

        // Privacy policy click listener
        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define the URL for the privacy policy
                String url = "https://your-privacy-policy-url.com";

                // Create an intent to open the browser with the URL
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));

                // Start the activity (browser) with the intent
                startActivity(intent);
            }
        });

        // Handle sign out button click
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create custom AlertDialog to show confirmation
                AlertDialog.Builder builder = new AlertDialog.Builder(PageSettings.this);
                builder.setTitle("Sign out");
                builder.setMessage("Do you want to sign out?");

                // Set the positive button ("Yes") to confirm sign out
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Perform sign out logic here
                        FirebaseAuth.getInstance().signOut();

                        // Clear SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear(); // Clear all stored user data
                        editor.apply(); // Apply the changes

                        // After sign out, navigate to the GetStartPage
                        Intent intent = new Intent(PageSettings.this, GetStartPage.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
                        startActivity(intent);
                        finish(); // Close current activity to prevent return
                    }
                });

                // Set the negative button ("No") to dismiss the dialog
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the dialog and stay on the settings page
                        dialog.dismiss();
                    }
                });

                // Create and show the dialog
                final AlertDialog dialog = builder.create();

                // Set OnShowListener to customize the button colors after the dialog is shown
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        // Customize the buttons after the dialog is shown
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    }
                });

                dialog.show();
            }
        });
    }
}
