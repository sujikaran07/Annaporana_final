package com.example.recipeapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";
    private String uploaderUserId;

    private ImageView profileImageView;
    private TextView userNameTextView;
    private TextView userHandleTextView;
    private TextView followersCountTextView;
    private TextView followingCountTextView;
    private TextView likesCountTextView;
    private Button followButton;
    private RecyclerView recipesRecyclerView;

    private DatabaseReference userDatabaseRef;

    // List to hold user's recipes
    private List<UserRecipe> userRecipes = new ArrayList<>();
    private UserRecipeAdapter recipeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        // Retrieve uploaderUserId from Intent
        uploaderUserId = getIntent().getStringExtra("uploader_user_id");
        if (uploaderUserId == null || uploaderUserId.isEmpty()) {
            Log.e(TAG, "No uploaderUserId passed to UserProfileActivity");
            Toast.makeText(this, "User ID not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Views
        ImageButton backButton = findViewById(R.id.back_button);
        profileImageView = findViewById(R.id.profile_image);
        userNameTextView = findViewById(R.id.user_name);
        userHandleTextView = findViewById(R.id.user_handle);
        followersCountTextView = findViewById(R.id.followers_count);
        followingCountTextView = findViewById(R.id.following_count);
        likesCountTextView = findViewById(R.id.likes_count);
        followButton = findViewById(R.id.follow_button);
        recipesRecyclerView = findViewById(R.id.recipes_recycler_view);

        // Set up RecyclerView
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipeAdapter = new UserRecipeAdapter(userRecipes);
        recipesRecyclerView.setAdapter(recipeAdapter);

        // Back Button Functionality
        backButton.setOnClickListener(v -> onBackPressed());

        // Set up Firebase reference
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child(uploaderUserId);

        // Load user data from Firebase
        loadUserProfile();
        setupFollowButton();
        loadUserRecipes();
    }

    private void loadUserProfile() {
        // Add listener for real-time updates
        userDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve and display user information
                    String userName = snapshot.child("name").getValue(String.class);
                    String userHandle = snapshot.child("username").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    Long followersCount = snapshot.child("followersCount").getValue(Long.class);
                    Long followingCount = snapshot.child("followingCount").getValue(Long.class);
                    Long likesCount = snapshot.child("likesCount").getValue(Long.class);

                    userNameTextView.setText(userName != null ? userName : "User");
                    userHandleTextView.setText(userHandle != null ? "@" + userHandle : "@unknown");
                    followersCountTextView.setText(followersCount != null ? String.valueOf(followersCount) : "0");
                    followingCountTextView.setText(followingCount != null ? String.valueOf(followingCount) : "0");
                    likesCountTextView.setText(likesCount != null ? String.valueOf(likesCount) : "0");

                    // Load profile image using Glide
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(UserProfileActivity.this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ic_profile)
                                .circleCrop()
                                .into(profileImageView);
                    } else {
                        profileImageView.setImageResource(R.drawable.ic_profile);
                    }
                } else {
                    Log.e(TAG, "User not found");
                    Toast.makeText(UserProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading user profile: " + error.getMessage());
                Toast.makeText(UserProfileActivity.this, "Error loading user profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFollowButton() {
        DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("users").child(uploaderUserId).child("followers");
        String currentUserId = "logged_in_user_id"; // Replace with actual current user ID logic

        // Check if user is already following
        followersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    followButton.setText("Unfollow");
                } else {
                    followButton.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking follow status: " + error.getMessage());
            }
        });

        followButton.setOnClickListener(v -> {
            if (followButton.getText().equals("Follow")) {
                followersRef.child(currentUserId).setValue(true).addOnSuccessListener(aVoid -> {
                    followButton.setText("Unfollow");
                    incrementFollowersCount();
                    Toast.makeText(UserProfileActivity.this, "Followed!", Toast.LENGTH_SHORT).show();
                });
            } else {
                followersRef.child(currentUserId).removeValue().addOnSuccessListener(aVoid -> {
                    followButton.setText("Follow");
                    decrementFollowersCount();
                    Toast.makeText(UserProfileActivity.this, "Unfollowed!", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void incrementFollowersCount() {
        userDatabaseRef.child("followersCount").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Long count = mutableData.getValue(Long.class);
                if (count == null) {
                    count = 0L;
                }
                mutableData.setValue(count + 1);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.e(TAG, "Error incrementing followers count: " + error.getMessage());
                }
            }
        });
    }

    private void decrementFollowersCount() {
        userDatabaseRef.child("followersCount").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Long count = mutableData.getValue(Long.class);
                if (count != null && count > 0) {
                    mutableData.setValue(count - 1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.e(TAG, "Error decrementing followers count: " + error.getMessage());
                }
            }
        });
    }

    private void loadUserRecipes() {
        DatabaseReference recipesRef = userDatabaseRef.child("recipes");

        // Add a listener for real-time updates
        recipesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userRecipes.clear();
                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    UserRecipe recipe = recipeSnapshot.getValue(UserRecipe.class);
                    if (recipe != null) {
                        userRecipes.add(recipe);
                    }
                }
                recipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading uploader recipes: " + error.getMessage());
                Toast.makeText(UserProfileActivity.this, "Error loading uploader recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
