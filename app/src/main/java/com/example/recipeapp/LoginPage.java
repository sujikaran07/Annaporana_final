package com.example.recipeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginPage extends AppCompatActivity {

    private EditText emailOrUsernameInput, passwordInput;
    private CheckBox rememberMeCheckbox;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // Initialize Firebase Auth and Database reference
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");  // Reference to "users" node in Firebase Realtime Database

        // Initialize UI components
        emailOrUsernameInput = findViewById(R.id.input_field);
        passwordInput = findViewById(R.id.password_input);
        rememberMeCheckbox = findViewById(R.id.remember_me_checkbox);
        ImageView eyeIcon = findViewById(R.id.eye_icon);
        Button loginButton = findViewById(R.id.login_button);
        TextView forgotPassword = findViewById(R.id.forgot_password_text);
        TextView signUpText = findViewById(R.id.sign_up_text);
        ImageView backArrow = findViewById(R.id.ic_back_arrow);  // Back arrow

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

        // Back arrow action to go to SplashPage
        backArrow.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPage.this, SplashPage.class);
            startActivity(intent);
            finish();
        });
    }

    // Method to log in the user
    private void loginUser() {
        String emailOrUsername = emailOrUsernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (!validateInput()) {
            return;
        }

        // Check if the input is an email or username
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrUsername).matches()) {
            // Input is an email, proceed to login
            loginWithEmail(emailOrUsername, password);
        } else {
            // Input is a username, find corresponding email
            findEmailByUsername(emailOrUsername, password);
        }
    }

    // Method to log in with email
    private void loginWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
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

    // Method to find the email by username in the database
    private void findEmailByUsername(String username, String password) {
        usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String email = userSnapshot.child("email").getValue(String.class);
                        if (email != null) {
                            loginWithEmail(email, password);
                        } else {
                            Toast.makeText(LoginPage.this, "Email not found for this username.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(LoginPage.this, "Username does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError databaseError) {
                Toast.makeText(LoginPage.this, "Error accessing database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to validate user input
    private boolean validateInput() {
        String emailOrUsername = emailOrUsernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Input validation for email/username
        if (emailOrUsername.isEmpty()) {
            emailOrUsernameInput.setError("Email or Username is required");
            return false;
        }

        // Password validation
        if (password.isEmpty()) {
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
}
