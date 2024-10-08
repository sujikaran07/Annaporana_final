package com.example.recipeapp;

public class Recipe {
    private String title;
    private String imageUrl;

    // No-argument constructor (required by Firebase)
    public Recipe() {
    }

    // Constructor to initialize title and imageUrl directly
    public Recipe(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    // Getters and setters for the title and imageUrl fields
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
}
