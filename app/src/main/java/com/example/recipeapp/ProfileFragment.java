package com.example.recipeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Find the settings button
        ImageButton settingsButton = view.findViewById(R.id.settings_button);

        // Set the click listener for the settings button
        settingsButton.setOnClickListener(v -> {
            // Open the PageSettings activity
            Intent intent = new Intent(getActivity(), PageSettings.class);
            startActivity(intent);
        });

        return view;
    }
}
