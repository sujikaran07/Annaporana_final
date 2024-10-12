package com.example.recipeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

public class ReviewPage extends AppCompatActivity {

    private RatingBar ratingBar;
    private String recipeId;  // The ID of the recipe being rated
    private DatabaseReference recipeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_review);

        // Get recipeId from the Intent
        recipeId = getIntent().getStringExtra("recipeId");

        // Initialize RatingBar and other views
        ratingBar = findViewById(R.id.ratingBar);
        TextView callToActionText = findViewById(R.id.callToActionText);

        // Firebase reference to the specific recipe's rating
        recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId).child("reviews");

        // Set listener for rating bar
        callToActionText.setOnClickListener(v -> submitRating());
    }

    // Method to submit the rating
    private void submitRating() {
        float userRating = ratingBar.getRating(); // Get the rating value

        if (userRating == 0) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the rating in Firebase
        saveRatingInFirebase(userRating);
    }

    // Save the new rating to Firebase and calculate the average
    private void saveRatingInFirebase(float userRating) {
        recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalRatings = 0;
                float totalRatingSum = 0;

                // Check if any ratings already exist
                if (snapshot.exists()) {
                    totalRatings = snapshot.child("totalRatings").getValue(Long.class);
                    totalRatingSum = snapshot.child("totalRatingSum").getValue(Float.class);
                }

                // Add the new rating to the sum and increase the total rating count
                totalRatingSum += userRating;
                totalRatings++;

                // Calculate the new average
                float averageRating = totalRatingSum / totalRatings;
                int roundedAverage = Math.round(averageRating);

                // Save the updated values back to Firebase
                recipeRef.child("totalRatings").setValue(totalRatings);
                recipeRef.child("totalRatingSum").setValue(totalRatingSum);
                recipeRef.child("averageRating").setValue(roundedAverage);

                // Navigate to the review success page
                navigateToReviewSuccessPage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReviewPage.this, "Failed to submit review", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to navigate to the review success page
    private void navigateToReviewSuccessPage() {
        Intent intent = new Intent(this, ReviewSucess.class);
        startActivity(intent);
        finish();  // Close the current activity
    }
}
