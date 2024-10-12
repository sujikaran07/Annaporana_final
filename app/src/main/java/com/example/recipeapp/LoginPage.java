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
import com.google.firebase.database.DatabaseError;
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
        usersRef = FirebaseDatabase.getInstance().getReference("users");  // Reference to "users" node

        // Initialize UI components
        initUI();
    }

    private void initUI() {
        emailOrUsernameInput = findViewById(R.id.input_field);
        passwordInput = findViewById(R.id.password_input);
        rememberMeCheckbox = findViewById(R.id.remember_me_checkbox);
        ImageView eyeIcon = findViewById(R.id.eye_icon);
        Button loginButton = findViewById(R.id.login_button);
        TextView forgotPassword = findViewById(R.id.forgot_password_text);
        TextView signUpText = findViewById(R.id.sign_up_text);
        ImageView backArrow = findViewById(R.id.ic_back_arrow);

        // Setup listeners
        setupListeners(eyeIcon, loginButton, forgotPassword, signUpText, backArrow);
    }

    private void setupListeners(ImageView eyeIcon, Button loginButton, TextView forgotPassword, TextView signUpText, ImageView backArrow) {
        eyeIcon.setOnClickListener(v -> togglePasswordVisibility());
        loginButton.setOnClickListener(v -> loginUser());
        forgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginPage.this, ForgotPasswordPage.class)));
        signUpText.setOnClickListener(v -> {
            startActivity(new Intent(LoginPage.this, RegisterPage.class));
            finish();
        });
        backArrow.setOnClickListener(v -> finish());
    }

    private void loginUser() {
        String emailOrUsername = emailOrUsernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (!validateInput()) {
            return;
        }

        if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrUsername).matches()) {
            // Input is an email
            loginWithEmail(emailOrUsername, password);
        } else {
            // Input is a username
            findEmailByUsername(emailOrUsername, password);
        }
    }

    private void loginWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        proceedToHome();
                    } else {
                        Toast.makeText(LoginPage.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void findEmailByUsername(String username, String password) {
        usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String email = dataSnapshot.getChildren().iterator().next().child("email").getValue(String.class);
                    loginWithEmail(email, password);
                } else {
                    Toast.makeText(LoginPage.this, "Username does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginPage.this, "Failed to query database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput() {
        if (emailOrUsernameInput.getText().toString().trim().isEmpty()) {
            emailOrUsernameInput.setError("Email or Username is required");
            return false;
        }
        if (passwordInput.getText().toString().trim().isEmpty()) {
            passwordInput.setError("Password is required");
            return false;
        }
        return true;
    }

    private void togglePasswordVisibility() {
        if (passwordInput.getInputType() == 129) {
            passwordInput.setInputType(1);
        } else {
            passwordInput.setInputType(129);
        }
        passwordInput.setSelection(passwordInput.length());
    }

    private void proceedToHome() {
        startActivity(new Intent(this, HomePage.class));
        finish();
    }
}
