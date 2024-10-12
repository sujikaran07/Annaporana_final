package com.example.recipeapp;

public class ProfileRecipe {
    private String id;
    private String title;
    private String description;
    private String imageUrl;

    // Default constructor (required for Firebase)
    public ProfileRecipe() {
    }

    // Constructor to initialize all fields
    public ProfileRecipe(String id, String title, String description, String imageUrl, String category, String cookTime, String serves) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // Getter and setter for id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter and setter for title
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Getter and setter for description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Getter and setter for imageUrl
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }




}
