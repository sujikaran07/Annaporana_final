package com.example.recipeapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;
import androidx.appcompat.app.AlertDialog;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private TextView userNameTextView, noRecipesTextView, followersCountTextView;
    private ImageView profileImageView;
    private DatabaseReference usersRef, recipesRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private ImageButton settingsButton, editProfileButton;
    private Button savedRecipesButton, yourRecipesButton;
    private RecyclerView recyclerView;
    private ProfileRecipeAdapter profileRecipeAdapter;
    private List<ProfileRecipe> recipeList;
    private EditText searchEditText;

    private boolean isSavedRecipesSelected = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize FirebaseAuth instance and get current user
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Initialize Firebase database references
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        recipesRef = FirebaseDatabase.getInstance().getReference("recipes");

        // Get references to UI elements
        userNameTextView = view.findViewById(R.id.user_name);
        profileImageView = view.findViewById(R.id.profile_image);
        settingsButton = view.findViewById(R.id.settings_button);
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        savedRecipesButton = view.findViewById(R.id.saved_recipes_button);
        yourRecipesButton = view.findViewById(R.id.your_recipes_button);
        recyclerView = view.findViewById(R.id.recycler_recipes);
        searchEditText = view.findViewById(R.id.search_recipes);
        noRecipesTextView = view.findViewById(R.id.no_recipes_text_view);
        followersCountTextView = view.findViewById(R.id.followers_count);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recipeList = new ArrayList<>();
        profileRecipeAdapter = new ProfileRecipeAdapter(recipeList, this::openRecipeDetails, this::showDeleteConfirmation);
        recyclerView.setAdapter(profileRecipeAdapter);

        // Fetch and display user data
        fetchUserData();

        // Set click listener for settings button
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PageSettings.class);
            startActivity(intent);
        });

        // Set click listener for edit profile button
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfile.class);
            startActivity(intent);
        });

        // Default to show saved recipes section
        selectSavedRecipesTab();

        // Set click listener for saved recipes button
        savedRecipesButton.setOnClickListener(v -> {
            selectSavedRecipesTab();
        });

        // Set click listener for your recipes button
        yourRecipesButton.setOnClickListener(v -> {
            selectYourRecipesTab();
        });

        // Set up search functionality
        setupSearchFunctionality();

        return view;
    }

    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRecipes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterRecipes(String query) {
        List<ProfileRecipe> filteredList = new ArrayList<>();
        for (ProfileRecipe recipe : recipeList) {
            if (recipe.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(recipe);
            }
        }
        profileRecipeAdapter.updateRecipes(filteredList);
        updateEmptyState(filteredList);
    }

    private void selectSavedRecipesTab() {
        isSavedRecipesSelected = true;
        updateTabSelection();
        loadSavedRecipes();
    }

    private void selectYourRecipesTab() {
        isSavedRecipesSelected = false;
        updateTabSelection();
        loadYourUploadedRecipes();
    }

    private void updateTabSelection() {
        if (isSavedRecipesSelected) {
            savedRecipesButton.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
            savedRecipesButton.setBackgroundResource(R.drawable.underline_green);
            yourRecipesButton.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            yourRecipesButton.setBackground(null);
        } else {
            yourRecipesButton.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
            yourRecipesButton.setBackgroundResource(R.drawable.underline_green);
            savedRecipesButton.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            savedRecipesButton.setBackground(null);
        }

        searchEditText.setText("");
        searchEditText.clearFocus();
    }

    private void loadSavedRecipes() {
        String userId = currentUser.getUid();
        DatabaseReference savedRecipesRef = usersRef.child(userId).child("bookmarkedRecipes");

        savedRecipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                        String recipeId = recipeSnapshot.getKey();
                        loadRecipeById(recipeId);
                    }
                } else {
                    updateEmptyState(recipeList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateEmptyState(recipeList);
            }
        });
    }

    private void loadYourUploadedRecipes() {
        String userId = currentUser.getUid();
        recipesRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                        ProfileRecipe recipe = recipeSnapshot.getValue(ProfileRecipe.class);
                        if (recipe != null) {
                            recipeList.add(recipe);
                        }
                    }
                }
                profileRecipeAdapter.updateRecipes(recipeList);
                updateEmptyState(recipeList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateEmptyState(recipeList);
            }
        });
    }

    private void loadRecipeById(String recipeId) {
        recipesRef.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ProfileRecipe recipe = snapshot.getValue(ProfileRecipe.class);
                if (recipe != null) {
                    recipeList.add(recipe);
                    profileRecipeAdapter.updateRecipes(recipeList);
                }
                updateEmptyState(recipeList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateEmptyState(recipeList);
            }
        });
    }

    private void fetchUserData() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            usersRef.child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("username").getValue(String.class);
                        String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                        int followersCount = snapshot.child("followersCount").getValue(Integer.class);

                        userNameTextView.setText(name != null ? name : "Buddy");
                        followersCountTextView.setText(String.valueOf(followersCount));

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Picasso.get().load(profileImageUrl).into(profileImageView);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        } else {
            userNameTextView.setText("Buddy");
        }
    }

    private void updateEmptyState(List<ProfileRecipe> recipeList) {
        if (recipeList.isEmpty()) {
            noRecipesTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noRecipesTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void openRecipeDetails(ProfileRecipe recipe) {
        RecipeDetailedFragment detailedFragment = RecipeDetailedFragment.newInstance(recipe.getTitle(), recipe.getId(), currentUser.getUid());
        getParentFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, detailedFragment)
                .addToBackStack(null)
                .commit();
    }

    private void showDeleteConfirmation(ProfileRecipe recipe) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Recipe")
                .setMessage("Are you sure you want to delete this recipe?")
                .setPositiveButton("Delete", (dialog, which) -> deleteRecipe(recipe))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteRecipe(ProfileRecipe recipe) {
        DatabaseReference recipeRef = recipesRef.child(recipe.getId());
        recipeRef.removeValue().addOnSuccessListener(aVoid -> {
            recipeList.remove(recipe);
            profileRecipeAdapter.updateRecipes(recipeList);
            Toast.makeText(getContext(), "Recipe deleted", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to delete recipe", Toast.LENGTH_SHORT).show();
        });
    }
}