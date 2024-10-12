package com.example.recipeapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class EditProfile extends AppCompatActivity {

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int NAME_REQUEST_CODE = 101;
    private static final int USERNAME_REQUEST_CODE = 102;
    private static final int BIO_REQUEST_CODE = 103;

    private Uri imageUri;
    private ImageView profileImage;
    private TextView nameValue, usernameValue, bioValue;
    private FirebaseDatabase database;
    private DatabaseReference userRef, usernameCheckRef;
    private StorageReference storageReference;
    private FirebaseUser currentUser; // Holds the current logged-in user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        // Initialize Firebase Authentication to get the current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase Database and Storage references
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("users").child(currentUser.getUid());  // Use logged-in user's ID
        usernameCheckRef = database.getReference("usernames");
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        // Find views by ID
        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton cameraIcon = findViewById(R.id.camera_icon);
        profileImage = findViewById(R.id.profile_image);
        nameValue = findViewById(R.id.name_value);
        usernameValue = findViewById(R.id.username_value);
        bioValue = findViewById(R.id.bio_value);
        Button saveButton = findViewById(R.id.save_button);

        // Load existing user data from Firebase
        loadProfileData();

        // Back button click listener
        backButton.setOnClickListener(v -> finish());

        // Camera icon click listener to change profile picture
        cameraIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE_PICK_CODE);
        });

        // Edit name click listener
        View.OnClickListener editNameListener = v -> {
            Intent intent = new Intent(EditProfile.this, EditName.class);
            startActivityForResult(intent, NAME_REQUEST_CODE);
        };
        nameValue.setOnClickListener(editNameListener);
        findViewById(R.id.name_edit_icon).setOnClickListener(editNameListener);

        // Edit username click listener
        View.OnClickListener editUsernameListener = v -> {
            Intent intent = new Intent(EditProfile.this, EditUserName.class);
            startActivityForResult(intent, USERNAME_REQUEST_CODE);
        };
        usernameValue.setOnClickListener(editUsernameListener);
        findViewById(R.id.username_edit_icon).setOnClickListener(editUsernameListener);

        // Edit bio click listener
        View.OnClickListener editBioListener = v -> {
            Intent intent = new Intent(EditProfile.this, EditBio.class);
            startActivityForResult(intent, BIO_REQUEST_CODE);
        };
        bioValue.setOnClickListener(editBioListener);
        findViewById(R.id.bio_edit_icon).setOnClickListener(editBioListener);

        // Save button click listener
        saveButton.setOnClickListener(v -> checkAndSaveProfileData());
    }

    // Load existing profile data from Firebase and populate fields
    private void loadProfileData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);
                    String bio = snapshot.child("bio").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    nameValue.setText(name != null ? name : "");
                    usernameValue.setText(username != null ? username : "");
                    bioValue.setText(bio != null ? bio : "");

                    // Use Glide to load the profile image if the URL exists
                    if (profileImageUrl != null) {
                        Glide.with(EditProfile.this).load(profileImageUrl).into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditProfile.this, "Failed to load profile data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Handle activity result from EditName, EditUsername, EditBio, and image picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
                uploadProfileImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == NAME_REQUEST_CODE && data != null) {
                String newName = data.getStringExtra("newName");
                nameValue.setText(newName); // Update name in EditProfile
            } else if (requestCode == USERNAME_REQUEST_CODE && data != null) {
                String newUsername = data.getStringExtra("newUsername");
                usernameValue.setText(newUsername); // Update username in EditProfile
            } else if (requestCode == BIO_REQUEST_CODE && data != null) {
                String newBio = data.getStringExtra("newBio");
                bioValue.setText(newBio); // Update bio in EditProfile
            }
        }
    }

    // Upload the selected profile image to Firebase Storage and update the URL in Realtime Database
    private void uploadProfileImage() {
        if (imageUri != null) {
            StorageReference fileRef = storageReference.child(currentUser.getUid() + ".jpg");  // Use logged-in user's ID
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        userRef.child("profileImageUrl").setValue(uri.toString());
                        Glide.with(EditProfile.this).load(uri.toString()).into(profileImage); // Load new image in UI
                        Toast.makeText(EditProfile.this, "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                    }))
                    .addOnFailureListener(e -> Toast.makeText(EditProfile.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
        }
    }

    // Check if the username is unique before saving profile data
    private void checkAndSaveProfileData() {
        String username = usernameValue.getText().toString().trim();
        String name = nameValue.getText().toString().trim();
        String bio = bioValue.getText().toString().trim();

        if (username.isEmpty() || name.isEmpty() || bio.isEmpty()) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the username is unique
        usernameCheckRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !dataSnapshot.getValue(String.class).equals(currentUser.getUid())) {
                    // Username is taken by another user
                    Toast.makeText(EditProfile.this, "Username is already taken", Toast.LENGTH_SHORT).show();
                } else {
                    saveProfileData(name, username, bio);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(EditProfile.this, "Error checking username", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Save profile data to Firebase
    private void saveProfileData(String name, String username, String bio) {
        userRef.child("name").setValue(name);
        userRef.child("username").setValue(username);
        userRef.child("bio").setValue(bio).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                usernameCheckRef.child(username).setValue(currentUser.getUid()); // Save the username to check uniqueness
                Toast.makeText(EditProfile.this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EditProfile.this, "Failed to save profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
