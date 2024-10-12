package com.example.recipeapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditUserName extends AppCompatActivity {

    private EditText usernameInput;
    private TextView characterCount;
    private Button cancelButton, saveButton;

    private static final int MAX_CHAR_COUNT = 30;
    private FirebaseDatabase database;
    private DatabaseReference usernameCheckRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_user_name);

        // Initialize views
        usernameInput = findViewById(R.id.name_input);
        characterCount = findViewById(R.id.character_count);
        cancelButton = findViewById(R.id.cancel_button);
        saveButton = findViewById(R.id.save_button);

        // Initialize Firebase references
        database = FirebaseDatabase.getInstance();
        usernameCheckRef = database.getReference("usernames");

        // Set initial character count
        characterCount.setText("0/" + MAX_CHAR_COUNT);

        // TextWatcher to update the character count dynamically
        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int currentLength = s.length();
                characterCount.setText(currentLength + "/" + MAX_CHAR_COUNT);

                // Disable save button if character limit is exceeded
                saveButton.setEnabled(currentLength <= MAX_CHAR_COUNT);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Cancel button click listener - return to EditProfile without saving changes
        cancelButton.setOnClickListener(v -> finish());

        // Save button click listener - validate and save the username
        saveButton.setOnClickListener(v -> {
            String newUsername = usernameInput.getText().toString().trim();

            if (newUsername.isEmpty()) {
                Toast.makeText(EditUserName.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the new username is unique
            usernameCheckRef.child(newUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Toast.makeText(EditUserName.this, "Username is already taken", Toast.LENGTH_SHORT).show();
                    } else {
                        // Username is unique, pass it back to EditProfile
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("newUsername", newUsername);
                        setResult(RESULT_OK, resultIntent);
                        finish(); // Close the activity and return to EditProfile
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(EditUserName.this, "Error checking username", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
