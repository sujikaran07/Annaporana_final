package com.example.recipeapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView foodRecyclerView;
    private FoodAdapter foodAdapter;
    private List<Recipe> recipeList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        TextView greetingText = view.findViewById(R.id.greetingText);
        foodRecyclerView = view.findViewById(R.id.foodRecyclerView);

        // Set GridLayoutManager for displaying 2 recipes per row
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        foodRecyclerView.setLayoutManager(gridLayoutManager);

        // Initialize the adapter
        foodAdapter = new FoodAdapter(recipeList);
        foodRecyclerView.setAdapter(foodAdapter);

        // Fetch username and update greeting text
        fetchUsername(greetingText);

        // Fetch and display recipes
        fetchRecipes();

        // Set button click listeners for navigating to ItemsFragment with a category
        setButtonListeners(view);
    }

    private void fetchUsername(TextView greetingText) {
        // Get current user ID from FirebaseAuth
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch the username from Firestore
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        greetingText.setText("Hello " + username + "!");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("FirestoreError", "Error fetching username", e);
                });
    }

    private void fetchRecipes() {
        // Reference to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch recipes from Firestore
        db.collection("recipes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recipeList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipeList.add(recipe);
                    }
                    // Notify the adapter about data changes
                    foodAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.w("FirestoreError", "Error fetching recipes", e);
                });
    }

    private void setButtonListeners(View view) {
        Button btnPopular = view.findViewById(R.id.btnPopular);
        Button btnRice = view.findViewById(R.id.btnRice);
        Button btnCurries = view.findViewById(R.id.btnCurries);
        Button btnGravy = view.findViewById(R.id.btnGravy);
        Button btnSideDish = view.findViewById(R.id.btnSideDish);
        Button btnDessert = view.findViewById(R.id.btnDessert);
        Button btnFestival = view.findViewById(R.id.btnFestival);
        Button btnJuice = view.findViewById(R.id.btnJuice);
        Button btnSoup = view.findViewById(R.id.btnSoup);

        btnPopular.setOnClickListener(v -> navigateToItemsFragment("Popular"));
        btnRice.setOnClickListener(v -> navigateToItemsFragment("Rice"));
        btnCurries.setOnClickListener(v -> navigateToItemsFragment("Curries"));
        btnGravy.setOnClickListener(v -> navigateToItemsFragment("Gravy"));
        btnSideDish.setOnClickListener(v -> navigateToItemsFragment("SideDish"));
        btnDessert.setOnClickListener(v -> navigateToItemsFragment("Dessert"));
        btnFestival.setOnClickListener(v -> navigateToItemsFragment("Festival"));
        btnJuice.setOnClickListener(v -> navigateToItemsFragment("Juice"));
        btnSoup.setOnClickListener(v -> navigateToItemsFragment("Soup"));
    }

    private void navigateToItemsFragment(String category) {
        Bundle bundle = new Bundle();
        bundle.putString("category", category);

        // Navigate to ItemsFragment with the selected category
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_homeFragment_to_itemsFragment, bundle);  // Make sure this ID exists in your nav_graph.xml
    }
}
