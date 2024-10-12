package com.example.recipeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.*;
import java.util.List;

public class RelatedRecipeAdapter extends RecyclerView.Adapter<RelatedRecipeAdapter.RelatedRecipeViewHolder> {

    private Context context;
    private List<RelatedRecipe> relatedRecipes;
    private OnRecipeClickListener onRecipeClickListener;

    public RelatedRecipeAdapter(Context context, List<RelatedRecipe> relatedRecipes, OnRecipeClickListener listener) {
        this.context = context;
        this.relatedRecipes = relatedRecipes;
        this.onRecipeClickListener = listener;
    }

    @NonNull
    @Override
    public RelatedRecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_related_recipe, parent, false);
        return new RelatedRecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedRecipeViewHolder holder, int position) {
        RelatedRecipe recipe = relatedRecipes.get(position);

        // Set recipe data
        holder.titleTextView.setText(recipe.getTitle());
        holder.timeTextView.setText(recipe.getTime());
        holder.servesTextView.setText(recipe.getServes());
        holder.caloriesTextView.setText(recipe.getCalories());

        // Load image using Glide
        Glide.with(context)
                .load(recipe.getImageUrl())
                .apply(new RequestOptions().placeholder(R.drawable.ic_placeholder_image).error(R.drawable.ic_error_image))
                .into(holder.recipeImageView);

        // Fetch and display the username based on userId
        fetchUserName(recipe.getUserId(), holder.userNameTextView);

        // Fetch and display the rating for the recipe
        fetchRecipeRating(recipe.getRecipeId(), holder.ratingTextView);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (onRecipeClickListener != null) {
                // Pass recipeTitle, userId, and recipeId to the listener
                onRecipeClickListener.onRecipeClick(recipe.getTitle(), recipe.getUserId(), recipe.getRecipeId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return relatedRecipes.size();
    }

    // Helper method to fetch and display the username based on userId
    private void fetchUserName(String userId, TextView userNameTextView) {
        if (userId == null) {
            userNameTextView.setText("Developer");
            return; // Return early if userId is null (meaning uploaded by developer)
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child("username").getValue(String.class);
                if (userName != null) {
                    userNameTextView.setText(userName);
                } else {
                    userNameTextView.setText("Unknown User"); // Fallback if username is not found
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userNameTextView.setText("Unknown User"); // Fallback on error
            }
        });
    }

    // Helper method to fetch and display the recipe rating
    private void fetchRecipeRating(String recipeId, TextView ratingTextView) {
        if (recipeId == null) {
            ratingTextView.setText("0.0 (0)");
            return; // Return early if recipeId is null
        }

        // Reference to the reviews node inside the specific recipe
        DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId).child("reviews");
        recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double averageRating = snapshot.child("averageRating").getValue(Double.class);
                Long totalRatings = snapshot.child("totalRatings").getValue(Long.class);

                // Set the rating text with the format: "AverageRating (TotalRatings)"
                if (averageRating != null && totalRatings != null) {
                    ratingTextView.setText(String.format("%.1f (%d)", averageRating, totalRatings));
                } else {
                    ratingTextView.setText("0.0 (0)");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ratingTextView.setText("0.0 (0)");
            }
        });
    }

    public interface OnRecipeClickListener {
        void onRecipeClick(String recipeTitle, String userId, String recipeId);  // Pass recipeId as well
    }

    public static class RelatedRecipeViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, timeTextView, servesTextView, caloriesTextView, userNameTextView, ratingTextView;
        ImageView recipeImageView;

        public RelatedRecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.recipe_name);
            recipeImageView = itemView.findViewById(R.id.recipe_image);
            timeTextView = itemView.findViewById(R.id.recipe_time);
            servesTextView = itemView.findViewById(R.id.recipe_serves);
            caloriesTextView = itemView.findViewById(R.id.recipe_calories);
            userNameTextView = itemView.findViewById(R.id.user_name);
            ratingTextView = itemView.findViewById(R.id.recipe_rating_text_view); // Added rating field
        }
    }
}
