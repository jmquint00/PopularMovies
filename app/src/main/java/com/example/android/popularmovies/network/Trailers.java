package com.example.android.popularmovies.network;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Trailers {

    @SerializedName("results")
    private final List<Trailer> trailers = new ArrayList<>();

    public List<Trailer> getTrailers() {
        return trailers;
    }
}
