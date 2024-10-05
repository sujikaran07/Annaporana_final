package com.example.recipeapp;

public class Recipe {
    private String name;
    private String videoUrl;  // Add this field to store the video URL

    // Empty constructor required for Firestore or Firebase Realtime Database
    public Recipe() {}

    public Recipe(String name, String videoUrl) {
        this.name = name;
        this.videoUrl = videoUrl;  // Set video URL in the constructor
    }

    // Getter and Setter for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and Setter for videoUrl
    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
