package com.example.recipeapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment; // <-- Import this class
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private static final int PERMISSION_REQUEST_CODE = 100;

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
    private ProgressDialog progressDialog;

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
        cookTimeEditText = findViewById(R.id.cookTimeEditText);
        nutritionEditText = findViewById(R.id.nutritionEditText);
        ingredientsEditText = findViewById(R.id.ingredientsEditText);
        methodsEditText = findViewById(R.id.methodsEditText);
        uploadButton = findViewById(R.id.uploadButton);
        closeButton = findViewById(R.id.cancelButton);

        // Add input validation for "Serves", "Cook time", and "Nutrition"
        servesEditText.addTextChangedListener(new AutoFillWatcher(servesEditText, " people"));
        cookTimeEditText.addTextChangedListener(new AutoFillWatcher(cookTimeEditText, " mins"));
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
            if (checkPermissions()) {
                openVideoPicker();
            } else {
                requestPermissions();
            }
        });

        // Handle recipe submission
        uploadButton.setOnClickListener(v -> {
            if (validateFields()) {
                if (isInternetAvailable()) {
                    progressDialog.show();
                    uploadDataToFirebase();
                } else {
                    Toast.makeText(RecipeUploadPage.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Check if permission is granted
    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 (API level 30) or higher
            return Environment.isExternalStorageManager();
        } else {
            // For Android 6.0 to Android 10
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    // Request for permission
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 (API level 30) or higher
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            } catch (Exception e) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            }
        } else {
            // For Android 6.0 to Android 10
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                openVideoPicker();
            } else {
                // Permission denied
                Toast.makeText(this, "Storage permission denied. Cannot upload video.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Open video picker if permission is granted
    private void openVideoPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    // Check network connectivity
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // Method to handle setting video into the VideoView and setting play/pause behavior
    private void setVideoToView(Uri videoUri) {
        if (videoUri != null) {
            videoView.setVideoURI(videoUri);
            videoView.requestFocus();
            uploadIcon.setVisibility(ImageView.GONE);
            uploadVideoText.setVisibility(TextView.GONE);

            videoView.setOnClickListener(v -> {
                if (videoView.isPlaying()) {
                    videoView.pause();
                } else {
                    videoView.start();
                }
            });

            videoView.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(RecipeUploadPage.this, "Error playing video", Toast.LENGTH_SHORT).show();
                return true;
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
                cookTimeEditText.getText().toString().isEmpty() ||
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

            Cursor returnCursor = getContentResolver().query(videoUri, null, null, null, null);
            if (returnCursor != null) {
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                long fileSize = returnCursor.getLong(sizeIndex);
                returnCursor.close();

                if (fileSize < 500 * 1024 * 1024) {
                    setVideoToView(videoUri);
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
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            if (videoUri != null) {
                StorageReference videoRef = storageReference.child("videos/" + UUID.randomUUID().toString());

                videoRef.putFile(videoUri)
                        .addOnSuccessListener(taskSnapshot -> videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String userId = currentUser.getUid();
                            saveRecipeDetails(uri.toString(), userId);
                        }))
                        .addOnFailureListener(e -> {
                            Toast.makeText(RecipeUploadPage.this, "Video upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("UploadError", e.getMessage());
                            progressDialog.dismiss(); // Dismiss the loading indicator if the upload fails
                        });
            }
        } else {
            // If the user is not authenticated, show an error message
            Toast.makeText(this, "User is not authenticated. Please log in first.", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    // Save recipe details to Firebase Realtime Database
    private void saveRecipeDetails(String videoUrl, String userId) {
        String recipeId = UUID.randomUUID().toString();
        String cookTime = cookTimeEditText.getText().toString().trim();

        Map<String, Object> recipeData = new HashMap<>();
        recipeData.put("recipeId", recipeId);
        recipeData.put("userId", userId);
        recipeData.put("title", titleEditText.getText().toString().trim());
        recipeData.put("description", descriptionEditText.getText().toString().trim());
        recipeData.put("serves", servesEditText.getText().toString().trim());
        recipeData.put("cookTime", cookTime);
        recipeData.put("nutrition", nutritionEditText.getText().toString().trim());
        recipeData.put("ingredients", ingredientsEditText.getText().toString().trim());
        recipeData.put("methods", methodsEditText.getText().toString().trim());
        recipeData.put("videoUrl", videoUrl);

        databaseReference.child(userId).child("recipes").child(recipeId).setValue(recipeData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RecipeUploadPage.this, "Recipe uploaded successfully", Toast.LENGTH_SHORT).show();
                        redirectToMyRecipes();
                    } else {
                        Toast.makeText(RecipeUploadPage.this, "Failed to upload recipe", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                });
    }

    // Redirect to My Recipes section after successful upload
    private void redirectToMyRecipes() {
        Intent intent = new Intent(RecipeUploadPage.this, PageCongrats.class);
        startActivity(intent);
        finish();
    }

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
