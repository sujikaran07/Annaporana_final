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

import java.util.regex.Pattern;

public class EditName extends AppCompatActivity {

    private EditText nameInput;
    private TextView characterCount;
    private Button cancelButton, saveButton;

    private static final int MAX_CHAR_COUNT = 30;
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-Z\\-' ]+$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_name);

        // Initialize views
        nameInput = findViewById(R.id.name_input);
        characterCount = findViewById(R.id.character_count);
        cancelButton = findViewById(R.id.cancel_button);
        saveButton = findViewById(R.id.save_button);

        // Set initial character count
        characterCount.setText("0/" + MAX_CHAR_COUNT);

        // TextWatcher to update the character count dynamically
        nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int currentLength = s.length();
                characterCount.setText(currentLength + "/" + MAX_CHAR_COUNT);
                saveButton.setEnabled(currentLength <= MAX_CHAR_COUNT);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Cancel button click listener - return to EditProfile without saving changes
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Simply finish the activity and go back to the previous page
            }
        });

        // Save button click listener - validate and save the name
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = nameInput.getText().toString().trim();

                if (isValidName(newName)) {
                    // Name is valid, pass the new name back to EditProfileActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("newName", newName);
                    setResult(RESULT_OK, resultIntent);
                    finish(); // Close the activity and return to EditProfile
                } else {
                    // Show validation error
                    Toast.makeText(EditName.this, "Invalid name. Only letters, hyphens, apostrophes, and spaces are allowed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to validate name input
    private boolean isValidName(String name) {
        return name.length() <= MAX_CHAR_COUNT && VALID_NAME_PATTERN.matcher(name).matches();
    }
}
