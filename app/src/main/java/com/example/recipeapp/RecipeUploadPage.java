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
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
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
    private static final int PICK_IMAGE_REQUEST = 2;  // Added for image
    private static final int PERMISSION_REQUEST_CODE = 100;

    private Uri videoUri, imageUri;  // Added imageUri for image

    // Firebase instances
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    // UI elements
    private VideoView videoView;
    private ImageView uploadIcon, recipeImageView;  // Added recipeImageView for image
    private TextView uploadVideoText, uploadImageText;  // Added uploadImageText for image
    private EditText titleEditText, descriptionEditText, servesEditText, cookTimeEditText, nutritionEditText, ingredientsEditText, methodsEditText;
    private Button uploadButton;
    private ImageButton closeButton;
    private Spinner foodCategorySpinner;  // Added spinner for food category
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
        recipeImageView = findViewById(R.id.recipeImageView);  // ImageView for the recipe image
        uploadImageText = findViewById(R.id.uploadImageText);  // Text for image upload
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
        foodCategorySpinner = findViewById(R.id.foodCategorySpinner);  // Spinner for food category

        // Add input validation for "Serves", "Cook time", and "Nutrition"
        servesEditText.addTextChangedListener(new AutoFillWatcher(servesEditText, " people"));
        cookTimeEditText.addTextChangedListener(new AutoFillWatcher(cookTimeEditText, " mins"));
        nutritionEditText.addTextChangedListener(new AutoFillWatcher(nutritionEditText, " kcal"));

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

        // Handle image upload click
        uploadImageText.setOnClickListener(v -> openImagePicker());

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

    // Open image picker to select a recipe image
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Check if permission is granted
    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    // Request for permission
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            } catch (Exception e) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openVideoPicker();
            } else {
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

    // Set the selected video to the VideoView
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
        }
    }

    // Set the selected image to the ImageView
    private void setImageToView(Uri imageUri) {
        if (imageUri != null) {
            recipeImageView.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
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
        if (imageUri == null) {
            Toast.makeText(this, "Please upload an image", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            videoUri = data.getData();
            setVideoToView(videoUri);
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            // Check the file size of the selected image
            Cursor returnCursor = getContentResolver().query(imageUri, null, null, null, null);
            if (returnCursor != null) {
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                long fileSize = returnCursor.getLong(sizeIndex);
                returnCursor.close();

                // Check if the image size is less than 100MB (100 * 1024 * 1024 = 104857600 bytes)
                if (fileSize < 100 * 1024 * 1024) {
                    setImageToView(imageUri);
                } else {
                    Toast.makeText(this, "Image must be less than 100MB", Toast.LENGTH_SHORT).show();
                    imageUri = null;  // Reset the imageUri if the size is too large
                }
            }
        } else {
            Toast.makeText(this, "Failed to select media", Toast.LENGTH_SHORT).show();
        }
    }

    // Upload data to Firebase (video, image, and recipe details)
    private void uploadDataToFirebase() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            if (videoUri != null && imageUri != null) {
                String userId = currentUser.getUid();

                // Upload video to Firebase
                StorageReference videoRef = storageReference.child("videos/" + UUID.randomUUID().toString());
                videoRef.putFile(videoUri)
                        .addOnSuccessListener(taskSnapshot -> videoRef.getDownloadUrl().addOnSuccessListener(videoUrl -> {
                            // Upload image to Firebase
                            StorageReference imageRef = storageReference.child("images/" + UUID.randomUUID().toString());
                            imageRef.putFile(imageUri)
                                    .addOnSuccessListener(imageTaskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(imageUrl -> {
                                        // Save recipe details
                                        saveRecipeDetails(videoUrl.toString(), imageUrl.toString(), userId);
                                    }))
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RecipeUploadPage.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        progressDialog.dismiss();
                                    });
                        }))
                        .addOnFailureListener(e -> {
                            Toast.makeText(RecipeUploadPage.this, "Video upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        });
            }
        } else {
            Toast.makeText(this, "User is not authenticated. Please log in first.", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    // Save recipe details to Firebase Realtime Database
    private void saveRecipeDetails(String videoUrl, String imageUrl, String userId) {
        String recipeId = UUID.randomUUID().toString();
        String cookTime = cookTimeEditText.getText().toString().trim();
        String selectedCategory = foodCategorySpinner.getSelectedItem().toString();  // Get selected category

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
        recipeData.put("imageUrl", imageUrl);  // Added image URL
        recipeData.put("category", selectedCategory);  // Added food category

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
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (!s.toString().endsWith(suffix)) {
                editText.setText(s.toString().replace(suffix, "") + suffix);
                editText.setSelection(editText.getText().length() - suffix.length());
            }
        }
    }
}
