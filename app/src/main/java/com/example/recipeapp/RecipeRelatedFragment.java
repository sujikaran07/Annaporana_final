package com.example.recipeapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class RecipeRelatedFragment extends Fragment implements RelatedRecipeAdapter.OnRecipeClickListener {

    private static final String ARG_RECIPE_TITLE = "recipe_title";
    private String recipeTitle;
    private RecyclerView recyclerView;
    private RelatedRecipeAdapter relatedRecipeAdapter;
    private List<RelatedRecipe> relatedRecipeList;
    private DatabaseReference databaseReference;
    private TextView relatedRecipeTitleText;
    private ImageButton backButton;

    public static RecipeRelatedFragment newInstance(String recipeTitle) {
        RecipeRelatedFragment fragment = new RecipeRelatedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RECIPE_TITLE, recipeTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recipeTitle = getArguments().getString(ARG_RECIPE_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_related, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.related_recipes_recycler_view);
        relatedRecipeTitleText = view.findViewById(R.id.recipe_title_text_view);
        backButton = view.findViewById(R.id.backButton);

        // Set the title based on the recipe clicked
        relatedRecipeTitleText.setText(recipeTitle);

        // Set up RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        // Initialize recipe list and adapter
        relatedRecipeList = new ArrayList<>();
        relatedRecipeAdapter = new RelatedRecipeAdapter(getContext(), relatedRecipeList, this);
        recyclerView.setAdapter(relatedRecipeAdapter);

        // Firebase Database reference to the "recipes" node
        databaseReference = FirebaseDatabase.getInstance().getReference("recipes");

        // Load related recipes based on the selected title
        loadRelatedRecipes();

        // Set up back button functionality
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    // Load related recipes based on the recipe title keywords
    private void loadRelatedRecipes() {
        String[] keywords = recipeTitle.split(" "); // Split recipe title into individual keywords

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                relatedRecipeList.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    DataSnapshot userRecipesSnapshot = userSnapshot.child("recipes");
                    if (userRecipesSnapshot.exists()) {
                        for (DataSnapshot recipeSnapshot : userRecipesSnapshot.getChildren()) {
                            String title = recipeSnapshot.child("title").getValue(String.class);
                            String imageUrl = recipeSnapshot.child("imageUrl").getValue(String.class);
                            String time = recipeSnapshot.child("cookTime").getValue(String.class);
                            String serves = recipeSnapshot.child("serves").getValue(String.class);
                            String calories = recipeSnapshot.child("nutrition").getValue(String.class);
                            String userId = recipeSnapshot.child("userId").getValue(String.class);  // Get userId

                            // Check if any keyword matches in the recipe title
                            if (title != null && containsAnyKeyword(title, keywords)) {
                                // Add the related recipe to the list
                                RelatedRecipe relatedRecipe = new RelatedRecipe(title, imageUrl, "4.5", time, serves, calories, userId);
                                relatedRecipeList.add(relatedRecipe);
                            }
                        }
                    }
                }

                // Notify the adapter of changes
                relatedRecipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RecipeRelatedFragment", "Error loading related recipes: " + error.getMessage());
            }
        });
    }

    // Helper method to check if the recipe title contains any of the keywords
    private boolean containsAnyKeyword(String title, String[] keywords) {
        for (String keyword : keywords) {
            if (title.toLowerCase().contains(keyword.toLowerCase())) {
                return true; // Return true if any keyword matches
            }
        }
        return false; // Return false if no keyword matches
    }

    // Handle recipe click event
    @Override
    public void onRecipeClick(String recipeTitle) {
        // Handle recipe click, navigate or show more details
        // Example: Navigate to another fragment with full recipe details
    }
}
