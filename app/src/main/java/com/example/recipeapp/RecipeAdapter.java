package com.example.recipeapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipeList;
    private Context context;
    private static final String TAG = "RecipeAdapter";

    public RecipeAdapter(Context context, List<Recipe> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);

        // Bind the title to the TextView (always update this when binding view)
        if (recipe.getTitle() != null && !recipe.getTitle().isEmpty()) {
            holder.titleTextView.setText(recipe.getTitle());
        } else {
            holder.titleTextView.setText("No Title");
        }

        // Get the image URL from the recipe
        String imageUrl = recipe.getImageUrl();

        // Check if the image URL is valid and load the image
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_placeholder_image)  // Placeholder image
                            .error(R.drawable.ic_error_image)  // Error image
                            .diskCacheStrategy(DiskCacheStrategy.ALL))  // Cache images for faster loading
                    .into(holder.recipeImageView);
        } else {
            // If the image URL is null or empty, load the placeholder image and log a warning
            Log.w(TAG, "Image URL is null or empty for recipe: " + recipe.getTitle());
            holder.recipeImageView.setImageResource(R.drawable.ic_placeholder_image);
        }
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    // ViewHolder class to hold the views
    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView recipeImageView;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.recipe_title_text_view);
            recipeImageView = itemView.findViewById(R.id.recipe_image_view);
        }
    }
}
