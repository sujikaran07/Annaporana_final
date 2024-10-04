package com.example.recipeapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
            Log.d("AddFragment", "Button 'New Recipe' found successfully.");
            // Set an OnClickListener to navigate to the RecipeUploadPage activity
            newRecipeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("AddFragment", "New Recipe button clicked.");
                    // Start the RecipeUploadPage activity when the button is clicked
                    Intent intent = new Intent(getActivity(), RecipeUploadPage.class);
                    startActivity(intent);
                }
            });
        } else {
            Log.e("AddFragment", "Error: 'button_recipe' not found in the layout. Check fragment_add.xml");
        }

        return view;
    }
}
