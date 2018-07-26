package com.example.android.popularmovies.network;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Reviews {

    @SerializedName("results")
    private final List<Review> reviews = new ArrayList<>();

    public List<Review> getReviews() {
        return reviews;
    }
}
