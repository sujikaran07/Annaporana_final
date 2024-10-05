package com.example.recipeapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RecipeUploadPage extends AppCompatActivity {

    private static final int PICK_VIDEO_REQUEST = 1;
    private Uri videoUri;

    // Firebase instances
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    // UI elements
    private VideoView videoView;
    private ImageView uploadIcon;
    private TextView uploadVideoText;
    private EditText titleEditText, descriptionEditText, servesEditText, cookTimeEditText, nutritionEditText, ingredientsEditText, methodsEditText;
    private Button uploadButton;
    private ImageButton closeButton;
    private ProgressDialog progressDialog;  // Loading indicator

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_upload_page);

        // Initialize Firebase instances
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("recipes");
        auth = FirebaseAuth.getInstance();

        // Initialize UI elements
        videoView = findViewById(R.id.videoView);
        uploadIcon = findViewById(R.id.uploadIcon);
        uploadVideoText = findViewById(R.id.uploadVideoText);
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        servesEditText = findViewById(R.id.servesEditText);
        cookTimeEditText = findViewById(R.id.cookTimeEditText); // For cook time in minutes
        nutritionEditText = findViewById(R.id.nutritionEditText);
        ingredientsEditText = findViewById(R.id.ingredientsEditText);
        methodsEditText = findViewById(R.id.methodsEditText);
        uploadButton = findViewById(R.id.uploadButton);
        closeButton = findViewById(R.id.cancelButton);

        // Add input validation for "Serves", "Cook time", and "Nutrition"
        servesEditText.addTextChangedListener(new AutoFillWatcher(servesEditText, " people"));
        cookTimeEditText.addTextChangedListener(new AutoFillWatcher(cookTimeEditText, " mins"));  // Auto-fill with "mins"
        nutritionEditText.addTextChangedListener(new AutoFillWatcher(nutritionEditText, " cal"));

        // Initialize progress dialog for showing loading state
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading your recipe...");
        progressDialog.setCancelable(false);

        // Handle close button click to navigate back to AddFragment
        closeButton.setOnClickListener(v -> finish());

        // Restore the videoUri if it was previously selected
        if (savedInstanceState != null) {
            videoUri = savedInstanceState.getParcelable("videoUri");
            if (videoUri != null) {
                setVideoToView(videoUri);
            }
        }

        // Handle video upload click
        uploadVideoText.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            startActivityForResult(intent, PICK_VIDEO_REQUEST);
        });

        // Handle recipe submission
        uploadButton.setOnClickListener(v -> {
            if (validateFields()) {
                // Show loading indicator
                progressDialog.show();
                uploadDataToFirebase();
            }
        });
    }

    // Method to handle setting video into the VideoView and resizing it to fit
    private void setVideoToView(Uri videoUri) {
        if (videoUri != null) {
            videoView.setVideoURI(videoUri);  // Set the video URI to the VideoView
            videoView.requestFocus();         // Request focus for the VideoView

            // Hide the upload icon and text once the video is set
            uploadIcon.setVisibility(ImageView.GONE);
            uploadVideoText.setVisibility(TextView.GONE);

            // Prepare the video but don't automatically play it
            videoView.setOnPreparedListener(mp -> {
                videoView.start();
            });

            // Handle video errors
            videoView.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(RecipeUploadPage.this, "Error playing video", Toast.LENGTH_SHORT).show();
                return true;  // Indicate that the error was handled
            });
        } else {
            Toast.makeText(this, "No video selected", Toast.LENGTH_SHORT).show();
        }
    }

    // Validate form fields
    private boolean validateFields() {
        if (titleEditText.getText().toString().isEmpty() ||
                descriptionEditText.getText().toString().isEmpty() ||
                servesEditText.getText().toString().isEmpty() ||
                cookTimeEditText.getText().toString().isEmpty() ||  // Now using only minutes for cook time
                nutritionEditText.getText().toString().isEmpty() ||
                ingredientsEditText.getText().toString().isEmpty() ||
                methodsEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (videoUri == null) {
            Toast.makeText(this, "Please upload a video", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Handle the video selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            videoUri = data.getData();

            // Check file size (max 500 MB)
            Cursor returnCursor = getContentResolver().query(videoUri, null, null, null, null);
            if (returnCursor != null) {
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                long fileSize = returnCursor.getLong(sizeIndex);
                returnCursor.close();

                if (fileSize < 500 * 1024 * 1024) {  // File size must be less than 500MB
                    setVideoToView(videoUri); // Set the video to VideoView
                } else {
                    Toast.makeText(this, "Video must be less than 500MB", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Failed to select video", Toast.LENGTH_SHORT).show();
        }
    }

    // Upload data to Firebase (both the video and recipe details)
    private void uploadDataToFirebase() {
        if (videoUri != null) {
            StorageReference videoRef = storageReference.child("videos/" + UUID.randomUUID().toString());

            videoRef.putFile(videoUri)
                    .addOnSuccessListener(taskSnapshot -> videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        FirebaseUser currentUser = auth.getCurrentUser();
                        if (currentUser != null) {
                            String userId = currentUser.getUid();
                            saveRecipeDetails(uri.toString(), userId);
                        }
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(RecipeUploadPage.this, "Video upload failed", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        progressDialog.dismiss(); // Dismiss the loading indicator if the upload fails
                    });
        }
    }

    // Save recipe details to Firebase Realtime Database
    private void saveRecipeDetails(String videoUrl, String userId) {
        String recipeId = UUID.randomUUID().toString();

        String cookTime = cookTimeEditText.getText().toString().trim(); // Use minutes for cook time

        Map<String, Object> recipeData = new HashMap<>();
        recipeData.put("recipeId", recipeId);
        recipeData.put("userId", userId);
        recipeData.put("title", titleEditText.getText().toString().trim());
        recipeData.put("description", descriptionEditText.getText().toString().trim());
        recipeData.put("serves", servesEditText.getText().toString().trim());
        recipeData.put("cookTime", cookTime);  // Save only minutes
        recipeData.put("nutrition", nutritionEditText.getText().toString().trim());
        recipeData.put("ingredients", ingredientsEditText.getText().toString().trim());
        recipeData.put("methods", methodsEditText.getText().toString().trim());
        recipeData.put("videoUrl", videoUrl);

        databaseReference.child(userId).child("recipes").child(recipeId).setValue(recipeData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RecipeUploadPage.this, "Recipe uploaded successfully", Toast.LENGTH_SHORT).show();
                        // Redirect to My Recipes section to view the uploaded recipes
                        redirectToMyRecipes();
                    } else {
                        Toast.makeText(RecipeUploadPage.this, "Failed to upload recipe", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss(); // Dismiss the loading indicator
                });
    }

    // Redirect to My Recipes section after successful upload
    private void redirectToMyRecipes() {
        Intent intent = new Intent(RecipeUploadPage.this, PageCongrats.class);
        startActivity(intent);
        finish();
    }

    // Save the videoUri state when the activity is destroyed (or rotated)
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (videoUri != null) {
            outState.putParcelable("videoUri", videoUri);
        }
    }

    // Custom TextWatcher for auto-filling fields
    private class AutoFillWatcher implements TextWatcher {
        private EditText editText;
        private String suffix;

        public AutoFillWatcher(EditText editText, String suffix) {
            this.editText = editText;
            this.suffix = suffix;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!s.toString().endsWith(suffix)) {
                editText.setText(s.toString().replace(suffix, "") + suffix);
                editText.setSelection(editText.getText().length() - suffix.length());
            }
        }
    }
}
