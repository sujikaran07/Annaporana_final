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

    private TextView inviteFriends, privacyPolicy, aboutApp;
    private Button signOutButton;
    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page1);

        inviteFriends = findViewById(R.id.invite_friends);
        privacyPolicy = findViewById(R.id.privacyPolicy);
        aboutApp = findViewById(R.id.aboutApp);
        signOutButton = findViewById(R.id.signOutButton);
        backArrow = findViewById(R.id.backArrow);

        backArrow.setOnClickListener(v -> finish());
        inviteFriends.setOnClickListener(v -> startActivity(new Intent(this, InviteFriendsActivity.class)));
        privacyPolicy.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://your-privacy-policy-url.com"))));

        signOutButton.setOnClickListener(v -> confirmSignOut());
    }

    private void confirmSignOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign out");
        builder.setMessage("Do you want to sign out?");
        builder.setPositiveButton("Yes", this::signOut);
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(this::customizeDialogButtons);
        dialog.show();
    }

    private void signOut(DialogInterface dialog, int which) {
        // Save any data needed before signing out
        saveDataBeforeSignOut();

        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Clear all data in SharedPreferences
        clearSharedPreferences();

        // Redirect user to the GetStartPage
        navigateToStartPage();
    }

    private void saveDataBeforeSignOut() {
        // Implement data save logic here if needed
    }

    private void clearSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private void navigateToStartPage() {
        Intent intent = new Intent(this, GetStartPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void customizeDialogButtons(DialogInterface dialogInterface) {
        AlertDialog dialog = (AlertDialog) dialogInterface;
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_green_dark));
    }
}
