package com.example.recipeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordPage extends AppCompatActivity {

    private EditText emailInput;
    private Button sendButton;
    private FirebaseAuth firebaseAuth;
    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_page);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize Views
        emailInput = findViewById(R.id.username_input);
        sendButton = findViewById(R.id.sign_up_button);
        backArrow = findViewById(R.id.ic_back_arrow);  // Assuming there's a back arrow in the layout

        // Set Send Button Click Listener
        sendButton.setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();

            if (!email.isEmpty()) {
                sendPasswordResetEmail(email);
            } else {
                Toast.makeText(ForgotPasswordPage.this, "Please enter an email", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle back arrow click
        backArrow.setOnClickListener(view -> {
            navigateToLoginPage();  // Go back to the login page
        });
    }

    // Function to send password reset email
    private void sendPasswordResetEmail(String email) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordPage.this, "Password reset email sent to " + email, Toast.LENGTH_SHORT).show();
                        navigateToLoginPageAfterReset();
                    } else {
                        Toast.makeText(ForgotPasswordPage.this, "Error: Email not found or invalid", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Function to navigate to the Login Page after password reset
    private void navigateToLoginPageAfterReset() {
        // Set the flag to indicate the user has just reset their password
        SharedPreferences.Editor editor = getSharedPreferences("LoginPrefs", MODE_PRIVATE).edit();
        editor.putBoolean("passwordReset", true);  // Set the flag to true
        editor.apply();

        navigateToLoginPage();
    }

    // Navigate back to the LoginPage
    private void navigateToLoginPage() {
        Intent intent = new Intent(ForgotPasswordPage.this, LoginPage.class);
        intent.putExtra("fromForgotPassword", true);  // Set an extra flag to indicate return from ForgotPassword
        startActivity(intent);
        finish();
    }

    // Handle the device back button press
    @Override
    public void onBackPressed() {
        navigateToLoginPage();  // Go back to the login page
    }
}
