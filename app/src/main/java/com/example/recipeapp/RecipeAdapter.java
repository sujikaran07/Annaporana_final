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
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipeList;
    private Context context;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(String recipeTitle);
    }

    public RecipeAdapter(Context context, List<Recipe> recipeList, OnRecipeClickListener listener) {
        this.context = context;
        this.recipeList = recipeList;
        this.listener = listener;
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

        // Bind the title to the TextView
        holder.titleTextView.setText(recipe.getTitle());

        // Load image using Glide
        Glide.with(context)
                .load(recipe.getImageUrl())
                .apply(new RequestOptions().placeholder(R.drawable.ic_placeholder_image).error(R.drawable.ic_error_image))
                .into(holder.recipeImageView);

        // Set the click listener for the item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe.getTitle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

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
