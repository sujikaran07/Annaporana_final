package com.example.recipeapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class UserRecipeAdapter extends RecyclerView.Adapter<UserRecipeAdapter.RecipeViewHolder> {

    private List<UserRecipe> recipes;

    public UserRecipeAdapter(List<UserRecipe> recipes) {
        this.recipes = recipes;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        UserRecipe recipe = recipes.get(position);

        // Set recipe title
        holder.recipeNameTextView.setText(recipe.getTitle());

        // Set recipe description
        holder.recipeDescriptionTextView.setText(recipe.getDescription());

        // Load the recipe image using Glide
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .into(holder.recipeImageView);
        } else {
            holder.recipeImageView.setImageResource(R.drawable.ic_placeholder_image);
        }
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView recipeNameTextView;
        ImageView recipeImageView;
        TextView recipeDescriptionTextView;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeNameTextView = itemView.findViewById(R.id.recipe_name);
            recipeImageView = itemView.findViewById(R.id.recipe_image);
            recipeDescriptionTextView = itemView.findViewById(R.id.recipe_description);
        }
    }
}
