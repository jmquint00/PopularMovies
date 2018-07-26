package com.example.android.popularmovies.network;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Movies {

    @SerializedName("results")
    private final List<Movie> movies = new ArrayList<>();

    public List<Movie> getMovies() {
        return movies;
    }
}
