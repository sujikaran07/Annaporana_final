package com.example.recipeapp;

import android.content.SharedPreferences; // For storing and retrieving search history
import android.os.Bundle;
import android.text.TextUtils; // For checking if the search query is empty
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager; // For setting up RecyclerView with a linear layout
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchFragment extends Fragment {

    // Declare UI elements and variables
    private EditText searchEditText; // Input field for the search query
    private RecyclerView searchResultsRecyclerView; // List for displaying search history
    private SearchHistoryAdapter searchHistoryAdapter; // Adapter to manage the RecyclerView
    private List<String> searchHistory; // List to hold the search history
    private SharedPreferences sharedPreferences; // For storing the search history persistently

    // onCreateView method to initialize the fragment view
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment's layout
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize UI elements by finding them in the layout
        searchEditText = view.findViewById(R.id.search_recipes); // Input field for search
        searchResultsRecyclerView = view.findViewById(R.id.search_results_list); // RecyclerView for search results

        // Initialize SharedPreferences for storing search history
        sharedPreferences = getActivity().getSharedPreferences("SearchHistory", getActivity().MODE_PRIVATE);
        searchHistory = new ArrayList<>(sharedPreferences.getStringSet("history", new HashSet<>())); // Load the saved search history

        // Set up the RecyclerView with a LinearLayoutManager and connect it to the adapter
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        searchHistoryAdapter = new SearchHistoryAdapter(searchHistory, this::removeHistoryItem); // Pass the search history and delete function to the adapter
        searchResultsRecyclerView.setAdapter(searchHistoryAdapter); // Set the adapter to the RecyclerView

        // Setup Search button functionality
        ImageView searchButton = view.findViewById(R.id.search_button); // Find the search button (ensure correct ID)
        searchButton.setOnClickListener(v -> performSearch()); // Set an onClickListener to trigger the search

        return view; // Return the created view
    }

    // Method to handle search action
    private void performSearch() {
        // Get the search query from the EditText
        String query = searchEditText.getText().toString().trim();

        // Check if the query is not empty
        if (!TextUtils.isEmpty(query)) {
            // Add the query to search history
            addSearchHistory(query);
            searchEditText.setText(""); // Clear the input field
            // Notify the user of the search (you can replace this with actual search functionality)
            Toast.makeText(getActivity(), "Searching for: " + query, Toast.LENGTH_SHORT).show();
        }
    }

    // Method to add the query to search history
    private void addSearchHistory(String query) {
        // Check if the query is already in the history
        if (!searchHistory.contains(query)) {
            // If not, add it to the history list
            searchHistory.add(query);
            searchHistoryAdapter.notifyDataSetChanged(); // Notify the adapter that the data has changed
            saveSearchHistory(); // Save the updated search history to SharedPreferences
        }
    }

    // Method to save search history in SharedPreferences
    private void saveSearchHistory() {
        // Convert the search history list to a Set and save it to SharedPreferences
        Set<String> historySet = new HashSet<>(searchHistory);
        sharedPreferences.edit().putStringSet("history", historySet).apply(); // Apply the changes
    }

    // Method to remove an item from the search history
    private void removeHistoryItem(String query) {
        // Remove the query from the history list
        searchHistory.remove(query);
        searchHistoryAdapter.notifyDataSetChanged(); // Notify the adapter to update the RecyclerView
        saveSearchHistory(); // Save the updated history
    }

    // Adapter Class for the RecyclerView
    private static class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {

        private List<String> searchHistory; // List to hold search history
        private OnItemClickListener onItemClickListener; // Interface for handling item click events

        // Constructor for the adapter
        public SearchHistoryAdapter(List<String> searchHistory, OnItemClickListener onItemClickListener) {
            this.searchHistory = searchHistory; // Initialize the search history
            this.onItemClickListener = onItemClickListener; // Initialize the click listener
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate the custom layout for each item (ensure correct spelling of the layout file)
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_serch_results, parent, false); // Make sure the layout file is named correctly
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // Get the query at the current position in the list
            String query = searchHistory.get(position);
            holder.queryTextView.setText(query); // Set the query text to the TextView
            // Set up a click listener for the delete button
            holder.deleteButton.setOnClickListener(v -> onItemClickListener.onDeleteClick(query));
        }

        @Override
        public int getItemCount() {
            return searchHistory.size(); // Return the size of the search history list
        }

        // ViewHolder class to hold references to the individual views in each item
        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView queryTextView; // TextView for displaying the query
            ImageView deleteButton; // Button to delete the search history item

            // Constructor for the ViewHolder
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                queryTextView = itemView.findViewById(R.id.query_text); // Find the query TextView in the layout
                deleteButton = itemView.findViewById(R.id.delete_icon); // Find the delete button in the layout
            }
        }

        // Interface for handling delete click events
        public interface OnItemClickListener {
            void onDeleteClick(String query);
        }
    }
}
