package com.example.recipeapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileYourRecipesFragment extends Fragment {

    private TextView noUploadedRecipesMessage;
    private Button createRecipeButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_your_recipes, container, false);

        // Get references to UI elements
        noUploadedRecipesMessage = view.findViewById(R.id.no_uploaded_recipes);
        createRecipeButton = view.findViewById(R.id.btnCreateRecipe);

        // Simulate whether the user has uploaded recipes or not
        boolean hasUploadedRecipes = false;

        // Show or hide the "No Recipes Yet" message
        if (hasUploadedRecipes) {
            noUploadedRecipesMessage.setVisibility(View.GONE);
            createRecipeButton.setVisibility(View.GONE);
        } else {
            noUploadedRecipesMessage.setVisibility(View.VISIBLE);
            createRecipeButton.setVisibility(View.VISIBLE);
        }

        // Set click listener for the "Create Recipe" button
        createRecipeButton.setOnClickListener(v -> {
            // Handle creating a new recipe (e.g., navigate to recipe creation screen)
        });

        return view;
    }
}
