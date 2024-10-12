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

public class EditBio extends AppCompatActivity {

    private EditText bioInput;
    private TextView characterCount;
    private Button cancelButton, saveButton;

    private static final int MAX_CHAR_COUNT = 40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_bio);

        // Initialize views
        bioInput = findViewById(R.id.name_input); // Reuse the name_input ID from the layout
        characterCount = findViewById(R.id.character_count);
        cancelButton = findViewById(R.id.cancel_button);
        saveButton = findViewById(R.id.save_button);

        // Set initial character count
        characterCount.setText("0/" + MAX_CHAR_COUNT);

        // TextWatcher to update the character count dynamically
        bioInput.addTextChangedListener(new TextWatcher() {
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

        // Save button click listener - save the bio and return to EditProfile
        saveButton.setOnClickListener(v -> {
            String newBio = bioInput.getText().toString().trim();

            if (newBio.isEmpty()) {
                Toast.makeText(EditBio.this, "Bio cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pass the new bio back to EditProfile
            Intent resultIntent = new Intent();
            resultIntent.putExtra("newBio", newBio);
            setResult(RESULT_OK, resultIntent);
            finish(); // Close the activity and return to EditProfile
        });
    }
}
