package com.example.recipeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterPage extends AppCompatActivity {

    private EditText usernameInput, emailInput, passwordInput;
    private CheckBox rememberMeCheckbox;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private boolean isPasswordVisible = false;
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_INTRO_COMPLETED = "IntroCompleted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI components
        usernameInput = findViewById(R.id.username_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        rememberMeCheckbox = findViewById(R.id.remember_me_checkbox);
        ImageView eyeIcon = findViewById(R.id.eye_icon);
        ImageView backArrow = findViewById(R.id.ic_back_arrow); // Back arrow
        Button signUpButton = findViewById(R.id.sign_up_button);
        TextView forgotPassword = findViewById(R.id.forgot_password_text);
        TextView loginText = findViewById(R.id.login_text);

        // Password toggle visibility
        eyeIcon.setOnClickListener(v -> togglePasswordVisibility());

        // Sign Up Button
        signUpButton.setOnClickListener(v -> registerUser());

        // Already have an account - go to LoginPage
        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterPage.this, LoginPage.class);
            startActivity(intent);
            finish();
        });

        // Back arrow action to go to SplashPage
        backArrow.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterPage.this, SplashPage.class);
            startActivity(intent);
            finish(); // Finish the current activity to remove it from the back stack
        });

        // Forgot password action
        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterPage.this, ForgotPasswordPage.class);
            startActivity(intent);
        });
    }

    // Method to register the user with Firebase Authentication and save username to Firestore
    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();

        if (!validateInput()) {
            return;
        }

        // Firebase Authentication: Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration success, save the username to Firestore
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), username, email);
                        }

                        // Mark intro as completed (since registration is done)
                        markIntroCompleted();

                        // Navigate to HomePage
                        Intent intent = new Intent(RegisterPage.this, HomePage.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Registration failed
                        Toast.makeText(RegisterPage.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to save user info (username and email) to Firestore
    private void saveUserToFirestore(String userId, String username, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);

        // Save to Firestore under the 'users' collection using the user's UID
        firestore.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    // Successfully saved user info
                    Toast.makeText(RegisterPage.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Error saving user info
                    Toast.makeText(RegisterPage.this, "Failed to save user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to validate user input
    private boolean validateInput() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Username validation (must be at least 2 characters, alphanumeric)
        if (username.length() < 2 || !username.matches("[a-zA-Z0-9]+")) {
            usernameInput.setError("Username must be at least 2 characters and alphanumeric");
            return false;
        }

        // Email validation
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            return false;
        }

        // Password validation (must be at least 8 characters)
        if (password.length() < 8) {
            passwordInput.setError("Password must be at least 8 characters long");
            return false;
        }

        return true;
    }

    // Method to toggle password visibility
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordInput.setInputType(129); // Hide password
            isPasswordVisible = false;
        } else {
            passwordInput.setInputType(1); // Show password
            isPasswordVisible = true;
        }
        passwordInput.setSelection(passwordInput.length());
    }

    // Mark the intro as completed after successful registration
    private void markIntroCompleted() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_INTRO_COMPLETED, true);
        editor.apply();
    }
}
