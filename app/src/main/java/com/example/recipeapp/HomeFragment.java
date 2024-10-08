package com.example.recipeapp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);

        // Enable fixed size for better performance
        recyclerView.setHasFixedSize(true);

        // Use a StaggeredGridLayoutManager for Pinterest-style grid
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        recipeList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(getContext(), recipeList);
        recyclerView.setAdapter(recipeAdapter);

        loadRecipes(); // Load all the recipes from the Firebase Realtime Database

        return view;
    }

    private void loadRecipes() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("recipes");

        // Ensure Firebase is synced for offline capabilities
        databaseReference.keepSynced(true);

        // Fetch all recipes from the database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear(); // Clear the list to avoid duplications

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    // Check if user has a "recipes" node
                    DataSnapshot userRecipesSnapshot = userSnapshot.child("recipes");
                    if (userRecipesSnapshot.exists()) {
                        for (DataSnapshot recipeSnapshot : userRecipesSnapshot.getChildren()) {
                            String title = recipeSnapshot.child("title").getValue(String.class);
                            String imageUrl = recipeSnapshot.child("imageUrl").getValue(String.class);

                            if (title != null && imageUrl != null) {
                                Recipe recipe = new Recipe(title, imageUrl);
                                recipeList.add(recipe);
                            }
                        }
                    }
                }



                Collections.sort(recipeList, new Comparator<Recipe>() {
                    @Override
                    public int compare(Recipe r1, Recipe r2) {
                        return r1.getTitle().compareToIgnoreCase(r2.getTitle());
                    }
                });


                recipeAdapter.notifyDataSetChanged(); // Notify the adapter that the data has changed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error, optionally log or show a message
            }
        });
    }
}
