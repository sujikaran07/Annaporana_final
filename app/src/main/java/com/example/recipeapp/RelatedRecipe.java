package com.example.recipeapp;

public class RelatedRecipe {

    private String title;
    private String imageUrl;
    private String rating;
    private String time;      // Store cook time as a string (e.g., "25 mins")
    private String serves;    // Store serves as a string (e.g., "4 people")
    private String calories;  // Store calories as a string (e.g., "360 kcal")
    private String userId;    // Store the userId of the recipe uploader
    private String recipeId;  // Store the recipe ID

    // No-argument constructor required by Firebase
    public RelatedRecipe() {
    }

    // Constructor to initialize all fields
    public RelatedRecipe(String title, String imageUrl, String rating, String time, String serves, String calories, String userId, String recipeId) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.time = time;
        this.serves = serves;
        this.calories = calories;
        this.userId = userId;
        this.recipeId = recipeId;
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

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getServes() {
        return serves;
    }

    public void setServes(String serves) {
        this.serves = serves;
    }

    public String getCalories() {
        return calories;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }
}
