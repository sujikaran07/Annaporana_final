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

public class LoginPage extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private CheckBox rememberMeCheckbox;
    private FirebaseAuth mAuth;
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_INTRO_COMPLETED = "IntroCompleted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        emailInput = findViewById(R.id.input_field);
        passwordInput = findViewById(R.id.password_input);
        rememberMeCheckbox = findViewById(R.id.remember_me_checkbox);
        ImageView eyeIcon = findViewById(R.id.eye_icon);
        Button loginButton = findViewById(R.id.login_button);
        TextView forgotPassword = findViewById(R.id.forgot_password_text);
        TextView signUpText = findViewById(R.id.sign_up_text);

        // Password toggle visibility
        eyeIcon.setOnClickListener(v -> togglePasswordVisibility());

        // Login Button action
        loginButton.setOnClickListener(v -> loginUser());

        // Forgot password action
        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPage.this, ForgotPasswordPage.class);
            startActivity(intent);
        });

        // Sign-up link to go to RegisterPage
        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPage.this, RegisterPage.class);
            startActivity(intent);
            finish();
        });
    }

    // Method to log in the user
    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (!validateInput()) {
            return;
        }

        // Firebase Authentication: Log in with email and password
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Mark intro as completed since the user is now logged in
                            markIntroCompleted();

                            // Navigate to home page
                            Intent intent = new Intent(LoginPage.this, HomePage.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        // Show error message if login fails
                        Toast.makeText(LoginPage.this, "Email or password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to validate user input
    private boolean validateInput() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Email validation
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            return false;
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return false;
        }

        return true;
    }

    // Method to toggle password visibility
    private void togglePasswordVisibility() {
        if (passwordInput.getInputType() == 129) {  // If password is hidden
            passwordInput.setInputType(1);  // Show password
        } else {
            passwordInput.setInputType(129);  // Hide password
        }
        passwordInput.setSelection(passwordInput.length());  // Move cursor to end
    }

    // Mark the intro as completed in SharedPreferences after successful login
    private void markIntroCompleted() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_INTRO_COMPLETED, true);
        editor.apply();
    }
}
