package com.example.recipeapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment implements RecipeAdapter.OnRecipeClickListener {

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList;
    private EditText searchInput;
    private TextView userGreeting, popularTitle;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;
    private static final String TAG = "HomeFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView);
        searchInput = view.findViewById(R.id.search_recipes);
        userGreeting = view.findViewById(R.id.userGreeting);
        popularTitle = view.findViewById(R.id.popularTitle);

        // Initialize SharedPreferences
        sharedPreferences = getContext().getSharedPreferences("UserDetails", Context.MODE_PRIVATE);

        // Set up RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        // Initialize recipe list and adapter
        recipeList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(getContext(), recipeList, this);
        recyclerView.setAdapter(recipeAdapter);

        // Firebase Database reference to the "recipes" node
        databaseReference = FirebaseDatabase.getInstance().getReference("recipes");

        // Sync Firebase data for offline access
        databaseReference.keepSynced(true);

        // Load the user's greeting from SharedPreferences or Firebase
        loadUserGreeting();

        // Load recipes by default
        loadRecipes();

        // Set up search functionality
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchRecipes(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Set up category filtering
        setupCategoryFiltering(view);

        return view;
    }

    // Load user's greeting from SharedPreferences or Firebase
    private void loadUserGreeting() {
        String savedUsername = sharedPreferences.getString("username", null);
        if (savedUsername != null) {
            userGreeting.setText("Hello, " + savedUsername + "!");
        } else {
            fetchAndSaveUserNameFromFirebase();
        }
    }

    // Fetch username from Firebase and save it in SharedPreferences
    private void fetchAndSaveUserNameFromFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child("username").getValue(String.class);
                if (userName != null) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", userName);
                    editor.apply();
                    userGreeting.setText("Hello, " + userName + "!");
                } else {
                    userGreeting.setText("Hello, Buddy!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userGreeting.setText("Hello, Buddy!");
                Log.e(TAG, "Error loading user data: " + error.getMessage());
            }
        });
    }

    // Load all recipes
    private void loadRecipes() {
        popularTitle.setText("Popular Recipes");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
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

                // Shuffle the recipe list for random display order
                Collections.shuffle(recipeList);

                recipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading recipes: " + error.getMessage());
            }
        });
    }

    // Search recipes by title, ingredients, cook time, and serves
    private void searchRecipes(String query) {
        if (query.isEmpty()) {
            loadRecipes();
            return;
        }

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    DataSnapshot userRecipesSnapshot = userSnapshot.child("recipes");
                    if (userRecipesSnapshot.exists()) {
                        for (DataSnapshot recipeSnapshot : userRecipesSnapshot.getChildren()) {
                            String title = recipeSnapshot.child("title").getValue(String.class);
                            String imageUrl = recipeSnapshot.child("imageUrl").getValue(String.class);
                            String ingredients = recipeSnapshot.child("ingredients").getValue(String.class);
                            String cookTime = recipeSnapshot.child("cookTime").getValue(String.class);
                            String serves = recipeSnapshot.child("serves").getValue(String.class);

                            if ((title != null && title.toLowerCase().contains(query.toLowerCase())) ||
                                    (ingredients != null && ingredients.toLowerCase().contains(query.toLowerCase())) ||
                                    (cookTime != null && cookTime.toLowerCase().contains(query.toLowerCase())) ||
                                    (serves != null && serves.toLowerCase().contains(query.toLowerCase()))) {

                                Recipe recipe = new Recipe(title, imageUrl);
                                recipeList.add(recipe);
                            }
                        }
                    }
                }

                // Shuffle the filtered recipe list
                Collections.shuffle(recipeList);

                recipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error during search: " + error.getMessage());
            }
        });
    }

    // Set up category filtering functionality
    private void setupCategoryFiltering(View view) {
        LinearLayout popularCategory = view.findViewById(R.id.popularCategory);
        LinearLayout tiffinCategory = view.findViewById(R.id.tiffinCategory);
        LinearLayout mealsCategory = view.findViewById(R.id.mealsCategory);
        LinearLayout snacksCategory = view.findViewById(R.id.snacksCategory);
        LinearLayout drinksCategory = view.findViewById(R.id.drinksCategory);

        popularCategory.setOnClickListener(v -> loadRecipes());
        tiffinCategory.setOnClickListener(v -> filterByCategory("Tiffin"));
        mealsCategory.setOnClickListener(v -> filterByCategory("Meals"));
        snacksCategory.setOnClickListener(v -> filterByCategory("Snacks"));
        drinksCategory.setOnClickListener(v -> filterByCategory("Drinks"));
    }

    // Filter recipes by category (Tiffin, Meals, Snacks, Drinks)
    private void filterByCategory(String category) {
        popularTitle.setText(category + " Recipes");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    DataSnapshot userRecipesSnapshot = userSnapshot.child("recipes");
                    if (userRecipesSnapshot.exists()) {
                        for (DataSnapshot recipeSnapshot : userRecipesSnapshot.getChildren()) {
                            String title = recipeSnapshot.child("title").getValue(String.class);
                            String imageUrl = recipeSnapshot.child("imageUrl").getValue(String.class);
                            String recipeCategory = recipeSnapshot.child("category").getValue(String.class);

                            if (title != null && imageUrl != null && category.equals(recipeCategory)) {
                                Recipe recipe = new Recipe(title, imageUrl);
                                recipeList.add(recipe);
                            }
                        }
                    }
                }

                // Shuffle the filtered recipe list
                Collections.shuffle(recipeList);

                recipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error filtering by category: " + error.getMessage());
            }
        });
    }

    // Handle the click event for recipes in the RecipeAdapter
    @Override
    public void onRecipeClick(String recipeTitle) {
        RecipeRelatedFragment relatedRecipeFragment = RecipeRelatedFragment.newInstance(recipeTitle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, relatedRecipeFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
