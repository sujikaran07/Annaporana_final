package com.example.recipeapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RecipeDetailedFragment extends Fragment {

    private static final String ARG_RECIPE_TITLE = "recipe_title";
    private static final String ARG_RECIPE_ID = "recipe_id";
    private static final String ARG_USER_ID = "user_id";
    private String recipeTitle;
    private String recipeId;
    private String userId;
    private DatabaseReference databaseReference;
    private boolean isLiked = false;
    private boolean isBookmarked = false;

    public static RecipeDetailedFragment newInstance(String recipeTitle, String recipeId, String userId) {
        RecipeDetailedFragment fragment = new RecipeDetailedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RECIPE_TITLE, recipeTitle);
        args.putString(ARG_RECIPE_ID, recipeId);
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_detailed, container, false);

        if (getArguments() != null) {
            recipeTitle = getArguments().getString(ARG_RECIPE_TITLE);
            recipeId = getArguments().getString(ARG_RECIPE_ID);
            userId = getArguments().getString(ARG_USER_ID);
        }

        TextView recipeNameTextView = view.findViewById(R.id.recipe_name);
        recipeNameTextView.setText(recipeTitle);

        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        ImageButton likeButton = view.findViewById(R.id.like_button);
        checkIfLiked(userId, likeButton);
        likeButton.setOnClickListener(v -> {
            if (isLiked) {
                unlikeRecipe(userId, likeButton);
            } else {
                likeRecipe(userId, likeButton);
            }
        });

        ImageButton ratingButton = view.findViewById(R.id.rate_button);
        ratingButton.setOnClickListener(v -> rateRecipe(ratingButton));

        ImageButton shareButton = view.findViewById(R.id.share_button);
        shareButton.setOnClickListener(this::shareRecipe);

        ImageButton bookmarkButton = view.findViewById(R.id.bookmark_button);
        checkIfBookmarked(userId, bookmarkButton);
        bookmarkButton.setOnClickListener(v -> {
            if (isBookmarked) {
                unbookmarkRecipe(userId, bookmarkButton);
            } else {
                bookmarkRecipe(userId, bookmarkButton);
            }
        });

        ImageView userProfileImageView = view.findViewById(R.id.user_profile_image);
        userProfileImageView.setOnClickListener(v -> navigateToUserProfile(userId));

        loadRecipeDetails(recipeTitle, userId, view);

        return view;
    }

    private void navigateToUserProfile(String userId) {
        if (userId != null && !userId.isEmpty()) {
            Log.d("RecipeDetailedFragment", "Navigating to user profile with userId: " + userId);
            Intent intent = new Intent(getActivity(), UserProfileActivity.class);
            intent.putExtra("uploader_user_id", userId); // Correct key for UserProfileActivity
            startActivity(intent);
        } else {
            Log.e("RecipeDetailedFragment", "userId is null or empty");
            Toast.makeText(getContext(), "User ID not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void rateRecipe(ImageButton ratingButton) {
        Intent intent = new Intent(getContext(), ReviewPage.class);
        intent.putExtra("recipeId", recipeId);
        startActivity(intent);
        ratingButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.myapp_button_color2));
    }

    private void shareRecipe(View view) {
        String shareText = "Check out this recipe: " + recipeTitle + "!";
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, "Share this recipe via");
        startActivity(shareIntent);
    }

    private void bookmarkRecipe(String userId, ImageButton bookmarkButton) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("bookmarkedRecipes");
        userRef.child(recipeId).setValue(true).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Recipe bookmarked!", Toast.LENGTH_SHORT).show();
            isBookmarked = true;
            bookmarkButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.myapp_button_color));
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to bookmark recipe", Toast.LENGTH_SHORT).show();
            Log.e("RecipeDetailedFragment", "Failed to bookmark recipe: " + e.getMessage());
        });
    }

    private void unbookmarkRecipe(String userId, ImageButton bookmarkButton) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("bookmarkedRecipes");
        userRef.child(recipeId).removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Recipe unbookmarked!", Toast.LENGTH_SHORT).show();
            isBookmarked = false;
            bookmarkButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white));
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to unbookmark recipe", Toast.LENGTH_SHORT).show();
            Log.e("RecipeDetailedFragment", "Failed to unbookmark recipe: " + e.getMessage());
        });
    }

    private void checkIfBookmarked(String userId, ImageButton bookmarkButton) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("bookmarkedRecipes").child(recipeId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    isBookmarked = true;
                    bookmarkButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.myapp_button_color));
                } else {
                    isBookmarked = false;
                    bookmarkButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RecipeDetailedFragment", "Error checking bookmark status: " + error.getMessage());
            }
        });
    }

    private void loadRecipeDetails(String recipeTitle, String userId, View view) {
        databaseReference = FirebaseDatabase.getInstance().getReference("recipes").child(userId).child("recipes");
        databaseReference.orderByChild("title").equalTo(recipeTitle)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                                String imageUrl = recipeSnapshot.child("imageUrl").getValue(String.class);
                                String time = recipeSnapshot.child("cookTime").getValue(String.class);
                                String serves = recipeSnapshot.child("serves").getValue(String.class);
                                String calories = recipeSnapshot.child("nutrition").getValue(String.class);
                                String methods = recipeSnapshot.child("methods").getValue(String.class);
                                String ingredients = recipeSnapshot.child("ingredients").getValue(String.class);
                                String videoUrl = recipeSnapshot.child("videoUrl").getValue(String.class);

                                updateRecipeDetails(view, imageUrl, time, serves, calories, methods, ingredients, videoUrl);
                                fetchUserInfo(userId, view);
                                break;
                            }
                        } else {
                            Log.e("RecipeDetailedFragment", "Recipe not found");
                            Toast.makeText(getContext(), "Recipe not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("RecipeDetailedFragment", "Error loading recipe details: " + error.getMessage());
                        Toast.makeText(getContext(), "Error loading recipe details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateRecipeDetails(View view, String imageUrl, String time, String serves, String calories, String methods, String ingredients, String videoUrl) {
        TextView methodsTextView = view.findViewById(R.id.methods_input);
        methodsTextView.setText(methods != null ? methods : "No methods available");

        TextView ingredientsTextView = view.findViewById(R.id.ingredients_input);
        ingredientsTextView.setText(ingredients != null ? ingredients : "No ingredients available");

        VideoView videoView = view.findViewById(R.id.curry_video);
        if (videoUrl != null && !videoUrl.isEmpty()) {
            Uri videoUri = Uri.parse(videoUrl);
            videoView.setVideoURI(videoUri);
            MediaController mediaController = new MediaController(getContext());
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            videoView.start();
        }

        ImageView recipeImageView = view.findViewById(R.id.recipe_image);
        if (recipeImageView != null && imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_error_image)
                    .into(recipeImageView);
        }
    }

    private void fetchUserInfo(String userId, View view) {
        TextView userNameTextView = view.findViewById(R.id.user_name);
        TextView userHandleTextView = view.findViewById(R.id.user_handle);
        ImageView userProfileImageView = view.findViewById(R.id.user_profile_image);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.child("name").getValue(String.class);
                    String userHandle = snapshot.child("username").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    userNameTextView.setText(userName);
                    userHandleTextView.setText("@" + userHandle);

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ic_profile)
                                .circleCrop()
                                .into(userProfileImageView);
                    } else {
                        userProfileImageView.setImageResource(R.drawable.ic_profile);
                    }
                } else {
                    userNameTextView.setText("Unknown User");
                    userHandleTextView.setText("@unknown");
                    userProfileImageView.setImageResource(R.drawable.ic_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userNameTextView.setText("Error loading user");
                userHandleTextView.setText("@error");
                userProfileImageView.setImageResource(R.drawable.ic_profile);
                Log.e("RecipeDetailedFragment", "Error fetching user info: " + error.getMessage());
            }
        });
    }

    private void likeRecipe(String userId, ImageButton likeButton) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("likes").child(recipeId);
        userRef.setValue(true).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Liked!", Toast.LENGTH_SHORT).show();
            isLiked = true;
            likeButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.myapp_button_color));
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to like recipe", Toast.LENGTH_SHORT).show();
            Log.e("RecipeDetailedFragment", "Failed to like recipe: " + e.getMessage());
        });
    }

    private void unlikeRecipe(String userId, ImageButton likeButton) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("likes").child(recipeId);
        userRef.removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Unliked!", Toast.LENGTH_SHORT).show();
            isLiked = false;
            likeButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white));
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to unlike recipe", Toast.LENGTH_SHORT). show();
            Log.e("RecipeDetailedFragment", "Failed to unlike recipe: " + e.getMessage());
        });
    }

    private void checkIfLiked(String userId, ImageButton likeButton) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("likes").child(recipeId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    isLiked = true;
                    likeButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.myapp_button_color));
                } else {
                    isLiked = false;
                    likeButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RecipeDetailedFragment", "Error checking like status: " + error.getMessage());
            }
        });
    }
}