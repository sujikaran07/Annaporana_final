package com.example.recipeapp;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<Recipe> recipeList;

    public FoodAdapter(List<Recipe> recipeList) {
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the correct layout (food_item_layout)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item_layout, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.recipeName.setText(recipe.getName());

        // Load video into VideoView using the video URL
        Uri videoUri = Uri.parse(recipe.getVideoUrl());  // No error now
        holder.recipeVideo.setVideoURI(videoUri);
        holder.recipeVideo.seekTo(1);  // Show the first frame of the video
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {

        TextView recipeName;
        VideoView recipeVideo;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            // Bind views from food_item_layout
            recipeName = itemView.findViewById(R.id.recipeName);
            recipeVideo = itemView.findViewById(R.id.recipeVideo);
        }
    }
}
