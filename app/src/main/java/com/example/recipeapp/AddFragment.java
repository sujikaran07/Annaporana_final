package com.example.recipeapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class AddFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        // Find the "New Recipe" button
        Button newRecipeButton = view.findViewById(R.id.button_recipe);

        // Check if the button exists and handle accordingly
        if (newRecipeButton != null) {
            Log.d("FragmentAdd", "Button 'New Recipe' found successfully.");
            // Set an OnClickListener to navigate to the RecipeUploadFragment
            newRecipeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("FragmentAdd", "New Recipe button clicked.");
                    // Load the RecipeUploadFragment when the button is clicked
                    loadFragment(new RecipeUploadFragment());
                }
            });
        } else {
            Log.e("FragmentAdd", "Error: 'button_recipe' not found in the layout. Check fragment_add.xml");
        }

        return view;
    }

    // Method to load a fragment
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // Ensure your activity contains a FrameLayout with the id 'frame_layout'
        transaction.replace(R.id.frame_layout, fragment);
        transaction.addToBackStack(null);  // This allows the user to navigate back
        transaction.commit();
    }
}
