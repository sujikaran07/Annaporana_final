package com.example.recipeapp;

public class Recipe {
    private String title;
    private String imageUrl;
    private String category;
    private String recipeId;

    // No-argument constructor (required by Firebase)
    public Recipe() {}

    // Constructor to initialize relevant fields
    public Recipe(String title, String imageUrl, String category, String recipeId) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.category = category;
        this.recipeId = recipeId;
    }

    // Overloaded constructor for title and imageUrl only
    public Recipe(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    // Getters and setters for each field
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }
}
